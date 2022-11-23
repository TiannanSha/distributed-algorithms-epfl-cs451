package cs451.links;

import java.nio.ByteBuffer;
import java.util.List;

public class PacketSerializer {

    // so now packet size is < 32 bytes..

    public static final int PKTID_OFFSET=0;
    public static final int NUM_MSGS_OFFSET=4;
    public static final int FIRST_MSG_ID_OFFSET=8;
    public static final int IS_ACK_OFFSET = 12; //use char, 2bytes
    public static final int SRC_OFFSET=14; // src is short, 2bytes
    public static final int DST_OFFSET=16; // dst is short, 2bytes
    public static final int RELAYED_BY_OFFSET=18; // relayedby is short, 2bytes
    public static final int PL_PKT_ID_OFFSET = 20; // plpktid is int 4 bytes
    // todo add new field here
    public static final int DATA_OFFSET =24;

    public static final int MAX_DATA_SIZE = 8 * Message.MAX_MSG_CONTENT_SIZE;
    public static final int MAX_PACKET_SIZE = DATA_OFFSET+ MAX_DATA_SIZE;

    public static byte[] serialize(Packet packet) {
//        System.out.println("in serialize, serialzing packet: " + packet);
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
        try {
            byteBuffer.putInt(packet.pktId);
            byteBuffer.putInt(packet.numMsgs);
            byteBuffer.putInt(packet.firstMsgId);

            if (packet.isACK) {
                byteBuffer.putChar('T');
            } else {
                byteBuffer.putChar('F');
            }
//            System.out.println("after put ack");
            byteBuffer.putShort(packet.src);
            byteBuffer.putShort(packet.dst);
            byteBuffer.putShort(packet.relayedBy);
//            System.out.println("after put relayby");
//            System.out.println("byteBuffer: "+byteBuffer);
            byteBuffer.putInt(packet.plPktId);
            // todo add new field here

            byteBuffer.put(packet.data);
            //System.out.println("after put packet.data");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteBuffer.array();
    }

    static public byte[] serializeMessagesToData(List<Message> msgs) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_DATA_SIZE);
        for (Message m:msgs) {
            byteBuffer.put(m.marshal());
        }
        return byteBuffer.array();
    }

    static public Packet deserializePacket(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int pktId = byteBuffer.getInt();
        int numMsgs = byteBuffer.getInt();
        int firstMsgId = byteBuffer.getInt();
        boolean isACK = byteBuffer.getChar() == 'T';
        short src = byteBuffer.getShort();
        short dst = byteBuffer.getShort();
        short relayedBy = byteBuffer.getShort();
        int plPktId = byteBuffer.getInt();
        // todo add new field here

        byte[] data = new byte[MAX_DATA_SIZE];
        Packet res = new Packet(data, numMsgs, firstMsgId, pktId, isACK, src, dst, relayedBy);
        res.setPerfectLinkId(plPktId);
        return res;
    }
}
