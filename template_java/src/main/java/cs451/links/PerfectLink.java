package cs451.links;

import cs451.Host;

import java.util.HashSet;

public class PerfectLink implements Link {
    HashSet<Integer> receivedMsgIds;
    HashSet<Integer> receivedPktIds;
    Host myHost;
    private final StubbornLink stubbornLink;

    public PerfectLink(Host myHost) {
        this.myHost = myHost;
        stubbornLink = new StubbornLink(myHost);
    }

    @Override
    public void send(Packet packet, Host host) {
        stubbornLink.send(packet, host);
    }

    @Override
    public Packet deliver() {
        Packet pkt = stubbornLink.deliver();
        if (!receivedPktIds.contains(pkt.pktId)) {
            receivedPktIds.add(pkt.pktId);
            return pkt;
        }
        return null;
    }
}
