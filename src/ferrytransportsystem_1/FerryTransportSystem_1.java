package ferrytransportsystem_1;

import javax.swing.SwingUtilities;

public class FerryTransportSystem_1 {

    public static void main(String[] args) throws InterruptedException {
        // Start the GUI on a safe thread (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            SimulationGUI gui = new SimulationGUI();
            gui.setVisible(true);
            
            Logger.setGUI(gui); // Register the GUI with the log system

            // Run the simulation in the background to prevent UI freezing
            new Thread(() -> {
                try {
                    Logger.log("FERRY TRANSPORT SIMULATION STARTED");
                    Simulation sim = new Simulation();
                    sim.run();
                } catch (InterruptedException e) {
                    Logger.log("Simulation interrupted!");
                }
            }).start();
        });
    }
}
