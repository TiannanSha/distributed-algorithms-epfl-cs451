package cs451;

import cs451.Broadcast.BroadcastUser;
import cs451.LatticeAgreement.AgreementsExecutor;
import cs451.LatticeAgreement.MultiAgreements;
import cs451.links.LinkUser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {

    private static void handleSignal(LinkUser linkUser) {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
        linkUser.stopLinkUserAndFlushLog();
    }

    private static void initSignalHandlers(LinkUser linkUser) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal(linkUser);
            }
        });
    }

    private static void handleSignal(BroadcastUser broadcastUser) {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
        broadcastUser.stopBroadcastUserAndFlushLog();
    }

    private static void initSignalHandlers(BroadcastUser broadcastUser) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal(broadcastUser);
            }
        });
    }

    private static void handleSignal(AgreementsExecutor agreementsExecutor) {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        agreementsExecutor.stopExecutionNow();

        //write/flush output file if necessary
        System.out.println("Writing output.");
        NetworkGlobalInfo.getLogger().flushToDisk();
    }

    private static void initSignalHandlers(AgreementsExecutor agreementsExecutor) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal(agreementsExecutor);
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");
        // *** my code ***
        Logger logger = new Logger(parser.output());
        ConfigParser configParser = parser.getConfigParser();
        /**
         * for testing perfect link
         */
//        configParser.readPerfectLinkConf();
        /**
         * for testing fifo broadcast
         */
//        configParser.readFifoBroadcastConf();
//
//        Host myhost = parser.hosts().get(parser.myId()-1);
//        NetworkGlobalInfo.init(myhost, parser.hosts(), logger, configParser.getNumMsgsToSend());

        /**
         * for testing perfect link
         */
        // todo comment out this when testing broadcast
//        Host hostToSendTo = parser.hosts().get(configParser.getHostIdToSendTo()-1);
//        LinkUser linkUser = new LinkUser(myhost,
//                configParser.getNumMsgsToSend(),
//                hostToSendTo, logger, parser.hosts());
//        initSignalHandlers(linkUser);
//        System.out.println("Broadcasting and delivering messages...\n");
//        if (myhost.getId() != configParser.getHostIdToSendTo()) {
//            linkUser.sendAllMsgs();
//        }
//        linkUser.startReceivingLoop();
//        linkUser.getExecutorService().shutdown();
//        linkUser.getExecutorService().awaitTermination(30, TimeUnit.MINUTES);


        /**
         * for testing fifo broadcast
         */
//        BroadcastUser broadcastUser = new BroadcastUser();
//        initSignalHandlers(broadcastUser);
//        broadcastUser.broadcastAllMsgs();
//        broadcastUser.startReceivingLoop();
//        broadcastUser.getExecutorService().shutdown();
//        broadcastUser.getExecutorService().awaitTermination(30, TimeUnit.MINUTES);

        /**
         * for agreements. agreements only need host info and logger for NetworkGlobalInfo
         */
        // numMsg is used in m1 m2
        Host myhost = parser.hosts().get(parser.myId()-1);
        NetworkGlobalInfo.init(myhost, parser.hosts(), logger, 0);
        NetworkGlobalInfo.initPerfectLink(); // initialize perfect link which is like a socket used by all classes
        System.out.println(NetworkGlobalInfo.getAllHosts());


        AgreementsExecutor agreementsExecutor = new AgreementsExecutor(parser.config());
        initSignalHandlers(agreementsExecutor);
        try {
            agreementsExecutor.makeProposalsAndDecisions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
