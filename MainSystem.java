import java.util.List;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class MainSystem {
    public static void main(String[] args) {
        // 1. Setup the Main Window Frame
        try {
            // Make it look like the native OS (Windows/Mac)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Parking Management System (Team Project)");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(1100, 750); // Big enough for tables
        frame.setLocationRelativeTo(null); // Center on screen

        DatabaseHelper.initializeDatabase();

        // 2. Initialize the Backend (Singleton)
        ParkingLot lot = ParkingLot.getInstance();
        System.out.println("System: Backend Initialized.");

        // --- NEW STEP: LOAD PREVIOUS CARS ---
        // Ask DataManager to find the saved file
        List<ParkingSpot> savedSpots = DataManager.loadState();

        // If the file exists and has data, overwrite the empty spots with the saved spots
        if (savedSpots != null && !savedSpots.isEmpty()) {
            lot.setSpots(savedSpots); 
        }

        // 3. Create the Tabbed Interface
        JTabbedPane tabs = new JTabbedPane();

        // --- TAB 1: VEHICLE ENTRY (Member 3) ---
        // This panel lets cars enter and generates tickets
        tabs.addTab("Entry Station", new EntryPanel());

        // --- TAB 2: VEHICLE EXIT (Member 2/3) ---
        // This panel calculates fees using FineManager
        tabs.addTab("Exit Station", new ExitPanel());

        // --- TAB 3: ADMIN DASHBOARD (Member 4) ---
        // This panel shows live stats and revenue
        // Note: passing 'lot' because your AdminPanel constructor requires it
        tabs.addTab("Admin Dashboard", new AdminPanel(lot));

        // --- TAB 4: REPORTS & ANALYTICS (Member 1) ---
        // This panel shows reports and analytics
        tabs.addTab("Reports & Analytics", new ReportPanel());

        // 4. Final Setup
        frame.add(tabs);

        // --- NEW STEP: SAVE CARS WHEN CLOSING ---
        // This listens for the user clicking the "X" button on the window
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("System: Saving data before shutting down...");
                
                // Save all currently parked cars to the .dat file
                DataManager.saveState(lot.getAllSpots()); 
                
                // Now it is safe to completely shut down the app
                System.exit(0); 
            }
        });

        frame.setVisible(true);
        
        System.out.println("System: GUI Launched Successfully!");
    }

}