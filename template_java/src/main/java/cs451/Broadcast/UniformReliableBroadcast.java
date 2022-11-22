package cs451.Broadcast;

import cs451.NetworkGlobalInfo;
import cs451.links.NodeIDToPktIDs;
import cs451.links.Packet;

import java.util.LinkedList;
import java.util.List;

public class UniformReliableBroadcast {

    NodeIDToPktIDs delivered = new NodeIDToPktIDs();

    /**
     * stores packets that I have seen (beb delivered then beb broadcast, or originally broadcast by me) but not delivered
     */
    NodeIdToPackets pending = new NodeIdToPackets();

    /**
     * for each packet stores who has seen it (beb delivered, then relayed). If enough nodes have beb delivered and then
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
        // todo probably should add myself to ack... only when delivers get more ack. so if two nodes 1,2
        // todo node 1 send <1, pkt1> to node 2, and hear <1,pkt1>, <1,pkt1> has only ack from 2, can never reach majority
        // todo so should include <1,pkt1>: ackBY 1,2, I mean of course I have seen the message I broadcast
        acks.addAck(pkt.getSrc(), pkt.getPktId(), pkt.getRelayedBy());

        pending.addPacket(pkt);
        bestEffortBroadcast.broadcast(pkt);
    }


    public List<Packet> deliver() {
        System.out.println("in urb deliver()");
        Packet pkt = bestEffortBroadcast.deliver();
        System.out.println("beb deliver after beb deliver " + pkt);
        if (pkt==null) {
            // this is not a data packet
            System.out.println("in urb deliver packet is null");
            return new LinkedList<>();
        }
        // add the relayedby and myself to the set of nodes have seen <src, pktId>
        acks.addAck(pkt.getSrc(), pkt.getPktId(), pkt.getRelayedBy());
        acks.addAck(pkt.getSrc(), pkt.getPktId(), NetworkGlobalInfo.getMyHost().getId());
        // relay a beb delivered packet if haven't done so
        if (!pending.alreadyContainsPacket(pkt)) {
            pending.addPacket(pkt);
            System.out.println("rebroadcast pkt: " + pkt);
            bestEffortBroadcast.broadcast(pkt);
        } else {
            // we are dealing with a already broadcast data packet
            System.out.println("*** pending: " + pending);
            System.out.println("*** ACK" + acks);
            List<Packet> res = pending.getDeliverablePackets(acks, delivered);
            System.out.println("in urb deliver res: " + res);
            for (Packet packet: res) {
                delivered.addPacket(packet.getSrc(), packet.getPktId());
            }
            return res;
        }

//        List<Packet> res = pending.getDeliverablePackets(acks, delivered);
//        System.out.println("in urb deliver res: " + res);
        // todo maybe delete acks for deleted packet,
        return new LinkedList<>();
    }

    public boolean canDeliver(Packet pkt) {
        return acks.ackedByMajority(pkt.getSrc(), pkt.getPktId())
                &&!delivered.alreadyContainsPacket(pkt.getSrc(), pkt.getPktId());
    }

    public List<Packet> getDeliverablePackets() {
        List<Packet> res = pending.getDeliverablePackets(acks, delivered);
        System.out.println("in urb deliver res: " + res);
        return res;
    }
}
