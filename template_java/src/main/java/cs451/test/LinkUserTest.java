package cs451.test;

import cs451.Host;
import cs451.LatticeAgreement.Messages.ProposalACKMsg;
import cs451.LatticeAgreement.Messages.ProposalMsg;
import cs451.LatticeAgreement.Messages.ProposalNACKMsg;
import cs451.Logger;
import cs451.NetworkGlobalInfo;
import cs451.links.LinkUser;
import cs451.links.Packet;
import cs451.links.PacketSerializer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LinkUserTest {
//    String outputFile = "/Users/sha/Desktop/EPFL/2022-2023fall/distributed-algorithms/CS451-2022-project/example/output/1.output";
//    // todo do we just append to output file or need to clear??
//    Host myHost = new Host("2", "127.0.0.1", "11002");
//    Host host1 = new Host("1", "127.0.0.1", "11001");
//    Host host2 = myHost;
//    Host host3 = new Host("1", "127.0.0.1", "11003");
//    List<Host> hosts = new ArrayList<>();
//
//    Logger logger;
//    LinkUser linkUser;

//    @Before
//    public void init() {
//        System.out.println("init()");
//        hosts.add(host1);
//        hosts.add(host2);
//        hosts.add(host3);
//        logger = new Logger(outputFile);
//        linkUser = new LinkUser(myHost, 10, myHost, logger, hosts);
//    }

//    @Test
//    public void testLinkUser() {
////        linkUser.sendMsgs(1,10);
////        linkUser.getPerfectLink().waitTillAllTaskFinished();
////        try {
////            linkUser.getExecutorService().awaitTermination(10, TimeUnit.SECONDS);
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
//        linkUser.startReceivingLoop();
//        linkUser.getExecutorService().shutdown();
//        try {
//            linkUser.getExecutorService().awaitTermination(30, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testLogger() {
//        linkUser.getLogger().appendBroadcastLogs(1,8);
//        linkUser.getLogger().appendBroadcastLogs(9,10);
//        linkUser.getLogger().appendDeliveryLogs((short)2, 9,16);
//        linkUser.stopLinkUserAndFlushLog();
//    }

    @Test
    public void testSerializePropsalACKMsg() {
        ProposalACKMsg proposalACKMsg = new ProposalACKMsg(3);
        byte[] data = proposalACKMsg.serialize();
        short myHostID = 0;
        Packet pkt1 = new Packet(data, data.length, myHostID,myHostID ,myHostID, Packet.PROPOSE_ACK_MSG, 0 );
        //Packet pkt2 = new Packet();
        byte[] pkt1AsBytes = PacketSerializer.serialize(pkt1);
        Packet pkt1Recv = PacketSerializer.deserializePacket(pkt1AsBytes);
        ProposalACKMsg proposalACKRecv = ProposalACKMsg.deserialize(pkt1Recv.getData());
        System.out.println(proposalACKRecv);
    }

    @Test
    public void testSerializePropsalNACKMsg() {
        HashSet<Integer> acceptedValues = new HashSet<>();
        acceptedValues.add(1);
        acceptedValues.add(11);
        acceptedValues.add(111);
        ProposalNACKMsg proposalNACKMsg = new ProposalNACKMsg(3, acceptedValues);
        byte[] data = proposalNACKMsg.serialize();
        short myHostID = 0;
        Packet pkt1 = new Packet(data, data.length, myHostID,myHostID ,myHostID, Packet.PROPOSE_ACK_MSG, 0 );
        byte[] pkt1AsBytes = PacketSerializer.serialize(pkt1);
        Packet pkt1Recv = PacketSerializer.deserializePacket(pkt1AsBytes);
        ProposalNACKMsg proposalNACKRecv = ProposalNACKMsg.deserialize(pkt1Recv.getData());
        System.out.println(proposalNACKRecv);
    }

    @Test
    public void testSerializePropsalMsg() {
        HashSet<Integer> proposal = new HashSet<>();
        for (int i=0; i<300; i++) {
            proposal.add(i);
        }
        HashSet<Integer> proposedValues = new HashSet<>();
        for (int i=0; i<300; i++) {
            proposedValues.add(i);
        }
        ProposalMsg proposalMsg = new ProposalMsg(proposal, proposedValues,2);
        byte[] data = proposalMsg.serialize();
        short myHostID = 0;
        Packet pkt1 = new Packet(data, data.length, myHostID,myHostID ,myHostID, Packet.PROPOSE_ACK_MSG, 3);
        byte[] pkt1AsBytes = PacketSerializer.serialize(pkt1);
        Packet pkt1Recv = Packet.unmarshalPacket(pkt1AsBytes);
        ProposalMsg proposalMsgRecv = ProposalMsg.deserialize(pkt1Recv.getData());
        System.out.println(proposalMsgRecv);
    }
}
