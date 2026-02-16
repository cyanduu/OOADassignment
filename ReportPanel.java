import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ReportPanel extends JPanel implements ParkingObserver {
    private JLabel lblTotalRevenue;
    private JLabel lblOccupancyRate;
    private JProgressBar progressOccupancy;
    private JTable tblVehicles;
    private DefaultTableModel vehicleModel;
    private JTable tblFines;
    private DefaultTableModel fineModel;

    public ReportPanel() {
        // 1. Register as an Observer to receive real-time updates
        ParkingLot.getInstance().addObserver(this);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("System Reporting & Analytics"));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Live Lot Status", createLiveStatusPanel());
        tabs.addTab("Financial Reports", createFinancialPanel());

        add(tabs, BorderLayout.CENTER);

        // Initial Data Load
        refreshData();
    }

    // --- SUB-PANEL: LIVE STATUS ---
    private JPanel createLiveStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Occupancy Summary (Top)
        JPanel statsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        lblOccupancyRate = new JLabel("Occupancy: 0 / 0 (0%)");
        lblOccupancyRate.setFont(new Font("Arial", Font.BOLD, 16));
        
        progressOccupancy = new JProgressBar(0, 100);
        progressOccupancy.setStringPainted(true);

        statsPanel.add(lblOccupancyRate);
        statsPanel.add(progressOccupancy);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(statsPanel, BorderLayout.NORTH);

        // Vehicle List Table (Center)
        String[] columns = {"Spot ID", "License Plate", "Type", "Entry Time"};
        vehicleModel = new DefaultTableModel(columns, 0);
        tblVehicles = new JTable(vehicleModel);
        
        JScrollPane scroll = new JScrollPane(tblVehicles);
        scroll.setBorder(BorderFactory.createTitledBorder("Vehicles Currently in Lot"));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --- SUB-PANEL: FINANCIAL REPORTS ---
    private JPanel createFinancialPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Revenue Report (Top)
        JPanel revenuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTotalRevenue = new JLabel("Total Revenue Collected: RM 0.00");
        lblTotalRevenue.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotalRevenue.setForeground(new Color(0, 100, 0)); // Dark Green for money
        
        revenuePanel.add(lblTotalRevenue);
        revenuePanel.setBorder(BorderFactory.createTitledBorder("Revenue Report"));
        panel.add(revenuePanel, BorderLayout.NORTH);

        // Fine Report Table (Center)
        String[] columns = {"License Plate", "Outstanding Amount (RM)", "Status"};
        fineModel = new DefaultTableModel(columns, 0);
        tblFines = new JTable(fineModel);
        
        JScrollPane scroll = new JScrollPane(tblFines);
        scroll.setBorder(BorderFactory.createTitledBorder("Outstanding Fines Report"));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --- OBSERVER TRIGGER ---
    @Override
    public void onParkingDataChanged() {
        // Use invokeLater to ensure thread safety when updating Swing components
        SwingUtilities.invokeLater(this::refreshData);
    }

    // --- DATA REFRESH LOGIC ---
    private void refreshData() {
        ParkingLot lot = ParkingLot.getInstance();
        List<ParkingSpot> spots = lot.getAllSpots();

        // 1. Update Occupancy Stats
        long occupiedCount = spots.stream().filter(ParkingSpot::isOccupied).count();
        int totalSpots = spots.size();
        int percent = totalSpots > 0 ? (int)((occupiedCount * 100) / totalSpots) : 0;

        lblOccupancyRate.setText(String.format("Occupancy: %d / %d (%d%%)", occupiedCount, totalSpots, percent));
        progressOccupancy.setValue(percent);

        // 2. Update Revenue Display
        lblTotalRevenue.setText(String.format("Total Revenue Collected: RM %.2f", lot.getTotalRevenue()));

        // 3. Update Vehicle Table
        vehicleModel.setRowCount(0); // Clear table
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss"); 
        
        for (ParkingSpot s : spots) {
            if (s.isOccupied() && s.getCurrentVehicle() != null) {
                Vehicle v = s.getCurrentVehicle();
                vehicleModel.addRow(new Object[]{
                    s.getSpotID(),
                    v.getLicensePlate(),
                    v.getType(),
                    sdf.format(new Date(v.getEntryTime()))
                });
            }
        }

        // 4. Update Fine Report
        updateFineReport();
    }

    private void updateFineReport() {
        fineModel.setRowCount(0); // Clear table
        
        // Fetch Real Data from FineManager
        Map<String, Double> realFines = FineManager.getAllOutstandingFines();

        for (Map.Entry<String, Double> entry : realFines.entrySet()) {
            fineModel.addRow(new Object[]{
                entry.getKey(),
                String.format("%.2f", entry.getValue()),
                "UNPAID"
            });
        }
    }
}