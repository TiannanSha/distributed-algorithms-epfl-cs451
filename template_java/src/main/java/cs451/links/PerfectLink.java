package cs451.links;

import cs451.Host;

import java.net.DatagramPacket;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;

/**
 * perfect link class uses stubborn link. Note stubborn link part is embedded in the while loop.
 * for performance and code readability reasons we don't include
 */

public class PerfectLink implements Link {

    HashSet<Integer> receivedPktIds = new HashSet<>();  // for preventing double delivery
    HashSet<Integer> ACKedSentPktIds = new HashSet<>(); // packetIds sent by me that have been ACKed
    Host myHost;
    private final FairLossLink fLink;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    List<Host> hosts;

    public PerfectLink(Host myHost, List<Host> host) {
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
        Packet pkt = fLink.deliver();
        if (!pkt.isACK) {
            System.out.println("perfectlink deliver not ACK");
            // send an ACK for this packet, and update received pkt
            receivedPktIds.add(pkt.pktId);
            System.out.println("perfectlink deliver before reply ACK");
            fLink.send(new Packet(new ArrayList<Message>(), pkt.pktId, true,
                    myHost.getId()), hosts.get(pkt.src-1));
            if (!receivedPktIds.contains(pkt.pktId)) {
                // this is a new packet
                return pkt;
            }
        } else {
            System.out.println("perfectlink deliver ACK");
            ACKedSentPktIds.add(pkt.pktId);
        }
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
