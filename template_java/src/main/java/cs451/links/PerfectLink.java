package cs451.links;

import cs451.Host;

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
    HashSet<int[]> receivedPktIds = new HashSet<>();
    HashSet<Integer> ACKedSentPktIds = new HashSet<>(); // packetIds sent by me that have been ACKed
    Host myHost;
    private final FairLossLink fLink;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    List<Host> hosts;

    public PerfectLink(Host myHost, List<Host> hosts) {
        this.myHost = myHost;
        fLink = new FairLossLink(myHost);
        this.hosts = hosts;
    }


    // todo use a scheduled thread pool https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/ScheduledExecutorService.html
    // have a hashmap store all pktId->Future
    // when receives a pkt, just cancel the future
    @Override
    public void send(Packet packet, Host host) {
        System.out.println("in perfect link send()");
        byte[] buf = packet.marshalPacket();
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, host.getInetIp(), host.getPort());
        Future<?> future = executorService.submit(
                ()-> {
                    // keep sending until ACK is received
                    while (!ACKedSentPktIds.contains(packet.pktId)) {
                        System.out.println("in perfect link send() loop, pktId = " + packet.pktId);
                        fLink.send(datagramPacket, host);
                        try {
                            Thread.sleep(1000);
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
        if (!pktRecv.isACK) {
            // received actual message packet pktRecv
            System.out.println("perfectlink deliver not ACK");
            // send an ACK for this packet, and update received pktRecv
            System.out.println("perfectlink deliver before reply ACK");
            Packet ACK = new Packet(new ArrayList<Message>(), pktRecv.pktId, true,
                    myHost.getId());
            fLink.send(ACK, hosts.get(pktRecv.src-1));
            System.out.println("after flink send");

            int[] pktIdTuple = new int[2];
            pktIdTuple[0] = pktRecv.src;
            pktIdTuple[1] = pktRecv.pktId;
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
