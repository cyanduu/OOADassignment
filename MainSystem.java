import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class MainSystem {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatabaseHelper.initializeDatabase();
        ParkingLot lot = ParkingLot.getInstance();
        System.out.println("System: Backend Services Initialized.");

        
        //load Parking Spots (Occupancy & Reservations)
        List<ParkingSpot> savedSpots = DataManager.loadState();
        if (savedSpots != null && !savedSpots.isEmpty()) {
            lot.setSpots(savedSpots);
            System.out.println("System: Previous parking data loaded.");
        }

        List<Transaction> savedHistory = DataManager.loadHistory();
        if (savedHistory != null) {
            lot.setHistory(savedHistory);
            System.out.println("System: Revenue history loaded.");
        }   

        //load Financial Data (Unpaid Fines)
        Map<String, Double> savedFines = DataManager.loadFines();
        if (savedFines != null && !savedFines.isEmpty()) {
            FineManager.setOutstandingFines(savedFines);
            System.out.println("System: Previous fine records loaded.");
        }

        //role selection (Access Control)
        String[] options = {"Driver / User", "Administrator"};
        int roleChoice = JOptionPane.showOptionDialog(
            null, 
            "Welcome to Parking Management System.\nPlease select your access mode:", 
            "System Login", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, options, options[0]
        );

        if (roleChoice == -1) System.exit(0); // Exit if user closes the dialog

        boolean isAdmin = (roleChoice == 1);

        //security Check for Admin Access
        if (isAdmin) {
            String password = JOptionPane.showInputDialog(null, "Enter Admin Password:");
            if (password != null && password.equals("1234")) {
                JOptionPane.showMessageDialog(null, "Access Granted. Welcome Admin.");
            } else {
                JOptionPane.showMessageDialog(null, "Access Denied!", "Security Alert", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }

        JFrame frame = new JFrame(isAdmin ? "Parking Lot Management System - ADMIN" : "Parking Lot Management System - DRIVER");
        
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(1100, 750);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Entry Station", new EntryPanel());
        tabs.addTab("Exit Station", new ExitPanel());

        //Admin-Only Views
        if (isAdmin) {
            tabs.addTab("Admin Dashboard", new AdminPanel(lot));
            tabs.addTab("Reports & Analytics", new ReportPanel()); 
        }

        frame.add(tabs);

        //Data Persistence Strategy (Save on Exit)
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(frame, 
                    "Are you sure you want to exit?", "Confirm Exit", 
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    System.out.println("System: Saving data before shutting down...");
                    
                    //save all currently parked cars and fines to file
                    DataManager.saveState(ParkingLot.getInstance().getSpots());
                    DataManager.saveHistory(ParkingLot.getInstance().getHistory()); 
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