package cs451.Broadcast;

import cs451.Host;
import cs451.NetworkGlobalInfo;
import cs451.links.NodeIDToPktIDs;
import cs451.links.Packet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class NodeIdToPackets {
    HashMap<Short, HashSet<Packet>> nodeIdToPackets = new HashMap<>();

    public NodeIdToPackets() {
        for (Host host: NetworkGlobalInfo.getAllHosts()) {
            nodeIdToPackets.put((short)host.getId(), new HashSet<>());
        }
    }

    // sync use object's intrinsic lock of object, every thing in locked method can only be accessed by one thread at a time,
    // i.e. the thread with this object's intrinsic lock
    synchronized public void addPacket(Packet pkt) {
        nodeIdToPackets.get(pkt.getSrc()).add(pkt);
    }

    synchronized public boolean alreadyContainsPacket(Packet packet) {
        return nodeIdToPackets.get(packet.getSrc()).contains(packet);
    }

    /**
     * special method for reliable broadcast
     * 'this' object represents pending packets
     * todo no actually pending should store actual pkts
     */
    synchronized List<Packet> getDeliverablePackets(Acks acks, NodeIDToPktIDs delivered) {
        List<Packet> res = new LinkedList<>();
        for (short nodeId: nodeIdToPackets.keySet()) {
            HashSet<Packet> pkts = nodeIdToPackets.get(nodeId);
            for (Packet pkt:pkts) {
                // check if packet that has been acked for majority
                if (  acks.ackedByMajority(pkt.getSrc(), pkt.getPktId()) &&
                        !delivered.alreadyContainsPacket(pkt.getSrc(), pkt.getPktId())){
                    res.add(pkt);
                }
            }
        }
        return res;
    }
}



