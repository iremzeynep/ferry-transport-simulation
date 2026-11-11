package ferrytransportsystem_1;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.*;
 
public class FerrySystem {
 
    // ── Infrastructure ──────────────────────────────────────────────────
    private final TollBooth[]   tolls  = new TollBooth[2];
    private final WaitingArea[] queues = new WaitingArea[2];
 
    // ── Per-vehicle boarding synchronization ────────────────────────────
    private final ReentrantLock boardLock     = new ReentrantLock(true);
    // Map from vehicle → its personal condition (created before enqueue)
    private final Map<Vehicle, Condition> boardConditions = new HashMap<>();
    private final Set<Vehicle>            boardedSet      = new HashSet<>();
 
    // ── Crossing synchronization ─────────────────────────────────────────
    // Vehicles on board wait here until ferry arrives
    private final ReentrantLock crossLock     = new ReentrantLock(true);
    private final Condition     crossCond     = crossLock.newCondition();
    private volatile boolean    ferryArrived  = false;
    private final Set<Vehicle>  onBoardSet    = new HashSet<>();
    
    private CountDownLatch unloadLatch;
 
    public FerrySystem() {
        tolls[0]  = new TollBooth("A");
        tolls[1]  = new TollBooth("B");
        queues[0] = new WaitingArea("A");
        queues[1] = new WaitingArea("B");
    }
 
    public TollBooth  toll(int side)  { return tolls[side];  }
    public WaitingArea queue(int side) { return queues[side]; }
 
    // ────────────────────────────────────────────────────────────────────
    //  BOARDING PROTOCOL
    // ────────────────────────────────────────────────────────────────────
 
    public void waitUntilBoarded(Vehicle v, int side) throws InterruptedException {
        boardLock.lock();
        try {
            Condition cond = boardLock.newCondition();
            boardConditions.put(v, cond);
            // Wait until ferry marks us as boarded
            while (!boardedSet.contains(v)) {
                cond.await();
            }
            boardedSet.remove(v);
        } finally {
            boardLock.unlock();
        }
    }
 
    public void signalBoarded(Vehicle v) {
        boardLock.lock();
        try {
            boardedSet.add(v);
            Condition cond = boardConditions.remove(v);
            if (cond != null) cond.signal();
        } finally {
            boardLock.unlock();
        }
    }
 
    // ────────────────────────────────────────────────────────────────────
    //  CROSSING PROTOCOL
    // ────────────────────────────────────────────────────────────────────
 
    public void registerOnBoard(Collection<Vehicle> vehicles) {
        crossLock.lock();
        try {
            onBoardSet.addAll(vehicles);
            ferryArrived = false;
            unloadLatch = new CountDownLatch(vehicles.size());
        } finally {
            crossLock.unlock();
        }
    }
 
    public void waitUntilArrived(Vehicle v) throws InterruptedException {
        crossLock.lock();
        try {
            while (!ferryArrived || onBoardSet.contains(v)) {
                crossCond.await();
            }
        } finally {
            crossLock.unlock();
        }
    }
 
    public void signalArrived() {
        crossLock.lock();
        try {
            ferryArrived = true;
            onBoardSet.clear();
            crossCond.signalAll();
        } finally {
            crossLock.unlock();
        }
    }
    
    public void signalUnloaded() {
        if (unloadLatch != null) {
            unloadLatch.countDown();
        }
    }
    
    public void waitForUnload() throws InterruptedException {
        if (unloadLatch != null) {
            unloadLatch.await();
        }
    }
}