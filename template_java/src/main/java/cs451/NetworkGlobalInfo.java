package cs451;

import cs451.Host;
import cs451.links.Link;
import cs451.links.PerfectLink;

import java.util.LinkedList;
import java.util.List;

/**
 * this class manages info of the global network
 */

public class NetworkGlobalInfo {
    static Host my;
    static List<Host> hosts;
    static List<Host> otherHosts = new LinkedList<>();
    static Logger logger;
    static int numMsgsToSend;
    /**
     * every class share one socket
     */
    public static PerfectLink perfectLink;

    public static void init(Host myHost, List<Host> allHosts, Logger globalLogger, int numMsg) {
        hosts = allHosts;
        my = myHost;
        for (Host host:hosts) {
            if (host.getId()!= myHost.getId()) {
                otherHosts.add(host);
            }
        }
        logger = globalLogger;
        numMsgsToSend = numMsg;
    }

    public static List<Host> getAllHosts() {
        return hosts;
    }

    public static Host getMyHost() {
        return my;
    }

    public static List<Host> getOtherHosts() {
        return otherHosts;
    }

    public static Logger getLogger() {
        return logger;
    }

    /**
     *
     * @param hostId smallest hostid is 1
     * @return
     */
    public static Host getHostById(int hostId) {
        return hosts.get(hostId-1);
    }

    public static int getNumMsgsToSend() {
        return numMsgsToSend;
    }

    public static int getMajorityThreshold() {
        return hosts.size()/2+1;
    }

    public static void initPerfectLink() {
        perfectLink = new PerfectLink();
    }
}
