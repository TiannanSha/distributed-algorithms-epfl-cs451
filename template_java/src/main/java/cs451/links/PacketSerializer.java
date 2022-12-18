package cs451.links;

import java.nio.ByteBuffer;
import java.sql.SQLOutput;
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
    public static final int MSG_TYPE_OFFSET = 24; // msgType is short 2 bytes
    public static final int SHOT_ID_OFFSET = 26; // shotID is int 4 bytes
    public static final int DATA_LEN_OFFSET = 30; // dataLen is int 4 bytes


    // todo add new field here
    public static final int DATA_OFFSET =34;

    public static final int MAX_DATA_SIZE = 8 * Message.MAX_MSG_CONTENT_SIZE; // for M2 ACK packets

    /**
     * max total size of a packet. For proposal msg, 1024 int for both proposal and proposevalues plus packet
     * header length. +8 for storing proposal length and proposedValues length。 +4 for proposal active number
     * + 512 additional space just to be safe
     */
    public static final int MAX_PACKET_SIZE = 2048*4 + DATA_OFFSET + 8 + 4 + 512;

    public static byte[] serialize(Packet packet) {
//        System.out.println("in serialize, serialzing packet: " + packet);
//        System.out.println("packet.getPacketSize()"+packet.getPacketSize());
        ByteBuffer byteBuffer = ByteBuffer.allocate(packet.getPacketSize());
        try {
            byteBuffer.putInt(packet.pktId);
            byteBuffer.putInt(packet.numMsgs);
            byteBuffer.putInt(packet.firstMsgId);

            if (packet.isACK) {
                byteBuffer.putChar('T');
            } else {
                byteBuffer.putChar('F');
            }
//            //System.out.println("after put ack");
            byteBuffer.putShort(packet.src);
            byteBuffer.putShort(packet.dst);
            byteBuffer.putShort(packet.relayedBy);
//            //System.out.println("after put relayby");
//            //System.out.println("byteBuffer: "+byteBuffer);
            byteBuffer.putInt(packet.plPktId);
            byteBuffer.putShort(packet.getMsgType());
            byteBuffer.putInt(packet.shotId);
            byteBuffer.putInt(packet.dataLen);
            // todo add new field here
//            //System.out.println("packet.isACK = "+packet.isACK);
//            //System.out.println("byteBuffer.capacity()"+byteBuffer.capacity());
//            //System.out.println("byteBuffer.position()"+byteBuffer.position());
            // fixme if data null don't put
            if (packet.data!=null) {
                byteBuffer.put(packet.data);
            }
            ////System.out.println("after put packet.data");

        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace(System.out);
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
        short msgType = byteBuffer.getShort();
        int shotId = byteBuffer.getInt();  // which round of agreement does this packet belong to
        int dataLen = byteBuffer.getInt();
        // todo add new field here


        // FIXME: if data len = 0 no need to read data, data =null, i.e. this is just an ACK
        if (dataLen>0) {
            byte[] data = new byte[dataLen];
//            System.out.println("---deserialize packet---");
//            System.out.println("plpktid = " + plPktId);
//            System.out.println("isACK = " + isACK);
//            System.out.println("datalen = " + dataLen);
//            System.out.println("msgType = " + msgType);
//            System.out.println("byteBuffer.capacity() = "+byteBuffer.capacity());
//            System.out.println("byteBuffer.position() = "+byteBuffer.position());
            byteBuffer.get(data); // read bytes to data[0, dataLen]
            //byte[] data = new byte[MAX_DATA_SIZE];
            Packet res = new Packet(data, numMsgs, firstMsgId, pktId, isACK, src, dst, relayedBy,
                    msgType, shotId, dataLen);
            res.setPerfectLinkId(plPktId);
            return res;
        } else {
            //if data len = 0 no need to read data, data =null, i.e. this is just an ACK
            Packet res = new Packet(null, numMsgs, firstMsgId, pktId, isACK, src, dst, relayedBy,
                    msgType, shotId, dataLen);
            res.setPerfectLinkId(plPktId);
            return res;
        }

    }
}
