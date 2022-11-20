package cs451.Broadcast;

import cs451.Host;
import cs451.NetworkGlobalInfo;
import cs451.links.Packet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * FRB1–FRB4: Same as properties RB1–RB4 in (regular) reliable broadcast (Mod- ule 3.2).
 * FRB5: FIFO delivery: If some process broadcasts message m1 before it broadcasts message m2, then no correct process delivers m2 unless it has already delivered m1.
 */
public class FIFOBroadcast {
    UniformReliableBroadcast uniformReliableBroadcast = new UniformReliableBroadcast();

    /**
     * for each node stores the last delivered packet id.
     */
    HashMap<Short, Integer> nodeIdToNextPktId = new HashMap<>();

    public FIFOBroadcast() {
        for (Host host: NetworkGlobalInfo.getAllHosts()) {
            nodeIdToNextPktId.put(host.getId(), 1);
        }
    }

    public void broadcast(Packet pkt) {
        uniformReliableBroadcast.broadcast(pkt);
    }

    public List<Packet> deliver() {
        List<Packet> res = new LinkedList<>();
        List<Packet> pkts = uniformReliableBroadcast.deliver();
        for (Packet pkt: pkts) {
            if (canDeliver(pkt)) {
                res.add(pkt);
                // increment next pktId for source
                nodeIdToNextPktId.put(pkt.getSrc(), nodeIdToNextPktId.get(pkt.getSrc()) + 1);
            }
        }
        return res;
    }

    private boolean canDeliver(Packet pkt) {
        int nextPktId = nodeIdToNextPktId.get(pkt.getSrc());
        return pkt.getPktId() == nextPktId;
    }

}
