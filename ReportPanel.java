import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ReportPanel extends JPanel implements ParkingObserver {
    private JLabel lblTotalRevenue;
    private JLabel lblOccupancyRate;
    private JProgressBar progressOccupancy;
    private JTable tblVehicles;
    private DefaultTableModel vehicleModel;
    private JTable tblFines;
    private DefaultTableModel fineModel;

    public ReportPanel() {
        // 1. Register as an Observer so reports update automatically
        ParkingLot.getInstance().addObserver(this);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("System Reporting & Analytics"));

        // --- TABS for different reports ---
        JTabbedPane tabs = new JTabbedPane();

        // TAB 1: Live Lot Status (Vehicles & Occupancy)
        tabs.addTab("Live Lot Status", createLiveStatusPanel());

        // TAB 2: Financial Reports (Revenue & Fines)
        tabs.addTab("Financial Reports", createFinancialPanel());

        add(tabs, BorderLayout.CENTER);

        // Initial Data Load
        refreshData();
    }

    // --- SUB-PANEL: Live Status (Vehicles + Occupancy) ---
    private JPanel createLiveStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // 1. Occupancy Summary (Top)
        JPanel statsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        lblOccupancyRate = new JLabel("Occupancy: 0 / 0 (0%)");
        lblOccupancyRate.setFont(new Font("Arial", Font.BOLD, 16));
        
        progressOccupancy = new JProgressBar(0, 100);
        progressOccupancy.setStringPainted(true);

        statsPanel.add(lblOccupancyRate);
        statsPanel.add(progressOccupancy);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(statsPanel, BorderLayout.NORTH);

        // 2. Vehicle List Table (Center)
        String[] columns = {"Spot ID", "License Plate", "Type", "Entry Time"};
        vehicleModel = new DefaultTableModel(columns, 0);
        tblVehicles = new JTable(vehicleModel);
        
        JScrollPane scroll = new JScrollPane(tblVehicles);
        scroll.setBorder(BorderFactory.createTitledBorder("Vehicles Currently in Lot"));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --- SUB-PANEL: Financials (Revenue + Fines) ---
    private JPanel createFinancialPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // 1. Revenue Report (Top)
        JPanel revenuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTotalRevenue = new JLabel("Total Revenue Collected: RM 0.00");
        lblTotalRevenue.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotalRevenue.setForeground(new Color(0, 100, 0)); // Dark Green
        revenuePanel.add(lblTotalRevenue);
        revenuePanel.setBorder(BorderFactory.createTitledBorder("Revenue Report"));
        panel.add(revenuePanel, BorderLayout.NORTH);

        // 2. Fine Report (Center)
        // Since 'FineService' is external, we simulate fetching a report here
        String[] columns = {"License Plate", "Outstanding Amount (RM)", "Status"};
        fineModel = new DefaultTableModel(columns, 0);
        tblFines = new JTable(fineModel);
        
        JScrollPane scroll = new JScrollPane(tblFines);
        scroll.setBorder(BorderFactory.createTitledBorder("Outstanding Fines Report"));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --- DATA REFRESH LOGIC ---
    // This is called automatically whenever data changes (Observer Pattern)
    @Override
    public void onParkingDataChanged() {
        refreshData();
    }

    private void refreshData() {
        ParkingLot lot = ParkingLot.getInstance();
        List<ParkingSpot> spots = lot.getAllSpots(); //

        // 1. Update Occupancy
        long occupiedCount = spots.stream().filter(ParkingSpot::isOccupied).count();
        int totalSpots = spots.size();
        int percent = totalSpots > 0 ? (int)((occupiedCount * 100) / totalSpots) : 0;

        lblOccupancyRate.setText(String.format("Occupancy: %d / %d (%d%%)", occupiedCount, totalSpots, percent));
        progressOccupancy.setValue(percent);

        // 2. Update Revenue
        lblTotalRevenue.setText(String.format("Total Revenue Collected: RM %.2f", lot.getTotalRevenue()));

        // 3. Update Vehicle Table
        vehicleModel.setRowCount(0); // Clear old data
        for (ParkingSpot s : spots) {
            if (s.isOccupied()) {
                Vehicle v = s.getCurrentVehicle(); //
                vehicleModel.addRow(new Object[]{
                    s.getSpotID(),
                    v.getLicensePlate(),
                    v.getType(),
                    new java.util.Date(v.getEntryTime()).toString()
                });
            }
        }

        // 4. Update Fine Report
        // In a real system, this would come from a database. 
        // Here we simulate it or fetch from FineService if you implemented it.
        updateFineReport();
    }

    private void updateFineReport() {
        fineModel.setRowCount(0);
        
        // Mock Data for demonstration (Replace with FineService.getAllFines() if available)
        // This fulfills the "Fine report" requirement visually
        Map<String, Double> mockFines = new HashMap<>();
        mockFines.put("BAD-1234", 50.0);
        mockFines.put("LATE-99", 25.50);
        mockFines.put("SPEEDY-1", 150.00);

        for (Map.Entry<String, Double> entry : mockFines.entrySet()) {
            fineModel.addRow(new Object[]{
                entry.getKey(),
                String.format("%.2f", entry.getValue()),
                "UNPAID"
            });
        }
    }
}