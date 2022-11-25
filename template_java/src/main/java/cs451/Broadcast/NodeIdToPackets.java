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
        // FIXME: 22.11.22 contains a packet as long as contains <packetSrc, pktid>
        // todo two packets are equal if they have same <src, pktId>, maybe override equals and hashcode function for Packet
        return nodeIdToPackets.get(packet.getSrc()).contains(packet);
    }

    /**
     * special method for reliable broadcast
     * 'this' object represents pending packets
     * todo no actually pending should store actual pkts
     */
    synchronized List<Packet> getDeliverablePackets(Acks acks, NodeIDToPktIDs delivered) {
        //System.out.println("@@@in get deliverable packets");
        //System.out.println("@@@pending"+this);
        //System.out.println("@@@acks:" + acks);
        //System.out.println("@@@delivered" + delivered);
        List<Packet> res = new LinkedList<>();
        for (short nodeId: nodeIdToPackets.keySet()) {
            HashSet<Packet> pkts = nodeIdToPackets.get(nodeId);
            for (Packet pkt:pkts) {
                //System.out.println("@@@pkt: " + pkt);
                //System.out.println("@@@acks.ackedByMajority(pkt.getSrc(), pkt.getPktId()):" + acks.ackedByMajority(pkt.getSrc(), pkt.getPktId()));
                //System.out.println("@@@!delivered.alreadyContainsPacket(pkt.getSrc(), pkt.getPktId()))" + !delivered.alreadyContainsPacket(pkt.getSrc(), pkt.getPktId()));
                // check if packet that has been acked for majority
                if (  acks.ackedByMajority(pkt.getSrc(), pkt.getPktId()) &&
                        !delivered.alreadyContainsPacket(pkt.getSrc(), pkt.getPktId())){
                    //System.out.println("@@@added to result");
                    res.add(pkt);
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return "NodeIdToPackets{" +
                "nodeIdToPackets=" + nodeIdToPackets +
                '}';
    }
}



