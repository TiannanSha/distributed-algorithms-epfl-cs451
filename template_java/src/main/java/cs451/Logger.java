package cs451;

import cs451.links.Packet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
        //System.out.println(entry);
        stringBuffer.append(entry);
    }

    // append log after sending a batch of messages
    public void appendBroadcastLogs(int lowMsgId, int highMsgId) {
        for (int msgId=lowMsgId; msgId<=highMsgId; msgId++) {
            String entry = String.format("b %d\n", msgId);
            //System.out.println("append to log with entry: " + entry);
            stringBuffer.append(entry);
        }
    }

    public void appendDeliveryLogs( short src, int msgIdLow, int msgIdHigh) {
        for (int msgId=msgIdLow; msgId<=msgIdHigh; msgId++) {
            String entry = String.format("d %d %d\n", src, msgId);
            //System.out.println(entry);
            stringBuffer.append(entry);
        }
    }

    public void flushToDisk() {
        System.out.println("enter flushToDisk, outputfile: " + outputFile);
        try {
            writer.write(stringBuffer.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * append the delievery of messages in a list of packets
     * @param pkts
     */
    public void appendDeliveryLogs(List<Packet> pkts) {
        for (Packet pkt: pkts) {
            appendDeliveryLogs(pkt.getSrc(), pkt.getFirstMsgId(), pkt.getLastMsgId());
        }
    }

    public void appendDecisionLogs(HashSet<Integer> decision) {
        ArrayList<Integer> arrayDecision = new ArrayList<>(decision);
        String entry = "";
        for (int i=0; i<decision.size(); i++) {
            if (i==decision.size()-1) {
                // this is the last item in the decision
                entry = entry + arrayDecision.get(i) + "\n";
            } else {
                // this is not the last item
                entry = entry + arrayDecision.get(i) + " ";
            }
        }
        stringBuffer.append(entry);
    }
}
