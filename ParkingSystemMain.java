import javax.swing.*;

public class ParkingSystemMain extends JFrame {

    public ParkingSystemMain() {
        // 1. Basic Window Setup
        setTitle("Parking Lot Management System");
        setSize(900, 600); // Width, Height
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers window on screen

        // 2. Create the Tabbed Pane (The navigation bar)
        JTabbedPane tabbedPane = new JTabbedPane();

        // 3. Add your specific panels (Modules)
        // We will create these separate classes in the next steps
        tabbedPane.addTab("Vehicle Entry", new EntryPanel());
        tabbedPane.addTab("Vehicle Exit", new ExitPanel());
        tabbedPane.addTab("Admin Dashboard", new AdminPanel());

        // 4. Add the tabs to the window
        add(tabbedPane);
    }

    public static void main(String[] args) {
        // Run the GUI in a thread-safe manner
        SwingUtilities.invokeLater(() -> {
            new ParkingSystemMain().setVisible(true);
        });
    }
}