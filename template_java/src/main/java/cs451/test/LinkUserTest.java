package cs451.test;

import cs451.Host;
import cs451.Logger;
import cs451.links.LinkUser;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LinkUserTest {
    String outputFile = "/Users/sha/Desktop/EPFL/2022-2023fall/distributed-algorithms/CS451-2022-project/example/output/1.output";
    // todo do we just append to output file or need to clear??
    Host myHost = new Host("2", "127.0.0.1", "11002");
    Host host1 = new Host("1", "127.0.0.1", "11001");
    Host host2 = myHost;
    Host host3 = new Host("1", "127.0.0.1", "11003");
    List<Host> hosts = new ArrayList<>();

    Logger logger;
    LinkUser linkUser;

    @Before
    public void init() {
        System.out.println("init()");
        hosts.add(host1);
        hosts.add(host2);
        hosts.add(host3);
        logger = new Logger(outputFile);
        linkUser = new LinkUser(myHost, 10, myHost, logger, hosts);
    }

    @Test
    public void testLinkUser() {
//        linkUser.sendMsgs(1,10);
//        linkUser.getPerfectLink().waitTillAllTaskFinished();
//        try {
//            linkUser.getExecutorService().awaitTermination(10, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        linkUser.startReceivingLoop();
        linkUser.getExecutorService().shutdown();
        try {
            linkUser.getExecutorService().awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLogger() {
        linkUser.getLogger().appendBroadcastLogs(1,8);
        linkUser.getLogger().appendBroadcastLogs(9,10);
        linkUser.getLogger().appendDeliveryLogs((short)2, 9,16);
        linkUser.stopLinkUserAndFlushLog();
    }
}
