package ferrytransportsystem_1;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Ferry implements Runnable {
 
    private static final int FERRY_CAPACITY    = 20;
    private static final long MAX_WAIT_MS      = 2000;  // starvation threshold
    private static final int FAIRNESS_THRESHOLD = 3;    // unit difference to force side switch
    private static final long TRAVEL_TIME_MS   = 800;   // crossing time
    private static final long BOARD_DELAY_MS   = 100;   // per-vehicle boarding delay
 
    private final FerrySystem   system;
    private final Statistics    stats;
    private final CountDownLatch allDone;  // set by Simulation when all vehicles finished
 
    private int  currentSide;  // 0=A, 1=B
 
    public Ferry(FerrySystem system, Statistics stats, CountDownLatch allDone, int startSide) {
        this.system      = system;
        this.stats       = stats;
        this.allDone     = allDone;
        this.currentSide = startSide;
    }
 
    @Override
    public void run() {
        Logger.log("Ferry created — starts on Side " + side(currentSide));
 
        try {
            while (allDone.getCount() > 0) {
                // ── DECIDE NEXT DIRECTION ────────────────────────────────────────
                currentSide = chooseSide();
 
                // ── WAIT FOR VEHICLES TO ARRIVE ──────────────────────────────────
                waitForQueueNotEmpty();
                if (allDone.getCount() == 0) break;
 
                // ── LOADING ──────────────────────────────────────────────────────
                List<Vehicle> loaded = loadVehicles();
                if (loaded.isEmpty()) continue;
 
                // ── DEPART ───────────────────────────────────────────────────────
                int totalLoad = loaded.stream().mapToInt(v -> v.type().capacity).sum();
                Logger.log("Ferry departing from Side " + side(currentSide)
                           + " → Side " + side(1 - currentSide)
                           + " [load=" + totalLoad + "/" + FERRY_CAPACITY
                           + ", vehicles=" + loaded.size() + "]");
 
                stats.recordTrip(totalLoad, FERRY_CAPACITY);
 
                // Register on-board vehicles for crossing sync
                system.registerOnBoard(loaded);
 
                // Simulate crossing
                Thread.sleep(TRAVEL_TIME_MS);
 
                // Switch side
                currentSide = 1 - currentSide;
 
                // ── ARRIVE + UNLOAD ───────────────────────────────────────────
                Logger.log("Ferry arrived  on Side " + side(currentSide)
                           + " — unloading " + loaded.size() + " vehicle(s)");
 
                // Signal all on-board vehicles: ferry has arrived — they can unload
                system.signalArrived();
                
                // Araçların (Vehicle thread'lerinin) inmeyi tamamlamasını güvenli bir şekilde bekle
                system.waitForUnload();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
 
        Logger.log("Ferry thread finished.");
    }
 
    // ── Choose which side to serve ───────────────────────────────────────
 
    private int chooseSide() {
        WaitingArea qA = system.queue(0);
        WaitingArea qB = system.queue(1);
        int unitsA = qA.totalUnitsWaiting();
        int unitsB = qB.totalUnitsWaiting();
 
        // If other side has significantly more units, switch (fairness)
        int other = 1 - currentSide;
        int otherUnits = (other == 0) ? unitsA : unitsB;
        int thisUnits  = (other == 0) ? unitsB : unitsA;
 
        if (otherUnits - thisUnits >= FAIRNESS_THRESHOLD) {
            Logger.log("Ferry switching to Side " + side(other)
                       + " (fairness: other=" + otherUnits + " vs here=" + thisUnits + ")");
            return other;
        }
        return currentSide;
    }
 
    // ── Wait until queue on current side is non-empty ────────────────────
 
    private void waitForQueueNotEmpty() throws InterruptedException {
        WaitingArea q = system.queue(currentSide);
        ReentrantLock lock = q.getLock();
        lock.lock();
        try {
            while (q.isEmpty() && allDone.getCount() > 0) {
                // Check if other side has vehicles → switch
                if (!system.queue(1 - currentSide).isEmpty()) {
                    lock.unlock();
                    currentSide = 1 - currentSide;
                    q = system.queue(currentSide);
                    lock = q.getLock();
                    lock.lock();
                    continue;
                }
                q.getNotEmpty().await(500,TimeUnit.MILLISECONDS);
            }
        } finally {
            lock.unlock();
        }
    }
 
    // ── Load vehicles from current side's queue ───────────────────────────
 
    private List<Vehicle> loadVehicles() throws InterruptedException {
        List<Vehicle> loaded    = new ArrayList<>();
        int           load      = 0;
        WaitingArea   q         = system.queue(currentSide);
        long          loadStart = System.currentTimeMillis();
 
        Logger.log("Ferry loading begins on Side " + side(currentSide));
 
        while (true) {
            // Check departure conditions
            boolean timeLimitReached = (System.currentTimeMillis() - loadStart) >= MAX_WAIT_MS;
 
            Vehicle next = q.peek();
 
            // No more vehicles
            if (next == null) break;
 
            // Next vehicle won't fit
            if (load + next.type().capacity > FERRY_CAPACITY) {
                Logger.log("Ferry cannot fit next vehicle (" + next.name()
                           + ", cap=" + next.type().capacity
                           + ") — remaining=" + (FERRY_CAPACITY - load) + " → departing");
                break;
            }
 
            // Time limit reached (starvation prevention) — depart even if not full
            if (timeLimitReached && !loaded.isEmpty()) {
                Logger.log("Ferry max-wait threshold reached — departing with "
                           + loaded.size() + " vehicle(s)");
                break;
            }
 
            // Board the vehicle
            Vehicle v = q.dequeue();
            load += v.type().capacity;
            loaded.add(v);
            Logger.log("Ferry loaded   " + v.name()
                       + " (cap=" + v.type().capacity
                       + ", current load=" + load + "/" + FERRY_CAPACITY + ")");
            v.recordBoarded();          // stats + log
            system.signalBoarded(v);    // wake vehicle thread
 
            Thread.sleep(BOARD_DELAY_MS); // loading takes a moment
 
            // Full?
            if (load >= FERRY_CAPACITY) {
                Logger.log("Ferry is full (" + load + " units) — departing");
                break;
            }
 
            // Can any remaining vehicle fit?
            if (!q.isEmpty() && !anyRemainingFits(q, FERRY_CAPACITY - load)) {
                Logger.log("No remaining vehicle fits remaining capacity ("
                           + (FERRY_CAPACITY - load) + ") — departing");
                break;
            }
        }
 
        return loaded;
    }
 
    private boolean anyRemainingFits(WaitingArea q, int remainingCap) {
        return q.anyFits(remainingCap);
    }
 
    private static String side(int s) { return s == 0 ? "A" : "B"; }
}