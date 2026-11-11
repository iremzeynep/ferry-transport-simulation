package ferrytransportsystem_1;

import java.util.concurrent.Semaphore;
import java.util.Random;

public class TollBooth {
    
    private final String name;
    
    private final Semaphore boothSemaphore = new Semaphore(2, true);
    
    // Track which booth (0 or 1) is occupied for logging purposes
    private final boolean[] boothOccupied = {false, false};
    private final Random rng = new Random();

    public TollBooth(String side) {
        this.name = side;
    }

    public void pass(String vehicleName) throws InterruptedException {
        // Vehicle enters queue and waits for an empty booth (without busy-waiting)
        boothSemaphore.acquire();

        int boothIdx = -1;
        // Acquire a very short lock (mutex) just to determine the booth ID
        synchronized (boothOccupied) {
            if (!boothOccupied[0]) {
                boothIdx = 0;
                boothOccupied[0] = true;
            } else {
                boothIdx = 1;
                boothOccupied[1] = true;
            }
        }

        try {
            Logger.log(vehicleName + " entered Toll Booth-" + (boothIdx + 1) + " on Side " + name);
            int delay = 100 + rng.nextInt(200); // 100–300 ms
            Thread.sleep(delay);
            Logger.log(vehicleName + " exited  Toll Booth-" + (boothIdx + 1) + " on Side " + name);
        } finally {
            // Release the booth upon exit (for other vehicles)
            synchronized (boothOccupied) {
                boothOccupied[boothIdx] = false;
            }
            // Release the semaphore to let the next waiting vehicle in
            boothSemaphore.release();
        }
    }
}


