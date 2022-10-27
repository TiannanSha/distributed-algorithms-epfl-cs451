package cs451.links;

import cs451.Host;

import java.io.IOException;
import java.net.*;
import java.util.List;

/**
 * fair loss link, the most basic form of communication channel between process pi and pj
 */

public class FairLossLink implements Link {

    private DatagramSocket socket;
    private Host myHost;

    public FairLossLink(Host myHost)  {
        try {
            socket = new DatagramSocket(myHost.getPort());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.myHost = myHost;
    }

    @Override
    public void send(Packet packet, Host host) {
        byte[] buf = packet.marshalPacket();
        try {
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, host.getInetIp(), host.getPort());
            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Packet deliver()  {
        try {
            byte[] buf = new byte[Link.BUF_SIZE];
            DatagramPacket datagramPacket = new DatagramPacket(buf, Link.BUF_SIZE);
            socket.receive(datagramPacket);
            return Packet.unmarshalPacket(datagramPacket.getData());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
