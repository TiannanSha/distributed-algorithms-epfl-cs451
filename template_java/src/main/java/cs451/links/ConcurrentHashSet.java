package cs451.links;

import java.util.HashSet;

public class ConcurrentHashSet {
    // e.g. store packetId sent by me
    HashSet<Integer> set = new HashSet<>();

    synchronized void add(int item) {
        set.add(item);
    }

    synchronized boolean contains(int item) {
        return set.contains(item);
    }

    synchronized void remove(int item) {
        set.remove(item);
    }

    synchronized int size() {
        return set.size();
    }
}
