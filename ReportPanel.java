import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ReportPanel extends JPanel implements ParkingObserver {
    private JLabel labelTotalRevenue;
    private JLabel labelOccupancyRate;
    private JProgressBar progressOccupancy;
    private JTable tableVehicles;
    private DefaultTableModel vehicleModel;
    private JTable tableFines;
    private DefaultTableModel fineModel;
    private DefaultTableModel historyModel;

    public ReportPanel() {
        //1. Register as an Observer to receive real-time updates
        ParkingLot.getInstance().addObserver(this);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("System Reporting & Analytics"));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Live Lot Status", createLiveStatusPanel());
        tabs.addTab("Financial Reports", createFinancialPanel());

        add(tabs, BorderLayout.CENTER);

        //Initial Data Load
        refreshData();
    }

    //SUB-PANEL: LIVE STATUS
    private JPanel createLiveStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        //Occupancy Summary (Top)
        JPanel statsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        labelOccupancyRate = new JLabel("Occupancy: 0 / 0 (0%)");
        labelOccupancyRate.setFont(new Font("Arial", Font.BOLD, 16));
        
        progressOccupancy = new JProgressBar(0, 100);
        progressOccupancy.setStringPainted(true);

        statsPanel.add(labelOccupancyRate);
        statsPanel.add(progressOccupancy);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(statsPanel, BorderLayout.NORTH);

        //Vehicle List Table (Center)
        String[] columns = {"Spot ID", "License Plate", "Type", "Entry Time"};
        vehicleModel = new DefaultTableModel(columns, 0);
        tableVehicles = new JTable(vehicleModel);
        
        JScrollPane scroll = new JScrollPane(tableVehicles);
        scroll.setBorder(BorderFactory.createTitledBorder("Vehicles Currently in Lot"));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    //SUB-PANEL: FINANCIAL REPORTS
    //Make sure you have this variable declared at the top of the class!
    private JPanel createFinancialPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        //1. Revenue Report (Top)
        JPanel revenuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelTotalRevenue = new JLabel("Total Revenue: RM 0.00");
        labelTotalRevenue.setFont(new Font("Arial", Font.BOLD, 18));
        labelTotalRevenue.setForeground(new Color(0, 100, 0));
        revenuePanel.add(labelTotalRevenue);
        revenuePanel.setBorder(BorderFactory.createTitledBorder("Revenue Report"));
        panel.add(revenuePanel, BorderLayout.NORTH);

        //2. Transaction History Table (New)
        String[] historyCols = {"Time", "Plate", "Spot", "Method", "Amount (RM)"};
        historyModel = new DefaultTableModel(historyCols, 0);
        JTable lableHistory = new JTable(historyModel);
        JScrollPane historyScroll = new JScrollPane(lableHistory);
        historyScroll.setBorder(BorderFactory.createTitledBorder("Transaction History (Past & Present)"));

        //3. Fine Report Table (RESTORED!)
        String[] fineCols = {"License Plate", "Outstanding Amount (RM)", "Status"};
        fineModel = new DefaultTableModel(fineCols, 0); //
        tableFines = new JTable(fineModel);
        JScrollPane fineScroll = new JScrollPane(tableFines);
        fineScroll.setBorder(BorderFactory.createTitledBorder("Outstanding Fines Report"));

        //4. Combine them (Split Pane so you see both)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, historyScroll, fineScroll);
        splitPane.setDividerLocation(300); // Give history half the space
        
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    //OBSERVER TRIGGER
    @Override
    public void onParkingDataChanged() {
        //Use invokeLater to ensure thread safety when updating Swing components
        SwingUtilities.invokeLater(this::refreshData);
    }

    //DATA REFRESH LOGIC
    private void refreshData() {
        ParkingLot lot = ParkingLot.getInstance();
        List<ParkingSpot> spots = lot.getAllSpots();

        //1. Update Occupancy Stats
        long occupiedCount = spots.stream().filter(ParkingSpot::isOccupied).count();
        int totalSpots = spots.size();
        int percent = totalSpots > 0 ? (int)((occupiedCount * 100) / totalSpots) : 0;

        labelOccupancyRate.setText(String.format("Occupancy: %d / %d (%d%%)", occupiedCount, totalSpots, percent));
        progressOccupancy.setValue(percent);

        //2. Update Revenue Display
        //Note: This now pulls the calculated total from your Transaction History list
        labelTotalRevenue.setText(String.format("Total Revenue Collected: RM %.2f", lot.getTotalRevenue()));

        //3. Update Live Vehicle Table (Currently Parked Cars)
        vehicleModel.setRowCount(0); 
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss"); 
        
        for (ParkingSpot s : spots) {
            if (s.isOccupied() && s.getCurrentVehicle() != null) {
                Vehicle v = s.getCurrentVehicle();
                vehicleModel.addRow(new Object[]{
                    s.getSpotID(),
                    v.getLicensePlate(),
                    v.getType(),
                    sdf.format(new java.util.Date(v.getEntryTime()))
                });
            }
        }

        //4. Update Transaction History Table (Past & Present Customers)
        //Ensure 'historyModel' was initialized in your constructor/createFinancialPanel
        if (historyModel != null) {
            historyModel.setRowCount(0); // Clear old data
            List<Transaction> history = lot.getHistory();
            
            if (history != null) {
                //Loop backwards so the NEWEST transaction appears at the TOP
                for (int i = history.size() - 1; i >= 0; i--) {
                    Transaction t = history.get(i);
                    historyModel.addRow(new Object[]{
                        sdf.format(t.getExitTime()),
                        t.getPlate(),
                        t.getSpotID(),
                        t.getMethod(),
                        String.format("%.2f", t.getAmount())
                    });
                }
            }
        }

        //5. Update Fine Report
        updateFineReport();
    }

    private void updateFineReport() {
        fineModel.setRowCount(0); //Clear table
        
        //Fetch Real Data from FineManager
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