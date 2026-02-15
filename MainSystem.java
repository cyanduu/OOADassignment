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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 750); // Big enough for tables
        frame.setLocationRelativeTo(null); // Center on screen

        DatabaseHelper.initializeDatabase();

        // 2. Initialize the Backend (Singleton)
        ParkingLot lot = ParkingLot.getInstance();
        System.out.println("System: Backend Initialized.");

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
        frame.setVisible(true);
        
        System.out.println("System: GUI Launched Successfully!");
    }

}