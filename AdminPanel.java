import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminPanel extends JPanel implements ParkingObserver {
    private JLabel lblRevenue;
    private JLabel lblOccupancy;
    private JComboBox<String> schemeSelector; // <--- NEW: The Dropdown
    private ParkingLot lot;

    public AdminPanel() {
        this.lot = ParkingLot.getInstance();
        this.lot.addObserver(this);

        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        // 1. Top Section: Stats
        JPanel statsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        lblRevenue = new JLabel("Total Revenue: RM 0.00", SwingConstants.CENTER);
        lblOccupancy = new JLabel("Live Occupancy: 0 / 20", SwingConstants.CENTER);
        
        // Style the labels
        lblRevenue.setFont(new Font("Arial", Font.BOLD, 20));
        lblOccupancy.setFont(new Font("Arial", Font.BOLD, 20));
        statsPanel.add(lblRevenue);
        statsPanel.add(lblOccupancy);
        add(statsPanel, BorderLayout.CENTER);

        // 2. Bottom Section: Fine Management Control
        JPanel controlsPanel = new JPanel();
        controlsPanel.setBorder(BorderFactory.createTitledBorder("System Configuration"));
        
        controlsPanel.add(new JLabel("Active Fine Scheme:"));
        String[] schemes = {"Fixed", "Hourly", "Progressive"};
        schemeSelector = new JComboBox<>(schemes);
        
        // Add Action Listener to update the backend immediately
        schemeSelector.addActionListener(e -> {
            String selected = (String) schemeSelector.getSelectedItem();
            FineManager.setActiveScheme(selected); // <--- Links to Member 2's code
            JOptionPane.showMessageDialog(this, "Fine Scheme updated to: " + selected);
        });
        
        controlsPanel.add(schemeSelector);
        add(controlsPanel, BorderLayout.SOUTH);
    }

    @Override
    public void update() {
        double revenue = lot.getTotalRevenue();
        int occupied = lot.getOccupiedCount();
        int totalSpots = lot.getSpots().size();

        lblRevenue.setText(String.format("Total Revenue: RM %.2f", revenue));
        lblOccupancy.setText("Live Occupancy: " + occupied + " / " + totalSpots);
    }
}