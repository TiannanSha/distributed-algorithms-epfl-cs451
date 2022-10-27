package cs451;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
    // A thread-safe, mutable sequence of characters
    StringBuffer stringBuffer = new StringBuffer();
    String outputFile;
    BufferedWriter writer;

    public Logger(String outputFile) {
        this.outputFile = outputFile;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendBroadcastLog(int msgId) {
        String entry = String.format("b %d\n", msgId);
        stringBuffer.append(entry);
    }

    public void appendDeliveryLog(int msgId) {
        String entry = String.format("b %d\n", msgId);
        stringBuffer.append(entry);
    }

    public void flushToDisk() {
        try {
            writer.write(stringBuffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
