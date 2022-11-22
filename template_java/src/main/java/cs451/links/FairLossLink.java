package cs451.links;

import cs451.Host;
import cs451.NetworkGlobalInfo;

import java.io.IOException;
import java.net.*;
import java.sql.SQLOutput;
import java.util.List;

/**
 * fair loss link, the most basic form of communication channel between process pi and pj
 */

public class FairLossLink implements Link {

    private DatagramSocket socket;
    //private Host myHost;

    public FairLossLink()  {
        try {
            socket = new DatagramSocket(NetworkGlobalInfo.getMyHost().getPort());
            //socket.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        //this.myHost = myHost;
    }

    @Override
    public void send(Packet packet, Host host) {

        byte[] buf = packet.marshalPacket();
        try {
            System.out.println("fairloss link send ACK to host " + host.getInetIp() + "port " + host.getPort());
            System.out.println("packet.src = " + packet.src + ", packet.pktId = "+packet.pktId);
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, host.getInetIp(), host.getPort());
            socket.send(datagramPacket);
            System.out.println("fairloss link after send ACK");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(DatagramPacket datagramPacket, Host host) {
        try {
            System.out.println("in fairloss link send()");
            System.out.println("send to addr:" + datagramPacket.getAddress() + ", port:" + datagramPacket.getPort());
            socket.send(datagramPacket);
        } catch (IOException e) {
            System.out.println("exception after socket.send() in fairloss link");
            e.printStackTrace();
        }
    }

    @Override
    /**
     * blocks until a packet is delivered
     */
    public Packet deliver()  {
        try {
            byte[] buf = new byte[Link.BUF_SIZE];
            DatagramPacket datagramPacket = new DatagramPacket(buf, Link.BUF_SIZE);
            System.out.println("before socket receive, which blocks if no packet is received");
            socket.receive(datagramPacket); // this blocks
            System.out.println("fair loss link deliver() after socket.receive()");
            return Packet.unmarshalPacket(datagramPacket.getData());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
