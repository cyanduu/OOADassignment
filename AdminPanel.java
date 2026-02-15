import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel implements ParkingObserver {
    private JLabel lblRevenue;
    private JLabel lblOccupancy;
    private JComboBox<String> schemeSelector;
    private JTable vehicleTable;
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

        // 2. Center Section: Vehicle List
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
            JOptionPane.showMessageDialog(this, "Fine Scheme updated to: " + selected);
        });
        
        controlsPanel.add(schemeSelector);
        add(controlsPanel, BorderLayout.SOUTH);

        // Inside AdminPanel.java constructor

// --- NEW SECTION: SQL Database Registration ---
JPanel sqlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
sqlPanel.setBorder(BorderFactory.createTitledBorder("Register Handicapped Permit (SQL)"));

JTextField txtPlateRegister = new JTextField(10);
JButton btnRegister = new JButton("Save to Database");

btnRegister.addActionListener(e -> {
    String plate = txtPlateRegister.getText();
    if (plate.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a plate number.");
        return;
    }
    
    // Call the SQL Insert method
    boolean success = DatabaseHelper.registerHandicappedPlate(plate);
    if (success) {
        JOptionPane.showMessageDialog(this, "Plate " + plate.toUpperCase() + " registered successfully!");
        txtPlateRegister.setText("");
    } else {
        JOptionPane.showMessageDialog(this, "Error: Plate already exists or database error.");
    }
});

sqlPanel.add(new JLabel("License Plate:"));
sqlPanel.add(txtPlateRegister);
sqlPanel.add(btnRegister);

// Add the sqlPanel to your controlsPanel or main layout
controlsPanel.add(sqlPanel);

// --- NEW SECTION: SQL Reserved Registration ---
JPanel reservedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
reservedPanel.setBorder(BorderFactory.createTitledBorder("Register VIP Reserved Plate (SQL)"));

JTextField txtReservedPlate = new JTextField(10);
JButton btnRegisterReserved = new JButton("Save VIP Plate");

btnRegisterReserved.addActionListener(e -> {
    String plate = txtReservedPlate.getText();
    if (plate.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a plate number.");
        return;
    }
    
    boolean success = DatabaseHelper.registerReservedPlate(plate);
    if (success) {
        JOptionPane.showMessageDialog(this, "VIP Plate " + plate.toUpperCase() + " registered successfully!");
        txtReservedPlate.setText("");
    } else {
        JOptionPane.showMessageDialog(this, "Error: Plate already exists or database error.");
    }
});

reservedPanel.add(new JLabel("License Plate:"));
reservedPanel.add(txtReservedPlate);
reservedPanel.add(btnRegisterReserved);

// Don't forget to add this to your layout!
// If you have a main 'controlsPanel', add it there:
controlsPanel.add(reservedPanel);

        // Initial refresh
        onParkingDataChanged();
    }

    @Override
    public void onParkingDataChanged() {
        // 1. Update Revenue
        // Ensure ParkingLot.java has this method!
        if (lot != null) {
             lblRevenue.setText(String.format("Total Revenue: RM %.2f", lot.getTotalRevenue()));
        }

        // 2. Update Occupancy
        // FIX 1: Changed 'getAllSpots()' to 'getSpots()' to match ParkingLot.java
        List<ParkingSpot> spots = lot.getSpots(); 
        
        long occupied = 0;
        if (spots != null) {
            occupied = spots.stream().filter(ParkingSpot::isOccupied).count();
            int total = spots.size();
            lblOccupancy.setText("Occupancy: " + occupied + " / " + total);

            // 3. Update Table
            tableModel.setRowCount(0); // Clear table
            for (ParkingSpot s : spots) {
                if (s.isOccupied()) {
                    // FIX 2: Changed 'getCurrentVehicle()' to 'getVehicle()' 
                    // (Unless you explicitly renamed it in ParkingSpot.java)
                    Vehicle v = s.getVehicle(); 
                    
                    if (v != null) {
                        tableModel.addRow(new Object[]{
                            s.getSpotID(),
                            v.getLicensePlate(),
                            v.getType(),
                            new java.util.Date(v.getEntryTime()).toString()
                        });
                    }
                }
            }
        }
    }
}