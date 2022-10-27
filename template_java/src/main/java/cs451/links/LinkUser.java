package cs451.links;

import cs451.Host;
import cs451.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A LinkUser use links to send and receive messages.
 * For sending,
 */
public class LinkUser {
    PerfectLink perfectLink;
    Host myHost;
    int numMsgsToSend;
    int sendToHost;
    int pktId = 0;
    // todo can use maybe 4 threads and 2 threads sending 2 threads receiving
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    Logger logger;

    public LinkUser(Host myHost, int numMsgsToSend, int sendToHost, Logger logger) {
        this.myHost = myHost;
        perfectLink = new PerfectLink(myHost);
        this.numMsgsToSend = numMsgsToSend;
        this.sendToHost = sendToHost;
        this.logger = logger;
    }

    public void sendMsgs(int msgIdLow, int msgIdHigh) {
        List<Message> msgs= new ArrayList<>();;
        for (int i=msgIdLow; i <= msgIdHigh; i++) {
            String content = Integer.toString(i);
            msgs.add(new Message(content, i));
            if (msgs.size()==Packet.MAX_NUM_MSG) {
                // a packet is full, we can send it now and re-accumulate a new batch of messages
                Packet pkt = new Packet(msgs, pktId);
                pktId++;
                msgs = new ArrayList<>();
            }
        }
    }

    public void sendAllMsgs() {
        // todo try split to multiple threads sending concurrently
        executorService.submit(()->sendMsgs(0, numMsgsToSend));
    }

    public void startReceivingLoop() {
        executorService.submit(this::receivingLoop);
    }

    private void receivingLoop() {
        while (true) {
            perfectLink.deliver();

        }
    }

    public void stopLinkUserAndFlushLog() {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(0, TimeUnit.SECONDS);
            logger.flushToDisk();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
