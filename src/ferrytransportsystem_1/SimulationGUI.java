package ferrytransportsystem_1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class SimulationGUI extends JFrame {

    // Figma / Tailwind Color Palette
    private final Color BG_SLATE_950 = new Color(2, 6, 23);
    private final Color BG_SLATE_900 = new Color(15, 23, 42);
    private final Color BG_SLATE_800 = new Color(30, 41, 59);
    private final Color BORDER_COLOR = new Color(51, 65, 85);
    private final Color TEXT_WHITE = new Color(248, 250, 252);
    private final Color TEXT_MUTED = new Color(148, 163, 184);
    private final Color ACCENT_BLUE = new Color(59, 130, 246);
    private final Color ACCENT_EMERALD = new Color(16, 185, 129);
    private final Color ACCENT_AMBER = new Color(245, 158, 11);

    // UI Components
    private JTextArea logTerminal;
    private JLabel lblFerryStatus;
    private JProgressBar ferryCapacityBar;
    private JLabel lblQueueA, lblQueueB;
    private JLabel lblTrips;

    public SimulationGUI() {
        setTitle("Ferry Transport System - Real-time Dashboard");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(BG_SLATE_950);
        
        // Margins
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. HEADER SECTION: Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_SLATE_950);
        JLabel title = new JLabel("Ferry Transport System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);
        JLabel subtitle = new JLabel("Real-time Simulation Dashboard");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        
        JPanel titleGroup = new JPanel(new GridLayout(2, 1));
        titleGroup.setBackground(BG_SLATE_950);
        titleGroup.add(title);
        titleGroup.add(subtitle);
        headerPanel.add(titleGroup, BorderLayout.WEST);

        // 2. CENTER SECTION: Statistics and Visualizer (GridBag/Border Mix)
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setBackground(BG_SLATE_950);

        // -- Statistic Cards (Equivalent to StatCard.tsx)
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(BG_SLATE_950);
        
        lblTrips = new JLabel("0");
        statsPanel.add(createStatCard("Total Ferry Trips", lblTrips, ACCENT_BLUE));
        statsPanel.add(createStatCard("System Status", new JLabel("ACTIVE"), ACCENT_EMERALD));
        
        centerPanel.add(statsPanel, BorderLayout.NORTH);

        // -- Live Simulation (Equivalent to SimulationVisualizer.tsx)
        JPanel visualizerPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        visualizerPanel.setBackground(BG_SLATE_950);

        // Terminal A
        lblQueueA = new JLabel("0 vehicles");
        visualizerPanel.add(createTerminalPanel("Terminal Side A", lblQueueA));

        // Water Channel and Ferry (Equivalent to Ferry.tsx)
        visualizerPanel.add(createFerryPanel());

        // Terminal B
        lblQueueB = new JLabel("0 vehicles");
        visualizerPanel.add(createTerminalPanel("Terminal Side B", lblQueueB));

        centerPanel.add(visualizerPanel, BorderLayout.CENTER);

        // 3. BOTTOM SECTION: Log Terminal (Equivalent to EventLog.tsx)
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(BG_SLATE_900);
        logPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel logTitle = new JLabel("  > System Logs");
        logTitle.setForeground(ACCENT_BLUE);
        logTitle.setFont(new Font("Consolas", Font.BOLD, 14));
        logPanel.add(logTitle, BorderLayout.NORTH);

        logTerminal = new JTextArea();
        logTerminal.setBackground(BG_SLATE_900);
        logTerminal.setForeground(TEXT_MUTED);
        logTerminal.setFont(new Font("Consolas", Font.PLAIN, 13));
        logTerminal.setEditable(false);
        logTerminal.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logTerminal);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(1000, 250));
        logPanel.add(scrollPane, BorderLayout.CENTER);

        // Adding to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);
    }

    // --- CUSTOM COMPONENT BUILDERS ---

    private JPanel createStatCard(String title, JLabel valueLabel, Color iconColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(BG_SLATE_800);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(TEXT_MUTED);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        valueLabel.setForeground(TEXT_WHITE);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTerminalPanel(String title, JLabel queueCountLabel) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(BG_SLATE_800);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(TEXT_WHITE);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        queueCountLabel.setForeground(ACCENT_AMBER);
        queueCountLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        
        JPanel center = new JPanel(new GridLayout(2, 1));
        center.setBackground(BG_SLATE_800);
        JLabel waitLbl = new JLabel("Waiting Queue:");
        waitLbl.setForeground(TEXT_MUTED);
        center.add(waitLbl);
        center.add(queueCountLabel);

        pnl.add(titleLbl, BorderLayout.NORTH);
        pnl.add(center, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createFerryPanel() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBackground(new Color(30, 58, 138)); // Dark blue water effect (Blue 900)
        pnl.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT_BLUE, 2, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        lblFerryStatus = new JLabel("Docked at Side A", SwingConstants.CENTER);
        lblFerryStatus.setForeground(TEXT_WHITE);
        lblFerryStatus.setFont(new Font("Segoe UI", Font.BOLD, 16));

        ferryCapacityBar = new JProgressBar(0, 20); // Max capacity 20
        ferryCapacityBar.setValue(0);
        ferryCapacityBar.setStringPainted(true);
        ferryCapacityBar.setForeground(ACCENT_EMERALD);
        ferryCapacityBar.setBackground(BG_SLATE_900);
        ferryCapacityBar.setBorderPainted(false);
        ferryCapacityBar.setPreferredSize(new Dimension(100, 30));

        JLabel capLbl = new JLabel("Ferry Capacity Load", SwingConstants.CENTER);
        capLbl.setForeground(TEXT_MUTED);

        JPanel center = new JPanel(new GridLayout(2,1, 0, 5));
        center.setOpaque(false);
        center.add(capLbl);
        center.add(ferryCapacityBar);

        pnl.add(lblFerryStatus, BorderLayout.NORTH);
        pnl.add(center, BorderLayout.CENTER);
        return pnl;
    }

    // --- LOG AND UI UPDATE METHOD ---
    
    public void processLog(String message) {
        SwingUtilities.invokeLater(() -> {
            // 1. Print Log to Screen
            logTerminal.append(message);
            logTerminal.setCaretPosition(logTerminal.getDocument().getLength());

            // 2. Dynamically Update UI by Parsing Log Text (The Magic Part!)
            
            // Ferry Load Update
            if (message.contains("current load=")) {
                try {
                    String part = message.split("current load=")[1].split("/")[0];
                    ferryCapacityBar.setValue(Integer.parseInt(part.trim()));
                } catch (Exception e) {}
            }
            
            // Ferry Movement Status
            if (message.contains("departing from Side A")) {
                lblFerryStatus.setText("Moving: A → B");
                lblFerryStatus.setForeground(ACCENT_AMBER);
            } else if (message.contains("departing from Side B")) {
                lblFerryStatus.setText("Moving: B → A");
                lblFerryStatus.setForeground(ACCENT_AMBER);
            } else if (message.contains("arrived  on Side A")) {
                lblFerryStatus.setText("Docked at Side A");
                lblFerryStatus.setForeground(ACCENT_EMERALD);
                ferryCapacityBar.setValue(0); // Empties upon unloading
            } else if (message.contains("arrived  on Side B")) {
                lblFerryStatus.setText("Docked at Side B");
                lblFerryStatus.setForeground(ACCENT_EMERALD);
                ferryCapacityBar.setValue(0);
                
                // Every arrival at B counts as one round trip
                int trips = Integer.parseInt(lblTrips.getText()) + 1;
                lblTrips.setText(String.valueOf(trips));
            }
            
            // Queue Updates
            if (message.contains("queue size=")) {
                try {
                    String size = message.split("queue size=")[1].split("]")[0];
                    if (message.contains("Side A")) lblQueueA.setText(size + " vehicles");
                    if (message.contains("Side B")) lblQueueB.setText(size + " vehicles");
                } catch (Exception e) {}
            }
        });
    }
}