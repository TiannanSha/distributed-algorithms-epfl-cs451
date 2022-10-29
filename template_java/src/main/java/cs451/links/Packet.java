package cs451.links;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * a packet contains messages with continuous ids
 */

public class Packet {
    byte[] data;
    int pktId;
    int numMsgs;
    int firstMsgId;
    static final int MAX_NUM_MSG = 8;
    boolean isACK;
    short src; // todo maybe can fit in one byte or char since maximum 128 processes



    public Packet(List<Message> messages, int pktId, boolean isACK, int src) {
        this.data = PacketSerializer.serializeMessagesToData(messages);
        this.pktId = pktId;
        numMsgs = messages.size();
        if (isACK) {
            System.out.println("info: creating ACK packet");
            firstMsgId = -1;
        } else {
            firstMsgId = messages.get(0).msgId;
        }
        this.isACK = isACK;
        this.src = (short)src;
    }

    public Packet(byte[] data, int numMsgs, int firstMsgId, int pktId, boolean isACK, int src) {
        this.data = data;
        this.pktId= pktId;
        this.numMsgs = numMsgs;
        this.firstMsgId = firstMsgId;
        this.src = (short)src;
        this.isACK = isACK;
    }

    public byte[] marshalPacket() {
        return PacketSerializer.serialize(this);
    }

    static public Packet unmarshalPacket(byte[] data) {
        return PacketSerializer.deserializePacket(data);
    }

    @Override
    public String toString() {
       return "pkt.src = " + this.src + ", pkt.pktId" + this.pktId + ",pkt.isACK = " + isACK;
    }


}
