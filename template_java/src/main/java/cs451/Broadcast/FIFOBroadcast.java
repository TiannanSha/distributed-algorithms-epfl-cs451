package cs451.Broadcast;

import cs451.Host;
import cs451.NetworkGlobalInfo;
import cs451.links.Packet;

import java.util.Collections;
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
     * pkts delivered by urb but didn't satisfy the fifo property last time checked
     */
    List<Packet> pktsViolatedFifo = new LinkedList<>();

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
        // todo maybe sort by pktid ascendingly to make it possibly return more packets
        //System.out.println("fifo deliver after urb deliver, pkts from urb: " + pkts);
        Collections.sort(pkts, (Packet p1, Packet p2)-> (p1.getPktId()-p2.getPktId()));
        for (Packet pkt: pkts) {
            if (canDeliver(pkt)) {
                res.add(pkt);
                // increment next pktId for source
                nodeIdToNextPktId.put(pkt.getSrc(), nodeIdToNextPktId.get(pkt.getSrc()) + 1);
            } else {
                pktsViolatedFifo.add(pkt);
            }
        }

        // check again whether some pkts didn't satisfy the fifo order can now be delivered
        Collections.sort(pktsViolatedFifo, (Packet p1, Packet p2)-> (p1.getPktId()-p2.getPktId()));
        for (Packet pkt: pktsViolatedFifo) {
            if (canDeliver(pkt)) {
                res.add(pkt);
                // increment next pktId for source
                nodeIdToNextPktId.put(pkt.getSrc(), nodeIdToNextPktId.get(pkt.getSrc()) + 1);
            }
        }
        for (Packet pkt: res) {
            // these packets no longer violate fifo and can be delivered now
            pktsViolatedFifo.remove(pkt);
        }
        return res;
    }

    private boolean canDeliver(Packet pkt) {
        int nextPktId = nodeIdToNextPktId.get(pkt.getSrc());
        return pkt.getPktId() == nextPktId;
    }

    /**
     * this function should be repeatedly called
     * @return
     */
    public List<Packet> getDeliverablePackets() {
        return uniformReliableBroadcast.getDeliverablePackets();
    }

}
