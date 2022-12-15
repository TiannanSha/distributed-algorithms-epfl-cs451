package cs451.LatticeAgreement;

import cs451.LatticeAgreement.Messages.ProposalACKMsg;
import cs451.LatticeAgreement.Messages.ProposalMsg;
import cs451.LatticeAgreement.Messages.ProposalNACKMsg;
import cs451.links.Packet;
import cs451.links.PerfectLink;

import java.util.HashSet;

/**
 * implement multi-shot lattice agreement. in multi-shot lattice agreement, processes run single-shot lattice agreement on a series of slots
 *
 * config is p, vs, ds
 * p denotes the number of proposals for each process, vs denotes the maximum number of elements in a proposal, and ds denotes the maximum number of distinct elements across all proposals of all processes.
 */
public class MultiAgreements {
    int numAgreement;
    static Agreement[] agreements;
    //PerfectLink perfectLink;

    public MultiAgreements(int numAgreement) {
        this.numAgreement = numAgreement;
//        this.perfectLink = perfectLink;
        agreements = new Agreement[numAgreement];
        for (int shotId=0; shotId<numAgreement; shotId++) {
            agreements[shotId] = new Agreement(shotId);
        }
    }

    public static void handlePacket(Packet pkt) {
        if (pkt==null) return;
        Agreement agreement = agreements[pkt.getShotId()];
        System.out.println("in handlePacket() pkt: " + pkt);
        if (pkt.getMsgType() == Packet.PROPOSE_MSG) {
            ProposalMsg proposalMsg = ProposalMsg.deserialize(pkt.getData());
            agreement.acceptor.handleProposalMsg(proposalMsg, pkt);
        } else if (pkt.getMsgType() == Packet.PROPOSE_ACK_MSG) {
            ProposalACKMsg proposalACKMsg = ProposalACKMsg.deserialize(pkt.getData());
            agreement.proposer.handleACK(proposalACKMsg);
        } else if (pkt.getMsgType() == Packet.PROPOSE_NACK_MSG) {
            ProposalNACKMsg proposalNACKMsg = ProposalNACKMsg.deserialize(pkt.getData());
            agreement.proposer.handleNACK(proposalNACKMsg);
        }
    }

    /**
     *  ask the correct agreement to propose. It blocks until a decision has been reached and then return the decision
     * @param shotID
     * @param proposal
     */
    public HashSet<Integer> propose(int shotID, HashSet<Integer> proposal) {
        HashSet<Integer> decision = agreements[shotID].proposer.propose(proposal);
        return decision;
    }
}
