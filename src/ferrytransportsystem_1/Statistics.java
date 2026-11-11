package ferrytransportsystem_1;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Statistics {
    
private final AtomicInteger tripCount      = new AtomicInteger(0);
    private final AtomicLong   totalWaitTime   = new AtomicLong(0);
    private final AtomicLong   maxWaitTime     = new AtomicLong(0);
    private final AtomicLong   totalFerryLoad  = new AtomicLong(0);
    private final AtomicLong   totalCapacity   = new AtomicLong(0); // cumulative ferry capacity per trip
    private final AtomicInteger vehicleCount   = new AtomicInteger(0);
 
    private long simStartTime;
    private long simEndTime;
 
    public void setSimStartTime(long t) { simStartTime = t; }
    public void setSimEndTime(long t)   { simEndTime   = t; }
 
    public void recordTrip(int loadedUnits, int ferryMaxCap) {
        tripCount.incrementAndGet();
        totalFerryLoad.addAndGet(loadedUnits);
        totalCapacity.addAndGet(ferryMaxCap);
    }
 
    public void recordWait(long waitMs) {
        totalWaitTime.addAndGet(waitMs);
        vehicleCount.incrementAndGet();
        long cur;
        do {
            cur = maxWaitTime.get();
        } while (waitMs > cur && !maxWaitTime.compareAndSet(cur, waitMs));
    }
 
    public void print() {
        long simTime = simEndTime - simStartTime;
        System.out.println("           SIMULATION STATISTICS          ");
        System.out.printf("  Total simulation time    : %d ms%n", simTime);
 
        int vc = vehicleCount.get();
        if (vc > 0) {
            System.out.printf("  Average waiting time      : %.1f ms%n", (double) totalWaitTime.get() / vc);
        }
        System.out.printf("  Maximum waiting time      : %d ms%n", maxWaitTime.get());
        System.out.printf("  Number of ferry trips     : %d%n", tripCount.get());
 
        long cap = totalCapacity.get();
        if (cap > 0) {
            double utilization = 100.0 * totalFerryLoad.get() / cap;
            System.out.printf("  Ferry utilization ratio   : %.1f%%%n", utilization);
        }
    }
}


