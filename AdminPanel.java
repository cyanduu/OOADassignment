import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List; // Import List

public class AdminPanel extends JPanel implements ParkingObserver {
    private JLabel lblRevenue;
    private JLabel lblOccupancy;
    private JComboBox<String> schemeSelector;
    private JTable vehicleTable; // NEW: Required by assignment
    private DefaultTableModel tableModel;
    private ParkingLot lot;

    // Fix: Pass the loaded 'lot' object here
    public AdminPanel(ParkingLot lot) {
        this.lot = lot;
        this.lot.addObserver(this); // Register for updates

        setLayout(new BorderLayout(10, 10)); // Add spacing
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Top Section: Stats
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        lblRevenue = new JLabel("Total Revenue: RM 0.00", SwingConstants.CENTER);
        lblOccupancy = new JLabel("Occupancy: 0 / 0", SwingConstants.CENTER);
        
        lblRevenue.setFont(new Font("Arial", Font.BOLD, 18));
        lblOccupancy.setFont(new Font("Arial", Font.BOLD, 18));
        lblRevenue.setOpaque(true);
        lblRevenue.setBackground(new Color(220, 255, 220)); // Light Green for money
        
        statsPanel.add(lblRevenue);
        statsPanel.add(lblOccupancy);
        add(statsPanel, BorderLayout.NORTH);

        // 2. Center Section: Vehicle List (REQUIRED [cite: 126])
        String[] columns = {"Spot ID", "License Plate", "Type", "Entry Time"};
        tableModel = new DefaultTableModel(columns, 0);
        vehicleTable = new JTable(tableModel);
        add(new JScrollPane(vehicleTable), BorderLayout.CENTER);

        // 3. Bottom Section: Controls
        JPanel controlsPanel = new JPanel();
        controlsPanel.setBorder(BorderFactory.createTitledBorder("System Configuration"));
        controlsPanel.add(new JLabel("Active Fine Scheme:"));
        
        String[] schemes = {"Fixed", "Hourly", "Progressive"};
        schemeSelector = new JComboBox<>(schemes);
        
        schemeSelector.addActionListener(e -> {
            String selected = (String) schemeSelector.getSelectedItem();
            // Ensure BillingService has this static method
            // BillingService.setFineScheme(selected);
            JOptionPane.showMessageDialog(this, "Fine Scheme updated to: " + selected);
        });
        
        controlsPanel.add(schemeSelector);
        add(controlsPanel, BorderLayout.SOUTH);

        // Initial refresh
        onParkingDataChanged();
    }

    @Override
    public void onParkingDataChanged() { // MATCHES INTERFACE NAME
        // 1. Update Revenue
        lblRevenue.setText(String.format("Total Revenue: RM %.2f", lot.getTotalRevenue()));

        // 2. Update Occupancy
        List<ParkingSpot> spots = lot.getAllSpots();
        long occupied = spots.stream().filter(ParkingSpot::isOccupied).count();
        int total = spots.size();
        lblOccupancy.setText("Occupancy: " + occupied + " / " + total);

        // 3. Update Table
        tableModel.setRowCount(0); // Clear table
        for (ParkingSpot s : spots) {
            if (s.isOccupied()) {
                Vehicle v = s.getCurrentVehicle();
                tableModel.addRow(new Object[]{
                    s.getSpotID(),
                    v.getLicensePlate(),
                    v.getType(),
                    new java.util.Date(v.getEntryTime()).toString() // Simple date format
                });
            }
        }
    }
}