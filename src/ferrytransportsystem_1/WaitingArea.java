package ferrytransportsystem_1;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
 
/**
 * FIFO waiting area for one side.
 * Vehicles join the tail and are served from the head — no overtaking.
 */
public class WaitingArea {
    private final String side;
    private final Queue<Vehicle> queue = new LinkedList<>();
    private final ReentrantLock lock   = new ReentrantLock(true); // fair
    private final Condition notEmpty   = lock.newCondition();
 
    public WaitingArea(String side) {
        this.side = side;
    }
 
    /** Vehicle joins the FIFO queue. */
    public void enqueue(Vehicle v) {
        lock.lock();
        try {
            queue.add(v);
            Logger.log(v.name() + " joined  waiting queue on Side " + side
                       + " [queue size=" + queue.size() + "]");
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }
 
    /** Peek at the head without removing. */
    public Vehicle peek() {
        lock.lock();
        try {
            return queue.peek();
        } finally {
            lock.unlock();
        }
    }
 
    /** Remove the head vehicle (called by ferry during loading). */
    public Vehicle dequeue() {
        lock.lock();
        try {
            return queue.poll();
        } finally {
            lock.unlock();
        }
    }
 
    /** Return snapshot of total capacity units waiting. */
    public int totalUnitsWaiting() {
        lock.lock();
        try {
            int sum = 0;
            for (Vehicle v : queue) sum += v.type().capacity;
            return sum;
        } finally {
            lock.unlock();
        }
    }
 
    public int size() {
        lock.lock();
        try { return queue.size(); }
        finally { lock.unlock(); }
    }
 
    public boolean isEmpty() {
        lock.lock();
        try { return queue.isEmpty(); }
        finally { lock.unlock(); }
    }
 
    public boolean anyFits(int remainingCap) {
        lock.lock();
        try {
            for (Vehicle v : queue) {
                if (v.type().capacity <= remainingCap) return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
 
 
    public Condition    getNotEmpty()   { return notEmpty; }

    public ReentrantLock getLock() {
    return this.lock;
}
}
 

