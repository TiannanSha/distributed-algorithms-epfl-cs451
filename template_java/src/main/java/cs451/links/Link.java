package cs451.links;

import cs451.Host;

public interface Link {

    final int BUF_SIZE = 512;  //
    /**
     *
     * @param packet
     * @param hostId the id of the host. A host is basically a process listening on one socket
     */
    void send(Packet packet, Host host);

    Packet deliver();

}
