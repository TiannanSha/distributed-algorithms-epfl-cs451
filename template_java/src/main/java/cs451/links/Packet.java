package cs451.links;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * a packet contains messages with continuous ids sorted in ascending order
 */

public class Packet {

    /**
     * max num of msg in a pkt
     */
    public static final int MAX_NUM_MSG = 8;

    /**
     * different message types
     */
    public static final short PROPOSE_MSG = 0;
    public static final short PROPOSE_ACK_MSG = 1;
    public static final short PROPOSE_NACK_MSG = 2;
    public static final short M2_MSG = 3;

    byte[] data;
    int pktId;//todo maybe can delete irrelevant fields
    int numMsgs;
    int firstMsgId;
    boolean isACK;
    short src; // todo maybe can fit in one byte or char since maximum 128 processes
    short dst;
    short relayedBy;
    int plPktId; // id used and set by perfect link when sending.
    short msgType = M2_MSG;
    int shotId = 0;  // which round of agreement does this packet belong to
    int dataLen = 0;

    /**
     * used for creating packets for upper layer, no need to worry about plPktId
     * @param messages
     * @param pktId
     * @param isACK
     * @param src
     * @param dst
     * @param relayedBy
     */
    public Packet(List<Message> messages, int pktId, boolean isACK, short src, short dst,
                  short relayedBy) {
        this.data = PacketSerializer.serializeMessagesToData(messages);
        this.pktId = pktId;
        numMsgs = messages.size();
        if (isACK) {
            //System.out.println("info: creating ACK packet");
            firstMsgId = -1;
        } else {
            firstMsgId = messages.get(0).msgId;
        }
        this.isACK = isACK;
        this.src = (short)src;
        this.dst = (short)dst;
        this.relayedBy = (short)relayedBy;
        // todo add new field here

    }

    /**
     * create a deep copy of input packet
     */
    static public Packet clonePacket(Packet packet) {
        byte[] data = packet.data.clone();
        return new Packet(data, packet.numMsgs, packet.firstMsgId,
                packet.pktId, packet.isACK,
                packet.src, packet.dst, packet.relayedBy,
                packet.msgType, packet.shotId, packet.dataLen);
    }

//    public Packet(List<Message> messages, int pktId, boolean isACK, short src, short dst,
//                  short relayedBy, int plPktId) {
//        this.data = PacketSerializer.serializeMessagesToData(messages);
//        this.pktId = pktId;
//        numMsgs = messages.size();
//        if (isACK) {
//            //System.out.println("info: creating ACK packet");
//            firstMsgId = -1;
//        } else {
//            firstMsgId = messages.get(0).msgId;
//        }
//        this.isACK = isACK;
//        this.src = (short)src;
//        this.dst = (short)dst;
//        this.relayedBy = (short)relayedBy;
//        //this.plPktId = plPktId;
//        // todo add new field here
//    }

    public Packet(byte[] data, int numMsgs, int firstMsgId, int pktId, boolean isACK,
                  short src, short dst, short relayedBy, short msgType, int shotId, int dataLen) {
        this.data = data;
        this.pktId= pktId;
        this.numMsgs = numMsgs;
        this.firstMsgId = firstMsgId;
        this.src = (short)src;
        this.dst = (short)dst;
        this.isACK = isACK;
        this.relayedBy = relayedBy;
        //this.plPktId = plPktId;
        this.msgType = msgType;
        this.shotId = shotId;
        this.dataLen = dataLen;
        // todo add new field here
    }

    /**
     * Packet constructor used for m3
     * @param data
     * @param src
     * @param dst
     * @param relayedBy
     */
    public Packet(byte[] data, int dataLen, short src, short dst, short relayedBy, short msgType, int shotId) {
        this.data = data;
        this.pktId= 0;
        this.numMsgs = 1;
        this.firstMsgId = 0;
        this.src = (short)src;
        this.dst = (short)dst;
        this.isACK = false;
        this.relayedBy = relayedBy;
        //this.plPktId = plPktId;
        this.msgType = msgType;
        this.shotId = shotId;
        this.dataLen = dataLen;
        // todo add new field here
    }

    public byte[] marshalPacket() {
        return PacketSerializer.serialize(this);
    }

    static public Packet unmarshalPacket(byte[] data) {
        return PacketSerializer.deserializePacket(data);
    }

//    @Override
//    public String toString() {
////       return "{ pkt.src = " + this.src + ", pkt.pktId" + this.pktId + ",pkt.isACK = "
////               + isACK + " this.plPktId = " + this.plPktId;
//        return "{ pkt.src = " + this.src + ", pkt.pktId = " + this.pktId + ", pkt.isACK = "
//                + isACK + ", relayedBy = "+this.relayedBy ;
//    }


    @Override
    public String toString() {
        String[] msgTypes = new String[]{"Propose", "ProposeACK", "ProposeNACK"};
        return "Packet{" +
                ", pktId=" + pktId +
                ", numMsgs=" + numMsgs +
                ", firstMsgId=" + firstMsgId +
                ", isACK=" + isACK +
                ", src=" + src +
                ", dst=" + dst +
                ", relayedBy=" + relayedBy +
                ", plPktId=" + plPktId +
                ", MsgType=" + msgTypes[msgType] +
                ", shotId=" + shotId +
                ", plPktId=" + plPktId +
                ", dataLen=" + dataLen +
                '}';
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

    public byte[] getData() {
        return data;
    }

    public short getMsgType() {
        return msgType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Packet packet = (Packet) o;
        return pktId == packet.pktId && isACK == packet.isACK && src == packet.src;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pktId, isACK, src);
    }

        public int getPlPktId() {
        return plPktId;
    }
//
    public void setPerfectLinkId(int linkLayerId) {
        this.plPktId = linkLayerId;
    }

    public int getShotId() {
        return shotId;
    }

    public int getDataLen() {
        return dataLen;
    }

    public int getPacketSize() {
        return PacketSerializer.DATA_OFFSET + dataLen;
    }
}
