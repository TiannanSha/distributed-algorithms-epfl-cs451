package cs451.links;

import java.util.HashMap;
import java.util.concurrent.Future;

public class IntToFuture {
    HashMap<Integer, Future<?>> tasksInScheduler = new HashMap<>(64);

    public synchronized void put(int id, Future<?> future) {
        tasksInScheduler.put(id, future);
    }

    /**
     * removes future from the scheduler and remove from this map
     * @param id
     */
    public synchronized void cancelAndRemove(int id) {
        // sometimes id might be plpkt id of a packet that is already deleted. So map.get(id) may be null
        //System.out.println("plpktid = "+id);
        Future<?> future = tasksInScheduler.get(id);
        if (future==null) return;
        future.cancel(true);
        tasksInScheduler.remove(id);
    }

}
