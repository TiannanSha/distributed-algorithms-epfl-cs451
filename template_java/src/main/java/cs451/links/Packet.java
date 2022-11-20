package cs451.links;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * a packet contains messages with continuous ids sorted in ascending order
 */

public class Packet {
    byte[] data;
    int pktId;
    int numMsgs;
    int firstMsgId;
    public static final int MAX_NUM_MSG = 8;
    boolean isACK;
    short src; // todo maybe can fit in one byte or char since maximum 128 processes
    short dst;
    short relayedBy;

    public Packet(List<Message> messages, int pktId, boolean isACK, short src, short dst, short relayedBy) {
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
        this.dst = (short)dst;
        this.relayedBy = (short)relayedBy;
    }

    public Packet(byte[] data, int numMsgs, int firstMsgId, int pktId, boolean isACK, short src, short dst, short relayedBy) {
        this.data = data;
        this.pktId= pktId;
        this.numMsgs = numMsgs;
        this.firstMsgId = firstMsgId;
        this.src = (short)src;
        this.dst = (short)dst;
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

    public int getPktId() {
        return pktId;
    }

    public short getDst() {
        return dst;
    }

    public short getSrc() {
        return src;
    }

    public short getRelayedBy() {
        return relayedBy;
    }

    public int getFirstMsgId() {
        return firstMsgId;
    }

    public int getNumMsgs() {
        return numMsgs;
    }

    public int getLastMsgId() {
        return firstMsgId + numMsgs - 1;
    }

    public void setDst(short dst) {
        this.dst = dst;
    }

    public void setRelayedBy(short relayedBy) {
        this.relayedBy = relayedBy;
    }
}
