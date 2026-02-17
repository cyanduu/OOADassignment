import java.awt.*;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminPanel extends JPanel implements ParkingObserver {
    private JLabel lblRevenue;
    private JLabel lblOccupancy;
    private JComboBox<String> schemeSelector;
    private JTable vehicleTable;
    private DefaultTableModel tableModel;
    private ParkingLot lot;

    public AdminPanel(ParkingLot lot) {
        this.lot = lot;
        this.lot.addObserver(this); // Register as an observer to receive real-time updates

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        
        lblRevenue = new JLabel("Total Revenue: RM 0.00", SwingConstants.CENTER);
        lblOccupancy = new JLabel("Occupancy: 0 / 0", SwingConstants.CENTER);
        
        lblRevenue.setFont(new Font("Arial", Font.BOLD, 18));
        lblRevenue.setOpaque(true);
        lblRevenue.setBackground(new Color(220, 255, 220)); // Light Green background
        
        lblOccupancy.setFont(new Font("Arial", Font.BOLD, 18));
        
        statsPanel.add(lblRevenue);
        statsPanel.add(lblOccupancy);
        add(statsPanel, BorderLayout.NORTH);

        String[] columns = {"Spot ID", "License Plate", "Type", "Entry Time"};
        tableModel = new DefaultTableModel(columns, 0);
        vehicleTable = new JTable(tableModel);
        add(new JScrollPane(vehicleTable), BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        controlsPanel.setBorder(BorderFactory.createTitledBorder("System Configuration & Database"));

        //1. Fine Scheme Selector
        JPanel schemePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        schemePanel.add(new JLabel("Active Fine Scheme:"));
        String[] schemes = {"Fixed", "Hourly", "Progressive"};
        schemeSelector = new JComboBox<>(schemes);
        schemeSelector.addActionListener(e -> {
            String selected = (String) schemeSelector.getSelectedItem();
            JOptionPane.showMessageDialog(this, "Fine Scheme updated to: " + selected);
        });
        schemePanel.add(schemeSelector);
        controlsPanel.add(schemePanel);

        //2. Register Handicapped Permit (db)
        JPanel sqlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sqlPanel.add(new JLabel("Register Handicapped Permit (SQL):"));
        JTextField txtPlateRegister = new JTextField(10);
        JButton btnRegister = new JButton("Save to DB");

        btnRegister.addActionListener(e -> {
            String plate = txtPlateRegister.getText().trim();
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a plate number.");
                return;
            }
            
            boolean success = DatabaseHelper.registerHandicappedPlate(plate);
            if (success) {
                JOptionPane.showMessageDialog(this, "Plate " + plate.toUpperCase() + " registered as Handicapped.");
                txtPlateRegister.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Error: Plate already exists or DB error.");
            }
        });

        sqlPanel.add(txtPlateRegister);
        sqlPanel.add(btnRegister);
        controlsPanel.add(sqlPanel);

        //3. Register VIP Reserved Permit (db)
        JPanel reservedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reservedPanel.add(new JLabel("Register VIP Reserved Plate (SQL):"));
        JTextField txtReservedPlate = new JTextField(10);
        JButton btnRegisterReserved = new JButton("Save VIP");

        btnRegisterReserved.addActionListener(e -> {
            String plate = txtReservedPlate.getText().trim();
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a plate number.");
                return;
            }
            
            boolean success = DatabaseHelper.registerReservedPlate(plate);
            if (success) {
                JOptionPane.showMessageDialog(this, "VIP Plate " + plate.toUpperCase() + " registered successfully!");
                txtReservedPlate.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Error: Plate already exists or DB error.");
            }
        });

        reservedPanel.add(txtReservedPlate);
        reservedPanel.add(btnRegisterReserved);
        controlsPanel.add(reservedPanel);

        add(controlsPanel, BorderLayout.SOUTH);

        onParkingDataChanged();
    }

    @Override
    public void onParkingDataChanged() {
        if (lot == null) return;

        //revenue display
        lblRevenue.setText(String.format("Total Revenue: RM %.2f", lot.getTotalRevenue()));

        //occupancy & table data
        List<ParkingSpot> spots = lot.getSpots();
        if (spots != null) {
            //calculate occupancy
            long occupiedCount = spots.stream().filter(ParkingSpot::isOccupied).count();
            lblOccupancy.setText("Occupancy: " + occupiedCount + " / " + spots.size());

            tableModel.setRowCount(0); 
            for (ParkingSpot s : spots) {
                if (s.isOccupied() && s.getCurrentVehicle() != null) {
                    Vehicle v = s.getCurrentVehicle();
                    tableModel.addRow(new Object[]{
                        s.getSpotID(),
                        v.getLicensePlate(),
                        v.getType(),
                        new Date(v.getEntryTime()).toString()
                    });
                }
            }
        }
    }
}