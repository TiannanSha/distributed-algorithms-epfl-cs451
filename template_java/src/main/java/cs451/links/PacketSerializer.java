package cs451.links;

import java.nio.ByteBuffer;
import java.util.List;

public class PacketSerializer {



    public static final int PKTID_OFFSET=0;
    public static final int NUM_MSGS_OFFSET=4;
    public static final int FIRST_MSG_ID_OFFSET=8;
    public static final int IS_ACK_OFFSET = 12; //use char
    public static final int src=14; // src is short
    public static final int DATA_OFFSET =16;

    public static final int MAX_DATA_SIZE = 32;
    public static final int MAX_PACKET_SIZE = DATA_OFFSET+ MAX_DATA_SIZE;

    public static byte[] serialize(Packet packet) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
        byteBuffer.putInt(packet.pktId);
        byteBuffer.putInt(packet.numMsgs);
        byteBuffer.putInt(packet.firstMsgId);

        if (packet.isACK) {
            byteBuffer.putChar('T');
        } else {
            byteBuffer.putChar('F');
        }
        byteBuffer.putShort(packet.src);
        byteBuffer.put(packet.data);

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
        byte[] data = new byte[bytes.length-DATA_OFFSET];
        return new Packet(data, numMsgs, firstMsgId, pktId, isACK, src);
    }
}
