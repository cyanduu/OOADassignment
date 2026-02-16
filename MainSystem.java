import javax.swing.*;
import java.util.Map;
import java.util.List;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainSystem {
    public static void main(String[] args) {
        // 1. Setup the Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Initialize the Backend
        DatabaseHelper.initializeDatabase();
        ParkingLot lot = ParkingLot.getInstance();
        System.out.println("System: Backend Initialized.");

        // --- STEP A: LOAD PREVIOUS STATE (From your code) ---
        // This restores the cars that were parked when you last closed the app
        List<ParkingSpot> savedSpots = DataManager.loadState();
        if (savedSpots != null && !savedSpots.isEmpty()) {
            lot.setSpots(savedSpots); 
            System.out.println("System: Previous parking data loaded.");
        }

        Map<String, Double> savedFines = DataManager.loadFines();
        if (savedFines != null && !savedFines.isEmpty()) {
            FineManager.setOutstandingFines(savedFines);
            System.out.println("System: Previous fine data loaded.");
        }

        // --- STEP B: ROLE SELECTION (From previous requirement) ---
        String[] options = {"Driver / User", "Administrator"};
        int roleChoice = JOptionPane.showOptionDialog(
            null, 
            "Welcome to Parking Management System.\nPlease select your access mode:", 
            "System Login", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, options, options[0]
        );

        if (roleChoice == -1) System.exit(0); // Exit if user clicks X

        boolean isAdmin = (roleChoice == 1);

        // Security Check for Admin
        if (isAdmin) {
            String password = JOptionPane.showInputDialog(null, "Enter Admin Password:");
            if (password != null && password.equals("CANDU")) {
                JOptionPane.showMessageDialog(null, "Access Granted. Welcome Admin.");
            } else {
                JOptionPane.showMessageDialog(null, "Access Denied!", "Security Alert", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }

        // 3. Create the Main Window
        JFrame frame = new JFrame(isAdmin ? "Parking Lot Management System - ADMIN" : "Parking Lot Management System - DRIVER");
        
        // IMPORTANT: We use DO_NOTHING_ON_CLOSE so we can handle the saving manually below
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        frame.setSize(1100, 750);
        frame.setLocationRelativeTo(null); 

        //Create Tabs based on Role
        JTabbedPane tabs = new JTabbedPane();

        //All User's POV
        tabs.addTab("Entry Station", new EntryPanel());
        tabs.addTab("Exit Station", new ExitPanel());

        //Admin's POV
        if (isAdmin) {
            tabs.addTab("Admin Dashboard", new AdminPanel(lot));
            tabs.addTab("Reports & Analytics", new ReportPanel());
        }

        frame.add(tabs);

        //STEP C: SAVE STATE ON CLOSE
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(frame, 
                    "Are you sure you want to exit?", "Confirm Exit", 
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    System.out.println("System: Saving data before shutting down...");
                    
                    //Save all currently parked cars and fines to file
                    DataManager.saveState(ParkingLot.getInstance().getAllSpots()); 
                    DataManager.saveFines(FineManager.getAllOutstandingFines());
                    
                    System.out.println("System: Data saved. Goodbye!");
                    System.exit(0); 
                }
            }
        });

        frame.setVisible(true);
        System.out.println("System: GUI Launched Successfully!");
    }
}