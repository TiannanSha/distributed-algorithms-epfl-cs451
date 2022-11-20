package cs451.links;

import cs451.Host;

public interface Link {

    final int BUF_SIZE = 256;  //
    /**
     *
     * @param packet
     * @param host the id of the host. A host is basically a process listening on one socket
     */
    void send(Packet packet, Host host);

    /**
     * blocks until a packet is delivered
     * @return a packet that is received
     */
    Packet deliver();

}
