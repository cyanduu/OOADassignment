import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ExitPanel extends JPanel {
    private JTextField textSearchPlate;
    private JTextArea textReceiptArea;
    private JButton buttonPay;
    
    // Logic Variables (Store data between "Calculate" and "Pay" steps)
    private ParkingSpot foundSpot;
    private double hourlyFee;
    private double unpaidFines;
    private double totalAmountDue;

    public ExitPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Exit & Payment Station"));

        // --- TOP: Search Section (Step 1) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Enter License Plate:"));
        textSearchPlate = new JTextField(15);
        searchPanel.add(textSearchPlate);
        
        JButton buttonCalculate = new JButton("Calculate Bill");
        searchPanel.add(buttonCalculate);
        
        add(searchPanel, BorderLayout.NORTH);

        // --- CENTER: Receipt/Info Display (Step 6 & 9) ---
        textReceiptArea = new JTextArea("Enter plate to calculate fee...");
        textReceiptArea.setEditable(false);
        textReceiptArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(textReceiptArea), BorderLayout.CENTER);

        // --- BOTTOM: Payment Action (Step 7) ---
        buttonPay = new JButton("Pay & Open Gate");
        buttonPay.setEnabled(false); // Disabled until bill is calculated
        buttonPay.setBackground(new Color(144, 238, 144)); // Light Green
        buttonPay.setPreferredSize(new Dimension(200, 50));
        add(buttonPay, BorderLayout.SOUTH);

        // --- EVENTS ---
        buttonCalculate.addActionListener(e -> calculateExitDetails());
        buttonPay.addActionListener(e -> processPayment());
    }

    // Steps 2, 3, 4, 5, 6
    private void calculateExitDetails() {
        String plate = textSearchPlate.getText().trim().toUpperCase();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a license plate.");
            return;
        }

        // Step 2: Find the vehicle in the system
        foundSpot = findSpotByPlate(plate);

        if (foundSpot == null) {
            JOptionPane.showMessageDialog(this, "Vehicle not found! Is it currently parked?");
            textReceiptArea.setText("Vehicle not found.");
            buttonPay.setEnabled(false);
            return;
        }

        Vehicle v = foundSpot.getCurrentVehicle(); //

        // Step 3: Calculate Duration
        long durationMs = System.currentTimeMillis() - v.getEntryTime();
        double hours = durationMs / (1000.0 * 60 * 60);
        
        // Simulation: If hours is 0 (testing), make it 1 hour minimum
        if (hours < 0.01) hours = 1.0; 

        // Step 4: Calculate Fee based on Spot Type/Rate
        double rate = foundSpot.getHourlyRate(); 
        hourlyFee = hours * rate;

        // Step 5: Check Unpaid Fines (Using our new helper)
        unpaidFines = FineService.getOutstandingFines(plate);

        // Total
        totalAmountDue = hourlyFee + unpaidFines;

        // Step 6: Show Details
        String invoice = String.format(
            "=== EXIT BILL ===\n" +
            "License Plate: %s\n" +
            "Spot ID:       %s (%s)\n" +
            "------------------------\n" +
            "Duration:      %.2f hrs\n" +
            "Hourly Rate:   RM %.2f\n" +
            "Parking Fee:   RM %.2f\n" +
            "Unpaid Fines:  RM %.2f\n" +
            "------------------------\n" +
            "TOTAL DUE:     RM %.2f",
            plate, 
            foundSpot.getSpotID(), 
            foundSpot.getType(),
            hours, 
            rate, 
            hourlyFee, 
            unpaidFines, 
            totalAmountDue
        );

        textReceiptArea.setText(invoice);
        buttonPay.setEnabled(true); // Enable payment button now
    }

    // Steps 7, 8, 9
    private void processPayment() {
        if (foundSpot == null) return;

        // Step 7: Accept Payment (Simulated)
        int choice = JOptionPane.showConfirmDialog(this, 
            "Total Amount: RM " + String.format("%.2f", totalAmountDue) + "\nConfirm Payment?", 
            "Payment", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            // Update Revenue in Backend
            ParkingLot.getInstance().addRevenue(totalAmountDue);

            // Step 8: Mark spot as available
            ParkingLot.getInstance().removeVehicle(foundSpot.getSpotID());

            // Step 9: Generate Exit Receipt
            String receipt = textReceiptArea.getText().replace("=== EXIT BILL ===", "=== PAID RECEIPT ===");
            receipt += "\n\n[PAID] Thank you for visiting!";
            textReceiptArea.setText(receipt);
            
            JOptionPane.showMessageDialog(this, "Payment Successful. Gate Opening...");

            // Reset UI for next customer
            textSearchPlate.setText("");
            buttonPay.setEnabled(false);
            foundSpot = null;
        }

    }
}