package cs451.LatticeAgreement.Messages;

import java.nio.ByteBuffer;
import java.util.HashSet;

public class ProposalNACKMsg {
    int proposalNumber;
    HashSet<Integer> acceptedValues = new HashSet<>();

    public ProposalNACKMsg(int proposalNumber, HashSet<Integer> acceptedValues) {
        this.proposalNumber = proposalNumber;
        this.acceptedValues = acceptedValues;
    }

    public byte[] serialize() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getMsgSize());
        try {
            byteBuffer.putInt(proposalNumber);
            for (int val: acceptedValues) {
                byteBuffer.putInt(val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteBuffer.array();
    }

    public static ProposalNACKMsg deserialize(byte[] bytes) {
        int acceptedValuesSize = bytes.length/4 - 1 ; // -1 because first int is proposalNumber
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int proposalNum = byteBuffer.getInt();
        HashSet<Integer> set = new HashSet<>();
        for (int i=0; i<acceptedValuesSize; i++) {
            set.add(byteBuffer.getInt());
        }
        return new ProposalNACKMsg(proposalNum, set);
    }

    public int getProposalNumber() {
        return proposalNumber;
    }

    public HashSet<Integer> getAcceptedValues() {
        return acceptedValues;
    }

    public int getMsgSize() {
        return 4 + 4*acceptedValues.size(); // first 4 for proposalNumber, other for acceptedValues
    }

    @Override
    public String toString() {
        return "ProposalNACKMsg{" +
                "proposalNumber=" + proposalNumber +
                ", acceptedValues=" + acceptedValues +
                '}';
    }
}
