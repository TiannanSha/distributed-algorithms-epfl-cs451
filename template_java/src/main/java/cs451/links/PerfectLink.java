package cs451.links;

import cs451.Host;
import cs451.Logger;
import cs451.NetworkGlobalInfo;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * perfect link class uses stubborn link. Note stubborn link part is embedded in the while loop.
 * for performance and code readability reasons we don't include
 */

public class PerfectLink implements Link {
    // todo change this to something similar to tcp, or at least a map of senderHost->pktId
    // for preventing double delivery, each entry consists of two integers, [senderHost, pktId]
    // FIXME: 19.11.22 use a thread safe set
    //HashSet<List<Integer>> receivedPktIds = new HashSet<>();
    NodeIDToPktIDs delivered = new NodeIDToPktIDs();

    // todo probably should be [dst, pktId]... because we are sending to different nodes
    // fixme implement a thread safe set, ideally a sparse representation of packet id , but maybe no need
    // packetIds sent by me that are schedule in thread pool but haven't been ACKed
    //HashSet<Integer> pendingPktIds = new HashSet<>();
    ConcurrentHashSet pendingPktIds = new ConcurrentHashSet();

    private final FairLossLink fLink;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static final int resendWaitingTimeMs = 1000;

    static final int MAX_PENDING_PKTS = 1024; // max num packets scheduled in thread pool but not acked

    /**
     *   maximum 32 packets that are waiting to be sent
     *   packets can be taken off the queue only when pending packets (scheduled in thread pool but not Acked)
     *   is smaller than certain threshold
     */
    BlockingQueue<Packet> sendQueue = new LinkedBlockingQueue<>(32);

    public PerfectLink() {
        fLink = new FairLossLink();
        executorService.submit(this::startSendingLoop);
    }


    // todo use a scheduled thread pool https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/ScheduledExecutorService.html
    // have a hashmap store all pktId->Future
    // when receives a pkt, just cancel the future
    @Override
    public void send(Packet packet, Host host) {
        System.out.println("packet.dst" + packet.dst);
        sendQueue.add(packet);
    }

    private void submitASendTask(Packet packet, Host host) {
        // todo debug this
        System.out.println("in perfect link submitasendtask()");
        pendingPktIds.add(packet.pktId);
        NetworkGlobalInfo.getLogger().appendBroadcastLogs(packet.firstMsgId, packet.firstMsgId+packet.numMsgs-1);
        byte[] buf = packet.marshalPacket();
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, host.getInetIp(), host.getPort());
        Future<?> future = executorService.submit(
                ()-> {
                    // keep sending until ACK is received
                    while (pendingPktIds.contains(packet.pktId)) {
                        System.out.println("in perfect link send() loop, pktId = " + packet.pktId);
                        fLink.send(datagramPacket, host);
                        try {
                            Thread.sleep(resendWaitingTimeMs);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                );
    }

    private void startSendingLoop() {
        while (true) {
            // todo maybe this should sleep sometime between check
            System.out.println("in sendingLoop");
            if (pendingPktIds.size()<MAX_PENDING_PKTS) {
                try {
                    Packet pkt = sendQueue.take();
                    submitASendTask(pkt, NetworkGlobalInfo.getHostById(pkt.dst));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
        System.out.println(pktRecv);
        if (!pktRecv.isACK) {
            // received actual message packet pktRecv
            System.out.println("perfectlink deliver not ACK");
            // send an ACK for this packet, and update received pktRecv
            System.out.println("perfectlink deliver before reply ACK");
            Packet ACK = new Packet(new ArrayList<Message>(), pktRecv.pktId, true,
                    NetworkGlobalInfo.getMyHost().getId(), pktRecv.src, NetworkGlobalInfo.getMyHost().getId());
            fLink.send(ACK, NetworkGlobalInfo.getAllHosts().get(pktRecv.src-1));
            System.out.println("after flink send");

//            List<Integer> pktIdTuple = new LinkedList<>();
//            pktIdTuple.add((int)pktRecv.src);
//            pktIdTuple.add(pktRecv.pktId);
            if (!delivered.alreadyContainsPacket(pktRecv.src, pktRecv.pktId)) {
                // this is a new non-ack packet
                delivered.addPacket(pktRecv.src, pktRecv.pktId);
                return pktRecv;
            } else {
                // this is an old non-ack packet
                return null;
            }
        }
        // this is an ACK packet
        System.out.println("perfectlink deliver ACK");
        pendingPktIds.remove(pktRecv.pktId);
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
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
