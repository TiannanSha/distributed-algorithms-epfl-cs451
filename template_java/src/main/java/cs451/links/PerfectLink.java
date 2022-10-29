package cs451.links;

import cs451.Host;
import cs451.Logger;

import java.net.DatagramPacket;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * perfect link class uses stubborn link. Note stubborn link part is embedded in the while loop.
 * for performance and code readability reasons we don't include
 */

public class PerfectLink implements Link {

    // for preventing double delivery, each entry consists of two integers, [senderHost, pktId]
    HashSet<List<Integer>> receivedPktIds = new HashSet<>();
    HashSet<Integer> ACKedSentPktIds = new HashSet<>(); // packetIds sent by me that have been ACKed
    Host myHost;
    private final FairLossLink fLink;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    List<Host> hosts;
    public static final int resendWaitingTimeMs = 1000;
    Logger logger;

    public PerfectLink(Host myHost, List<Host> hosts, Logger logger) {
        this.myHost = myHost;
        fLink = new FairLossLink(myHost);
        this.hosts = hosts;
        this.logger = logger;
    }


    // todo use a scheduled thread pool https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/ScheduledExecutorService.html
    // have a hashmap store all pktId->Future
    // when receives a pkt, just cancel the future
    @Override
    public void send(Packet packet, Host host) {
        System.out.println("in perfect link send()");
        logger.appendBroadcastLogs(packet.firstMsgId, packet.firstMsgId+packet.numMsgs-1);
        byte[] buf = packet.marshalPacket();
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, host.getInetIp(), host.getPort());
        Future<?> future = executorService.submit(
                ()-> {
                    //todo append broadcast log here
                    // keep sending until ACK is received
                    while (!ACKedSentPktIds.contains(packet.pktId)) {
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

    @Override
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
                    myHost.getId());
            fLink.send(ACK, hosts.get(pktRecv.src-1));
            System.out.println("after flink send");

            List<Integer> pktIdTuple = new LinkedList<>();
            pktIdTuple.add((int)pktRecv.src);
            pktIdTuple.add(pktRecv.pktId);
            if (!receivedPktIds.contains(pktIdTuple)) {
                // this is a new non-ack packet
                receivedPktIds.add(pktIdTuple);
                return pktRecv;
            } else {
                // this is an old non-ack packet
                return null;
            }
        }
        // this is an ACK packet
        System.out.println("perfectlink deliver ACK");
        ACKedSentPktIds.add(pktRecv.pktId);
        return null;
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
