package cs451.links;

import java.nio.ByteBuffer;

public class Message {
    String content;
    int msgId;

    public Message(String content, int msgId) {
        this.content = content;
        this.msgId = msgId;
    }

    public byte[] marshal() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(content.length());
        return byteBuffer.array();
    }
}
