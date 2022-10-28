package cs451;

import cs451.links.LinkUser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

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

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        // example
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
        configParser.readPerfectLinkConf();
        Host myhost = parser.hosts().get(parser.myId()-1);
        Host hostToSendTo = parser.hosts().get(configParser.getHostIdToSendTo()-1);
        LinkUser linkUser = new LinkUser(myhost,
                configParser.getNumMsgsToSend(),
                hostToSendTo, logger, parser.hosts());
        initSignalHandlers(linkUser);

        System.out.println("Broadcasting and delivering messages...\n");
        // *** my code ***
        if (myhost.getId() != configParser.getHostIdToSendTo()) {
            linkUser.sendAllMsgs();
        }
        linkUser.startReceivingLoop();

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
