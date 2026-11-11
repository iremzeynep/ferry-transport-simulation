package ferrytransportsystem_1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
 
public class Simulation {
 
    // Vehicle counts per spec
    private static final int NUM_CARS     = 12;
    private static final int NUM_MINIBUSES = 10;
    private static final int NUM_TRUCKS   = 8;
 
    public void run() throws InterruptedException {
        Random rng = new Random();
 
        FerrySystem  system    = new FerrySystem();
        Statistics   stats     = new Statistics();
 
        int totalVehicles = NUM_CARS + NUM_MINIBUSES + NUM_TRUCKS;
        CountDownLatch allDone = new CountDownLatch(totalVehicles);
 
        // ── Create vehicle threads ───────────────────────────────────────
        List<Thread> vehicleThreads = new ArrayList<>();
 
        for (int i = 1; i <= NUM_CARS; i++) {
            Vehicle v = new Vehicle("Car-" + i, VehicleType.CAR,
                                   rng.nextInt(2), system, stats, allDone);
            vehicleThreads.add(new Thread(v, "Car-" + i));
        }
        for (int i = 1; i <= NUM_MINIBUSES; i++) {
            Vehicle v = new Vehicle("Minibus-" + i, VehicleType.MINIBUS,
                                   rng.nextInt(2), system, stats, allDone);
            vehicleThreads.add(new Thread(v, "Minibus-" + i));
        }
        for (int i = 1; i <= NUM_TRUCKS; i++) {
            Vehicle v = new Vehicle("Truck-" + i, VehicleType.TRUCK,
                                   rng.nextInt(2), system, stats, allDone);
            vehicleThreads.add(new Thread(v, "Truck-" + i));
        }
 
        // ── Create ferry thread ──────────────────────────────────────────
        int ferryStart = rng.nextInt(2);
        Ferry ferry = new Ferry(system, stats, allDone, ferryStart);
        Thread ferryThread = new Thread(ferry, "Ferry");
 
        // ── Start everything ─────────────────────────────────────────────
        long simStart = System.currentTimeMillis();
        stats.setSimStartTime(simStart);
 
        ferryThread.start();
        // Stagger vehicle starts slightly to avoid thundering-herd on toll booths
        for (Thread t : vehicleThreads) {
            t.start();
            Thread.sleep(rng.nextInt(80)); // 0–80 ms stagger
        }
 
        // ── Wait for all vehicles to complete ───────────────────────────
        allDone.await();
 
        long simEnd = System.currentTimeMillis();
        stats.setSimEndTime(simEnd);
 
        // Interrupt ferry (it loops on allDone.getCount() > 0, but interrupt is cleaner)
        ferryThread.interrupt();
        ferryThread.join(3000);
 
        // Wait for all vehicle threads to finish
        for (Thread t : vehicleThreads) {
            t.join(3000);
        }
 
        // ── Print statistics ─────────────────────────────────────────────
        stats.print();
    }
}
 


