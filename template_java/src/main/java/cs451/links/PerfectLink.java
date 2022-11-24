package cs451.links;

import cs451.Host;
import cs451.NetworkGlobalInfo;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * perfect link class uses stubborn link. Note stubborn link part is embedded in the while loop.
 * for performance and code readability reasons we don't include
 * todo !!the true identifier of a link layer message is (relayedby, plpktid). think of perfect link as socket
 * todo relayby is like socket id, plpktid gives this is the nth packet out of socket
 */
public class PerfectLink implements Link {
    // todo change this to something similar to tcp, or at least a map of senderHost->pktId
    // for preventing double delivery, each entry consists of two integers, [senderHost, pktId]
    // FIXME: 19.11.22 use a thread safe set
    //HashSet<List<Integer>> receivedPktIds = new HashSet<>();
    NodeIDToPktIDs delivered = new NodeIDToPktIDs();

    /**
     * unique id for each packet sent out of this perfect link, help for identify ack for which and avoid duplicate
     */
    int plPktId = 1;

    // todo probably should be [dst, pktId]... because we are sending to different nodes
    // fixme implement a thread safe set, ideally a sparse representation of packet id , but maybe no need
    // packets sent/relayed by me that are schedule in thread pool but haven't been ACKed
    //HashSet<Integer> pendingPktIds = new HashSet<>();
    //ConcurrentHashSet pendingPktIds = new ConcurrentHashSet();

    /**
     * I might broadcast (node1, pkt1) and also relay (node2, pkt1), these are different packets
     * if you want to use perfect link to send same msg twice, you should change pktid.
     * but when broadcasting a pkt, I need to seperately ACK (1,pkt1) sent to node 2 and 3
     */
    NodeIDToPktIDs pendingPlPktIds = new NodeIDToPktIDs();

    private final FairLossLink fLink;

//    private ExecutorService threadPoolForSendingTasks = Executors.newFixedThreadPool(2);
    private ScheduledExecutorService schedulerForSendingTasks = Executors.newScheduledThreadPool(2);
    private ExecutorService threadForSendingLoop = Executors.newSingleThreadExecutor();

    //public static final int resendWaitingTimeMs = 1000;

    static final int MAX_PENDING_PKTS = 1024; // max num packets scheduled in thread pool but not acked

    /**
     *   maximum 32 packets that are waiting to be sent
     *   packets can be taken off the queue only when pending packets (scheduled in thread pool but not Acked)
     *   is smaller than certain threshold
     */
    BlockingQueue<Packet> sendQueue = new LinkedBlockingQueue<>(32);

    public PerfectLink() {
        fLink = new FairLossLink();
        threadForSendingLoop.submit(this::startSendingLoop);
    }


    // todo use a scheduled thread pool https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/ScheduledExecutorService.html
    // have a hashmap store all pktId->Future
    // when receives a pkt, just cancel the future
    @Override
    public void send(Packet packet, Host host) {
        packet.setRelayedBy(NetworkGlobalInfo.getMyHost().getId());
        // plPktId uniquely identifies the each packet send out of this perfect link
        packet.setPerfectLinkId(plPktId);
        plPktId++;

        System.out.println("packet.dst:" + packet.dst);
        sendQueue.add(packet);
        System.out.println("in send, sendQueue add packet:" + packet);
        System.out.println("sendQueue: "+sendQueue);
    }

