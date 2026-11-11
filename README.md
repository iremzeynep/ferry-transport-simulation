# Java Concurrent Ferry Transport Simulation

*Note: This simulation was collaboratively developed as an academic group project.*

A multithreaded simulation of a ferry transport system built with Java. This project demonstrates advanced concurrent programming techniques, thread synchronization, and starvation prevention mechanisms while simulating a real-time ferry loading/unloading process across a channel.

## Features & Architecture
* **Concurrency & Synchronization:** Utilizes `ReentrantLock`, `Condition` variables, and `CountDownLatch` for thread-safe boarding and crossing protocols. No busy-waiting is used.
* **Smart Toll Booths:** Regulates vehicle entry using `Semaphore` to ensure fair and realistic delay timings before joining the queue.
* **Fairness & Starvation Prevention:** The ferry intelligently switches sides based on queue load differences and implements a timeout threshold to depart even if not fully loaded, preventing vehicle starvation.
* **Real-time GUI Dashboard:** Built with Java Swing, featuring a live log terminal, dynamic capacity progress bars, and real-time statistics (total trips, average wait times, utilization ratio).

## Project Structure
* `src/ferrytransportsystem_1/` : Contains all the Java source code.
  * **Core Simulation:** `Ferry.java`, `Vehicle.java`, `TollBooth.java`, `WaitingArea.java`
  * **Coordination:** `FerrySystem.java` (Central synchronization hub)
  * **UI & Monitoring:** `SimulationGUI.java`, `Statistics.java`, `Logger.java`

## How to Run
1. Clone this repository to your local machine.
2. Ensure you have the Java Development Kit (JDK 8 or higher) installed.
3. Navigate to the `src` directory and compile the `.java` files.
4. Run the main class:
   ```bash
   java ferrytransportsystem_1.FerryTransportSystem_1
