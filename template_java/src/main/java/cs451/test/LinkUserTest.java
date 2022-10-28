package cs451.test;

import cs451.Host;
import cs451.Logger;
import cs451.links.LinkUser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LinkUserTest {
    String outputFile = "/Users/sha/Desktop/EPFL/2022-2023fall/distributed-algorithms/CS451-2022-project/example/output/1.output";
    // todo do we just append to output file or need to clear??
    Host myHost = new Host("1", "127.0.0.1", "11001");
    List<Host> hosts = new ArrayList<>();
    Logger logger = new Logger(outputFile);
    LinkUser linkUser = new LinkUser(myHost, 10, myHost, logger, hosts);

    @Test
    public void testLinkUser() {
        linkUser.sendMsgs(1,10);
        linkUser.getPerfectLink().waitTillAllTaskFinished();
        try {
            linkUser.getExecutorService().awaitTermination(10, TimeUnit.SECONDS);
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
