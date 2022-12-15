package cs451;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host {

    private static final String IP_START_REGEX = "/";

    private int id;
    private String ip;
    private int port = -1;

    public Host() {

    }

    // for testing purpose
    public Host(String idString, String ipString, String portString) {
        populate(idString, ipString, portString);
    }

    public boolean populate(String idString, String ipString, String portString) {
        try {
            id = Integer.parseInt(idString);

            String ipTest = InetAddress.getByName(ipString).toString();
            if (ipTest.startsWith(IP_START_REGEX)) {
                ip = ipTest.substring(1);
            } else {
                ip = InetAddress.getByName(ipTest.split(IP_START_REGEX)[0]).getHostAddress();
            }

            port = Integer.parseInt(portString);
            if (port <= 0) {
                System.err.println("Port in the hosts file must be a positive number!");
                return false;
            }
        } catch (NumberFormatException e) {
            if (port == -1) {
                System.err.println("Id in the hosts file must be a number!");
            } else {
                System.err.println("Port in the hosts file must be a number!");
            }
            return false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return true;
    }

    public short getId() {
        return (short)id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getInetIp() {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "Host{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
