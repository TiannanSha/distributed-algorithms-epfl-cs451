package cs451.LatticeAgreement;

import cs451.Broadcast.BestEffortBroadcast;
import cs451.Host;
import cs451.LatticeAgreement.Messages.ProposalACKMsg;
import cs451.LatticeAgreement.Messages.ProposalMsg;
import cs451.LatticeAgreement.Messages.ProposalNACKMsg;
import cs451.NetworkGlobalInfo;
import cs451.links.Packet;
import cs451.links.PerfectLink;

import java.util.HashSet;

/**
 * implement single-shot lattic agreement
 */
public class Agreement {

    PerfectLink perfectLink = NetworkGlobalInfo.perfectLink;
    BestEffortBroadcast bestEffortBroadcast = new BestEffortBroadcast();
    public Proposer proposer;
    public Acceptor acceptor;
    int shotId;

    Agreement( int slotId) {
        //this.perfectLink = perfectLink;
        proposer = new Proposer();
        acceptor = new Acceptor();
        this.shotId = slotId;
    }

    class Proposer {
        boolean active = false;
        int ackCount = 0;
        int nackCount = 0;
        int activeProposalNumber = 0;
        HashSet<Integer> proposedValues = new HashSet<>();

        /**
         * values proposed by this agreement when calling propose()
         */
        HashSet<Integer> proposal;

        public Proposer() {

        }

        /**
         *
         * @param proposal values I propose
         * @return valuea I have decided
         */
        public synchronized HashSet<Integer> propose(HashSet<Integer> proposal ) {
            // todo need to protect shared variable as sending and receiving are concurrent, probably just make all methods sync
            System.out.println("in propose()");
            System.out.println("proposal: " + proposal);
            proposedValues = proposal;
            active = true;
            activeProposalNumber++;
            ackCount = 0;
            nackCount = 0;
            this.proposal = proposal;
            ProposalMsg proposalMsg = new ProposalMsg(proposal, proposedValues, activeProposalNumber);
            short myHostId = NetworkGlobalInfo.getMyHost().getId();
            byte[] data = proposalMsg.serialize();
            Packet pkt = new Packet(data, data.length, myHostId,
                    myHostId, myHostId, Packet.PROPOSE_MSG, shotId);
            bestEffortBroadcast.broadcast(pkt);
            // fixme need to handle the packet send to myself

            // lock this object's intrinsic lock, wait till received enough ACK then notify this to continue

            try {
                while (active) {// according to javadoc wait is recommended to be put in while
                    this.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return new HashSet<>(proposedValues); // return deep copy to avoid race condition
        }

        public synchronized void handleACK(ProposalACKMsg proposalACKMsg) {
            if (proposalACKMsg.getProposalNumber() == activeProposalNumber) {
                ackCount++;
                checkNeedToRebroadcast();
                checkDecisionReached();
            }
            System.out.println("in handleACK(), proposalACKMsg" + proposalACKMsg);
        }

        public synchronized void handleNACK(ProposalNACKMsg proposalNACKMsg) {
            if (proposalNACKMsg.getProposalNumber() == activeProposalNumber) {
                proposedValues.addAll(proposalNACKMsg.getAcceptedValues());
                nackCount++;
                checkNeedToRebroadcast();
                checkDecisionReached();
            }
            System.out.println("in handle NACK(), proposalNACKMsg" + proposalNACKMsg);
        }

        private void checkNeedToRebroadcast() {
            //upon nack_count𝑖 > 0 and ack_count𝑖 + nack_count𝑖 ≥ 𝑓 + 1 and active𝑖 = true:
            if (nackCount > 0 && ackCount + nackCount >= NetworkGlobalInfo.getMajorityThreshold() && active) {
                activeProposalNumber++;
                ackCount = 0;
                nackCount = 0;
                ProposalMsg proposalMsg = new ProposalMsg(proposal, proposedValues, activeProposalNumber);
                short myHostId = NetworkGlobalInfo.getMyHost().getId();
                byte[] data = proposalMsg.serialize();
                Packet pkt = new Packet(data, data.length, myHostId,
                        myHostId, myHostId, Packet.PROPOSE_MSG, shotId);
                bestEffortBroadcast.broadcast(pkt);
                // FIXME: 14.12.22 handle packet send to myself
            }
        }

        private void checkDecisionReached() {
            if (ackCount >= NetworkGlobalInfo.getAllHosts().size()/2+1 && active) {
                active = false;
                // notify propose() to stop waiting and return the decided values
                this.notify();
            }
        }

    }

    class Acceptor {
        HashSet<Integer> acceptedValues = new HashSet<>();
        public Acceptor() {

        }

        public void handleProposalMsg(ProposalMsg proposalMsg, Packet pkt) {
            System.out.println("handle proposalMsg :" + proposalMsg);
            HashSet<Integer> proposedValues = proposalMsg.getProposedValues();
            if (proposedValues.containsAll(acceptedValues)) {
                acceptedValues = proposedValues;
                // send ACK to sender of proposalMsg
                ProposalACKMsg proposalACKMsg = new ProposalACKMsg(proposalMsg.getActiveProposalNumber());
                byte[] data = proposalACKMsg.serialize();
                Packet ACKpkt = new Packet(data, data.length,
                        NetworkGlobalInfo.getMyHost().getId(), pkt.getSrc(), NetworkGlobalInfo.getMyHost().getId(),
                        Packet.PROPOSE_ACK_MSG, shotId);
                Host dstHost = NetworkGlobalInfo.getHostById(pkt.getSrc());
                perfectLink.send(ACKpkt, dstHost);
            } else {
                acceptedValues.addAll(proposedValues);
                // send NACK to sender of proposalMSg
                ProposalNACKMsg proposalNACKMsg = new ProposalNACKMsg(proposalMsg.getActiveProposalNumber(),
                        acceptedValues);
                byte[] data = proposalNACKMsg.serialize();
                Packet NACKpkt = new Packet(data, data.length,
                        NetworkGlobalInfo.getMyHost().getId(), pkt.getSrc(), NetworkGlobalInfo.getMyHost().getId(),
                        Packet.PROPOSE_NACK_MSG, shotId);
                Host dstHost = NetworkGlobalInfo.getHostById(pkt.getSrc());
                perfectLink.send(NACKpkt, dstHost);
            }
        }
    }
}
