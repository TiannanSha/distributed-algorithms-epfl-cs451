package cs451.Broadcast;

import cs451.NetworkGlobalInfo;
import cs451.links.NodeIDToPktIDs;
import cs451.links.Packet;

import java.util.List;

public class UniformReliableBroadcast {

    NodeIDToPktIDs delivered = new NodeIDToPktIDs();

    /**
     * stores packets that I have seen (beb delivered then beb broadcast, or originally broadcast by me) but not delivered
     */
    NodeIdToPackets pending = new NodeIdToPackets();

    /**
     * for each packet stores who has relayed it (beb delivered, then relayed). If enough nodes have beb delivered and then
     * relayed a packet, we know we can deliver this packet
     *
     * because an entry's sole purpose is for checking whether [nodeId, pktId] can be delivered, an entry can be
     * deleted after [nodeId, pktId] is delivered
     */
    Acks acks = new Acks();

    BestEffortBroadcast bestEffortBroadcast = new BestEffortBroadcast();

    public UniformReliableBroadcast() {

    }


    public void broadcast(Packet pkt) {
        pending.addPacket(pkt);
        bestEffortBroadcast.broadcast(pkt);
    }


    public List<Packet> deliver() {

        Packet pkt = bestEffortBroadcast.deliver();
        acks.addAck(pkt.getSrc(), pkt.getPktId(), pkt.getRelayedBy());
        // relay a beb delivered packet if haven't done so
        if (!pending.alreadyContainsPacket(pkt)) {
            pending.addPacket(pkt);
            // FIXME: 20.11.22 rebroadcas, need to change the relay by field. do it in beb.broadcast
            bestEffortBroadcast.broadcast(pkt);
        }

        List<Packet> res = pending.getDeliverablePackets(acks, delivered);
        // todo maybe delete acks for deleted packet, maybe can also delete pending?
        return res;
    }

    public boolean canDeliver(Packet pkt) {
        return acks.ackedByMajority(pkt.getSrc(), pkt.getPktId())
                &&!delivered.alreadyContainsPacket(pkt.getSrc(), pkt.getPktId());
    }
}
