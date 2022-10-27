package cs451.links;

import java.util.List;

public class Packet {
    byte[] data;
    int pktId;
    int numMsgs;
    static final int MAX_NUM_MSG = 8;

    public Packet(List<Message> messages, int pktId) {
        this.data = marshalMessages(messages);
        this.pktId = pktId;
        numMsgs = messages.size();
    }

    private byte[] marshalMessages(List<Message> messages) {
        // todo implement marshaling
        return null;
    }

    public byte[] marshalPacket() {
        return null;
    }

    static public Packet unmarshalPacket(byte[] data) {
        return null;
    }
}
