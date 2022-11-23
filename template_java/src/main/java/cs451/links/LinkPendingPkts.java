package cs451.links;

import java.util.HashMap;
import java.util.HashSet;

public class LinkPendingPkts {
    /**
     * src -> [pktId, dst]
     */
    HashMap<Short, HashSet<Integer>> linkPendingPkts = new HashMap<>();

    public LinkPendingPkts() {

    }

    synchronized public void addPacket(short src, int pktId) {
        linkPendingPkts.get(src).add(pktId);
    }

    synchronized public boolean alreadyContainsPacket(short src, int pktId) {
        return linkPendingPkts.get(src).contains(pktId);
    }

    synchronized public int getTotalNumPkts() {
        int res = 0;
        for (Short nodeId: linkPendingPkts.keySet()) {
            res += linkPendingPkts.get(nodeId).size();
        }
        return res;
    }

    /**
     * remove a packet from this collection
     * @param pktId
     */
    synchronized public void removePacket(short src, int pktId) {
        HashSet<Integer> pktIds = linkPendingPkts.get(src);
        pktIds.remove(pktId);
    }

//    /**
//     * special method for reliable broadcast
//     * 'this' object represents pending packets
//     * todo no actually pending should store actual pkts
//     */
//    synchronized List<Packet> getDeliverablePackets(Acks acks) {
//        for (short nodeId: nodeIdToPktIds.keySet()) {
//            HashSet<Integer> pktIds = nodeIdToPktIds.get(nodeId);
//            for (int pktId:pktIds) {
//                // check if <NodeId, pktId> represents a packet that has been acked for majority
//                acks.ackedByMajority(nodeId, pktId);
//            }
//        }
//    }


    @Override
    public String toString() {
        return "NodeIDToPktIDs{" +
                "nodeIdToPktIds=" + linkPendingPkts +
                '}';
    }

}
