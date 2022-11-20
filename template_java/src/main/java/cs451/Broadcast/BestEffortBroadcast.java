package cs451.Broadcast;

import cs451.Host;
import cs451.Logger;
import cs451.NetworkGlobalInfo;
import cs451.links.Packet;
import cs451.links.PerfectLink;

import java.util.List;

/** BEB1: Validity: If a correct process broadcasts a message m, then every correct process eventually delivers m.
 * BEB2: No duplication: No message is delivered more than once.
 * BEB3: No creation: If a process delivers a message m with sender s, then m was previously broadcast by process s.
 */


public class BestEffortBroadcast {
   PerfectLink perfectLink; // perfect link can be used to send to different hosts from same port. note the requirement was each process use only one port

    BestEffortBroadcast() {
        perfectLink = new PerfectLink();
    }

    public void broadcast(Packet pkt) {
        for (Host host: NetworkGlobalInfo.getOtherHosts()) {
            //fixme pkt.src should be set when creating the packet at the src node
            // fixme but relayedBy and dst need to be changed
            pkt.setDst(host.getId());
            pkt.setRelayedBy(NetworkGlobalInfo.getMyHost().getId());
            perfectLink.send(pkt, host);
        }
    }


    /**
     * blocks until an unseen data packet can be returned...
     */
    public Packet deliver() {
        return perfectLink.deliverDataPacket();
    }
}
