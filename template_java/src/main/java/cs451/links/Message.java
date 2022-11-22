package cs451.links;

import java.nio.ByteBuffer;

public class Message {
    byte[] content;
    int msgId;
    // note that this can be easily changed if we need larger messages
    public static final int MAX_MSG_CONTENT_SIZE = 4;


    public Message(byte[] content, int msgId) {
        this.content = content;
        this.msgId = msgId;
    }

    public byte[] marshal() {
//        ByteBuffer byteBuffer = ByteBuffer.allocate(content.length());
        return content;
    }

    public int getMsgId() {
        return msgId;
    }
}
