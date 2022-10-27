package cs451.links;

import cs451.Host;

public class StubbornLink implements Link{
    private FairLossLink fairLossLink;

    public StubbornLink(Host myHost) {
        fairLossLink = new FairLossLink(myHost);
    }

    @Override
    public void send(Packet packet, Host host) {
        while (true) {
            fairLossLink.send(packet, host);
        }
    }

    @Override
    public Packet deliver() {
        return fairLossLink.deliver();
    }
}
