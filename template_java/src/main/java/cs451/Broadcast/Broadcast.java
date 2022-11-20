package cs451.Broadcast;

import cs451.links.Packet;

public interface Broadcast {
    void broadcast(Packet pkt);

    Packet deliver();
}
