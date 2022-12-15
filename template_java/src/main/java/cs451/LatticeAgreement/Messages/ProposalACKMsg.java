package cs451.LatticeAgreement.Messages;

import java.nio.ByteBuffer;

public class ProposalACKMsg {
    int proposalNumber;
    public static final int MSG_SIZE = 4; // only one int

    public ProposalACKMsg(int proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    public byte[] serialize() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(MSG_SIZE);
        try {
            byteBuffer.putInt(proposalNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteBuffer.array();
    }

    public static ProposalACKMsg deserialize(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int proposalNum = byteBuffer.getInt();
        return new ProposalACKMsg(proposalNum);
    }

    public int getProposalNumber() {
        return proposalNumber;
    }

    @Override
    public String toString() {
        return "ProposalACKMsg{" +
                "proposalNumber=" + proposalNumber +
                '}';
    }
}
