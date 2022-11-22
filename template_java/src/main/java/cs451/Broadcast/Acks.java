package cs451.Broadcast;

import cs451.Host;
import cs451.NetworkGlobalInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * for each packet identified by (src, pktId), store the nodes that have seen this packet. That is
 * either the original sender of the packet or the node that relayed the packet. This information is
 * for knowing when I can deliver a packet.
 */
// todo check if we need thread safe version
public class Acks {
    HashMap<Short, HashMap<Integer, HashSet<Short>>> acks = new HashMap<>();

    public Acks() {
        for (Host host: NetworkGlobalInfo.getAllHosts()) {
            acks.put(host.getId(), new HashMap<>());
        }
    }

    public void addAck(short src, int packetId, short relayBy) {
        if (!acks.get(src).containsKey(packetId)) {
            // check if this packet is already in acks. if not, create a new relayset for it
            HashSet<Short> relaySet = new HashSet<>();
            relaySet.add(relayBy);
            acks.get(src).put(packetId, relaySet);
        } else {
            // this packet already have a relay set
            acks.get(src).get(packetId).add(relayBy);
        }
    }

    /**
     * when a packet is delivered, we no longer need to keep tracking Acks to know when it can be delivered
     */
    public void removeAck(short src, int packetId) {
        acks.get(src).remove(packetId);
    }

    /**
     * return true if a packet is acked, i.e. seen and relayed by majority of the nodes in the network
     * @param src
     * @param packetId
     * @return
     */
    public boolean ackedByMajority(short src, int packetId) {
        return acks.get(src).get(packetId).size() > NetworkGlobalInfo.getAllHosts().size()/2;
    }

    @Override
    public String toString() {
        return "Acks{" +
                "acks=" + acks +
                '}';
    }
}
