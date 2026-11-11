package ferrytransportsystem_1;
 
public class Logger {
    private static final long startTime = System.currentTimeMillis();
    private static SimulationGUI gui; // GUI reference

    public static void setGUI(SimulationGUI guiInstance) {
        gui = guiInstance;
    }

    public static long now() {
        return System.currentTimeMillis() - startTime;
    }

    public static void log(String message) {
        String formattedMessage = String.format("[Time %4d] %s%n", now(), message);
        
        System.out.print(formattedMessage); // Standard console output
        
        if (gui != null) {
            gui.processLog(formattedMessage); // Update the GUI
        }
    }
}

