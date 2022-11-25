package cs451.Broadcast;

import cs451.NetworkGlobalInfo;
import cs451.links.Message;
import cs451.links.Packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BroadcastUser {

    FIFOBroadcast fifoBroadcast = new FIFOBroadcast();
    int pktId = 1;
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * time to wait after broadcast one packet until broadcast next packet. This is for avoiding submitting too many
     * sending tasks to perfect link
     */
    public static int SLEEP_TIME_BEFORE_NEXT_BROADCAST = 280;

    public BroadcastUser() {

    }

    public void broadcastAllMsgs() {
        // todo try split to multiple threads sending concurrently
        // message id starts from
       // //System.out.println("broadcastAllMsgs()\n");
        //executorService.submit(()->sendMsgs(1, numMsgsToSend));
        broadcastMsgs(1, NetworkGlobalInfo.getNumMsgsToSend());
    }

    public void broadcastMsgs(int msgIdLow, int msgIdHigh) {
        ////System.out.println("broadcast Msgs()\n");
        List<Message> msgs= new ArrayList<>();
        int msgId = msgIdLow;
        for (msgId=msgIdLow; msgId <= msgIdHigh; msgId++) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(Message.MAX_MSG_CONTENT_SIZE);
            byteBuffer.putInt(msgId);
            byte[] content = byteBuffer.array();
            msgs.add(new Message(content, msgId));
            if (msgs.size() == Packet.MAX_NUM_MSG) {
                // a packet is full, we can send it now and re-accumulate a new batch of messages
                // the dst filled should be altered by the beb broadcast layer
                Packet pkt = new Packet(msgs, pktId, false, NetworkGlobalInfo.getMyHost().getId(),
                        (short)-1, NetworkGlobalInfo.getMyHost().getId());
                fifoBroadcast.broadcast(pkt);
                //logger.appendBroadcastLogs(msgs.get(0).msgId, msgs.get(0).msgId+msgs.size()-1);
                NetworkGlobalInfo.getLogger().appendBroadcastLogs(msgs.get(0).getMsgId(),
                        msgs.get(msgs.size()-1).getMsgId());
                pktId++;
                msgs = new ArrayList<>();
            }
            try {
                Thread.sleep(SLEEP_TIME_BEFORE_NEXT_BROADCAST);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int numMsgToSend = msgIdHigh - msgIdLow + 1;
        if (numMsgToSend % Packet.MAX_NUM_MSG != 0) {
            // there is a last batch of messages need to be sent
            Packet pkt = new Packet(msgs, pktId, false, NetworkGlobalInfo.getMyHost().getId(), (short)-1, NetworkGlobalInfo.getMyHost().getId());
            fifoBroadcast.broadcast(pkt);
            NetworkGlobalInfo.getLogger().appendBroadcastLogs(msgs.get(0).getMsgId(),
                    msgs.get(msgs.size()-1).getMsgId());
            pktId++;
        }
    }

    /**
     * keep polling packets from lower layer. And the layer below would poll item from the layer below it, and so on...
     */
    public void startReceivingLoop() {
        executorService.submit(this::receivingLoop);
    }

    private void receivingLoop() {
        while (true) {
            //System.out.println("broadcast user receivingLoop");
            List<Packet> pkts = fifoBroadcast.deliver();
            //System.out.println("broadcast user after fifo deliver");
            if (pkts!=null && pkts.size()>0) {
                //System.out.println("broadcast user gets finally gets packets!");
                NetworkGlobalInfo.getLogger().appendDeliveryLogs(pkts);
            }
            //System.out.println("broadcast user receivingLoop after perfectLink deliver");
        }
    }

    public void stopBroadcastUserAndFlushLog() {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(0, TimeUnit.SECONDS);
            NetworkGlobalInfo.getLogger().flushToDisk();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
