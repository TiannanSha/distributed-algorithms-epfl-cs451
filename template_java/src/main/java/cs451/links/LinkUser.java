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
    Host sendToHost;
    int pktId = 0;
    // todo can use maybe 4 threads and 2 threads sending 2 threads receiving
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    Logger logger;
    List<Host> hosts;

    public LinkUser(Host myHost, int numMsgsToSend, Host sendToHost, Logger logger, List<Host> hosts ) {
        this.myHost = myHost;
        perfectLink = new PerfectLink(myHost, hosts);
        this.numMsgsToSend = numMsgsToSend;
        this.sendToHost = sendToHost;
        this.logger = logger;
        this.hosts = hosts;
        System.out.println(this);
    }

    public void sendMsgs(int msgIdLow, int msgIdHigh) {
        System.out.println("sendMsgs()\n");
        List<Message> msgs= new ArrayList<>();;
        for (int msgId=msgIdLow; msgId <= msgIdHigh; msgId++) {
            String content = Integer.toString(msgId);
            msgs.add(new Message(content, msgId));
            if (msgs.size()==Packet.MAX_NUM_MSG) {
                // a packet is full, we can send it now and re-accumulate a new batch of messages
                Packet pkt = new Packet(msgs, pktId, false, myHost.getId());
                perfectLink.send(pkt, sendToHost);
                logger.appendBroadcastLogs(msgs.get(0).msgId, msgs.get(0).msgId+msgs.size()-1);
                pktId++;
                msgs = new ArrayList<>();
            }
        }
        // send the last batch of meesages
        Packet pkt = new Packet(msgs, pktId, false, myHost.getId());
        perfectLink.send(pkt, sendToHost);
        pktId++;
        logger.appendBroadcastLogs(msgs.get(0).msgId, msgs.get(0).msgId+msgs.size()-1);
        //logger.appendBroadcastLogs(msgIdLow, msgIdHigh);
    }

    public void sendAllMsgs() {
        // todo try split to multiple threads sending concurrently
        // message id starts from
        System.out.println("sendAllMsgs()\n");
        //executorService.submit(()->sendMsgs(1, numMsgsToSend));
        sendMsgs(1, numMsgsToSend);
    }

    public void startReceivingLoop() {
        executorService.submit(this::receivingLoop);
    }

    private void receivingLoop() {
        while (true) {
            System.out.println("link user receivingLoop");
            Packet pkt = perfectLink.deliver();
            logger.appendDeliveryLogs(pkt.src, pkt.firstMsgId, pkt.firstMsgId+pkt.numMsgs-1);
            System.out.println("link user receivingLoop after perfectLink deliver");
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

    public PerfectLink getPerfectLink() {
        return perfectLink;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Logger getLogger() {
        return logger;
    }
}