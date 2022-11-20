package cs451.links;

import cs451.Broadcast.Acks;
import cs451.Host;
import cs451.NetworkGlobalInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class NodeIDToPktIDs {
    /**
     * map from node id to a list of pktId from this node id
     * node 1 -> [1,2,3,5,6]
     * node 2 -> [2,3,4,5,9]
     */
    HashMap<Short, HashSet<Integer>> nodeIdToPktIds = new HashMap<>();

    public NodeIDToPktIDs() {
        for (Host host: NetworkGlobalInfo.getAllHosts()) {
            nodeIdToPktIds.put((short)host.getId(), new HashSet<>());
        }
    }

    // sync use object's intrinsic lock of object, every thing in locked method can only be accessed by one thread at a time,
    // i.e. the thread with this object's intrinsic lock
    synchronized public void addPacket(short src, int pktId) {
        nodeIdToPktIds.get(src).add(pktId);
    }

    synchronized public boolean alreadyContainsPacket(short src, int pktId) {
        return nodeIdToPktIds.get(src).contains(pktId);
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

}
