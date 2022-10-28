package cs451.links;

import cs451.Host;

import java.net.DatagramPacket;

public class StubbornLink implements Link{
    private FairLossLink fairLossLink;

    public StubbornLink(Host myHost) {
        fairLossLink = new FairLossLink(myHost);
    }

    @Override
    public void send(Packet packet, Host host) {
        byte[] buf = packet.marshalPacket();
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, host.getInetIp(), host.getPort());
        while (true) {
            System.out.println("in stubbornLink sending loop");
            fairLossLink.send(datagramPacket, host);
        }
    }



    @Override
    public Packet deliver() {
        return fairLossLink.deliver();
    }
}
