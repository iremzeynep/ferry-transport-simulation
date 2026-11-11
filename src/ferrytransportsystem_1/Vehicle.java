package ferrytransportsystem_1;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Vehicle implements Runnable {
 
    private final String      id;
    private final VehicleType type;
    private final int         startSide;   // 0 = A, 1 = B
    private final FerrySystem system;
    private final Statistics  stats;
    private final CountDownLatch doneLatch;
 
    // For statistics: queue entry time
    private volatile long queueEntryTime;
 
    private final Random rng = new Random();
 
    public Vehicle(String id, VehicleType type, int startSide,
                   FerrySystem system, Statistics stats, CountDownLatch doneLatch) {
        this.id        = id;
        this.type      = type;
        this.startSide = startSide;
        this.system    = system;
        this.stats     = stats;
        this.doneLatch = doneLatch;
    }
 
    public String      name()  { return id; }
    public VehicleType type()  { return type; }
 
    /** Called by the ferry after loading this vehicle (records wait time). */
    public void recordBoarded() {
        long waited = System.currentTimeMillis() - queueEntryTime;
        stats.recordWait(waited);
        Logger.log(id + " boarded ferry (waited " + waited + " ms in queue)");
    }
 
    @Override
    public void run() {
        try {
            Logger.log(id + " created  [type=" + type + ", cap=" + type.capacity
                       + ", startSide=" + (startSide == 0 ? "A" : "B") + "]");
 
            int currentSide = startSide;
 
            // ── Outbound trip ──────────────────────────────────────────
            doTrip(currentSide);
            currentSide = 1 - currentSide;
 
            // ── Wait before return ─────────────────────────────────────
            int rest = 300 + rng.nextInt(700); // 300–1000 ms
            Logger.log(id + " resting   on Side " + side(currentSide)
                       + " for " + rest + " ms before return trip");
            Thread.sleep(rest);
 
            // ── Return trip ────────────────────────────────────────────
            doTrip(currentSide);
 
            Logger.log(id + " completed full round trip — DONE");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log(id + " interrupted!");
        } finally {
            doneLatch.countDown();
        }
    }
 
    private void doTrip(int fromSide) throws InterruptedException {
        Logger.log(id + " starting  trip from Side " + side(fromSide)
                   + " → Side " + side(1 - fromSide));
 
        // 1. Pass toll
        system.toll(fromSide).pass(id);
 
        // 2. Join waiting queue
        queueEntryTime = System.currentTimeMillis();
        system.queue(fromSide).enqueue(this);
 
        // 3. Wait until the ferry loads us (ferry signals us when boarding)
        system.waitUntilBoarded(this, fromSide);
 
        // 4. Wait for ferry to arrive at the other side
        system.waitUntilArrived(this);
 
        // 5. Unload
        Logger.log(id + " unloaded  on Side " + side(1 - fromSide));
    
        // FIX: Vehicle signals the ferry that it has successfully unloaded
        system.signalUnloaded(); 
    }
 
    private static String side(int s) { return s == 0 ? "A" : "B"; }
}
 