    private void submitASendTask(Packet packet, Host host) {
        // todo debug this
        System.out.println("###in perfect link submitasendtask() submitting sending packet: " + packet);
        pendingPlPktIds.addPacket(packet.relayedBy, packet.plPktId);
        System.out.println("###in perfect link submitasendtask() pendingPktIds" + pendingPlPktIds);
        //NetworkGlobalInfo.getLogger().appendBroadcastLogs(packet.firstMsgId, packet.firstMsgId+packet.numMsgs-1);
        byte[] buf = packet.marshalPacket();
        //System.out.println("in submit A send task after marshalPacket");
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, host.getInetIp(), host.getPort());
        Future<?> future = schedulerForSendingTasks.scheduleAtFixedRate(
                ()-> {
                    // todo maybe change while to if and use scheduled thread pool to avoid busy waiting?
                    // periodically checking whether ACK is received, if not, resend
                    if (pendingPlPktIds.alreadyContainsPacket(packet.relayedBy, packet.plPktId)) {
                        System.out.println("###inside perfect link scheduler send loop, pkt = " + packet);
                        fLink.send(datagramPacket, host);
//                        try {
//                            Thread.sleep(resendWaitingTimeMs);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                },
                0, 1000, TimeUnit.MILLISECONDS
                );
        System.out.println("after submit send task of sending packet: " + packet);
    }

    private void startSendingLoop() {
        while (true) {
            // todo maybe this should sleep sometime between check
            System.out.println("$$$submitloop in sendingLoop");
            System.out.println("$$$submitloop  sendQueue:" + sendQueue);
            if (pendingPlPktIds.getTotalNumPkts()<MAX_PENDING_PKTS) {
                try {
                    System.out.println("$$$submitloop before sendQueue.take()");
                    Packet pkt = sendQueue.take();
                    System.out.println("$$$submitloop  after sendQueue.take() pkt: " + pkt);
                    submitASendTask(pkt, NetworkGlobalInfo.getHostById(pkt.dst));
                    System.out.println("$$$submitloop  after submit an task");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    /**
     * blocks until receives a packet
     * @return return a packet if this packet is new and contains unseen messages. return null if received
     */
    public Packet deliver() {
        System.out.println("perfectlink deliver()");
        Packet pktRecv = fLink.deliver();
        if (pktRecv==null) return null;
        System.out.println("perfectlink after flink deliver()");
        System.out.println("pktRecv in perfect link deliver"+pktRecv);
        if (!pktRecv.isACK) {
            // received actual message packet pktRecv
            //System.out.println("perfectlink deliver not ACK");
            // send an ACK for this packet, and update received pktRecv
            // todo an ACK only changes data field, isACK field
            // todo put this to a Packet function create ACK
            //System.out.println("perfectlink deliver before reply ACK");
            Packet ACK = new Packet(new ArrayList<Message>(), pktRecv.pktId, true,
                    pktRecv.src, pktRecv.dst, pktRecv.relayedBy);
            ACK.setPerfectLinkId(pktRecv.plPktId);
            // ack should be sent to the relayedby to tell it we received the relayedby
            fLink.send(ACK, NetworkGlobalInfo.getAllHosts().get(pktRecv.relayedBy-1));
            //fLink.send(ACK, NetworkGlobalInfo.getAllHosts().get(pktRecv.src-1));
            //System.out.println("after flink send");

//            List<Integer> pktIdTuple = new LinkedList<>();
//            pktIdTuple.add((int)pktRecv.src);
//            pktIdTuple.add(pktRecv.pktId);
            if (!delivered.alreadyContainsPacket(pktRecv.relayedBy, pktRecv.plPktId)) {
                // this is a new non-ack packet
                delivered.addPacket(pktRecv.relayedBy, pktRecv.plPktId);
                return pktRecv;
            } else {
                // this is an old non-ack packet
                return null;
            }
        }
        // this is an ACK packet
        // for an ack packet, the src and the pktId are both the same as the packet it's acking
        System.out.println("perfectlink deliver ACK");
        System.out.println("ACK:" + pktRecv);
        System.out.println("pendingPlPktIds " + pendingPlPktIds);
        pendingPlPktIds.removePacket(pktRecv.relayedBy, pktRecv.plPktId);
        //System.out.println("after remove, " + pendingPktIds.alreadyContainsPacket(pktRecv.src, ));
        return null;
    }

    /**
     * blocks until an unseen data packet is received
     * @return an unseen data packet. All msgs in unseen data packet are contiguous unseen msgs
     */
    public Packet deliverDataPacket() {
        Packet pkt = deliver();
        while (pkt==null) {
            pkt = deliver();
        }
        return pkt;
    }


    // for testing purpose
    public void waitTillAllTaskFinished() {
        schedulerForSendingTasks.shutdown();
        try {
            schedulerForSendingTasks.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
