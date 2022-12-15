package cs451.LatticeAgreement.Messages;

import java.nio.ByteBuffer;
import java.util.HashSet;

//⟨proposal,proposed_value𝑖,active_proposal_number𝑖⟩
public class ProposalMsg {
    HashSet<Integer> proposal = new HashSet<>();
    HashSet<Integer> proposedValues = new HashSet<>();
    int activeProposalNumber = 0;

    public ProposalMsg(HashSet<Integer> proposal, HashSet<Integer> proposedValues, int activeProposalNumber) {
        this.proposal = proposal;
        this.proposedValues = proposedValues;
        this.activeProposalNumber = activeProposalNumber;
    }

    public HashSet<Integer> getProposal() {
        return proposal;
    }

    public HashSet<Integer> getProposedValues() {
        return proposedValues;
    }

    public int getActiveProposalNumber() {
        return activeProposalNumber;
    }

    public byte[] serialize() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getMsgSize()+8);  // 4 for proposal.size() 4 for proposedValues.size()
        try {
            byteBuffer.putInt(activeProposalNumber);
            // need to know how many nums are in proposal and how many are in proposedValues
            byteBuffer.putInt(proposal.size());
            for (int p:proposal) {
                byteBuffer.putInt(p);
            }
            byteBuffer.putInt(proposedValues.size());
            for (int p:proposedValues) {
                byteBuffer.putInt(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteBuffer.array();
    }

    public static ProposalMsg deserialize(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int activeProposalNumber = byteBuffer.getInt();

        int proposalSize = byteBuffer.getInt();
        HashSet<Integer> proposalSet = new HashSet<>();
        for (int i=0; i<proposalSize; i++) {
            proposalSet.add(byteBuffer.getInt());
        }

        int proposedValuesSize = byteBuffer.getInt();
        HashSet<Integer> proposedValuesSet = new HashSet<>();
        for (int i=0; i<proposedValuesSize; i++) {
            proposedValuesSet.add(byteBuffer.getInt());
        }
        return new ProposalMsg( proposalSet, proposedValuesSet, activeProposalNumber);
    }

    public int getMsgSize() {
        // first 4 for ativeProposalNumber, other for proposal and proposedValues
        return 4 + 4*proposal.size() + 4*proposedValues.size();
    }

    @Override
    public String toString() {
        return "ProposalMsg{" +
                "proposal=" + proposal +
                ", proposedValues=" + proposedValues +
                ", activeProposalNumber=" + activeProposalNumber +
                '}';
    }
}

