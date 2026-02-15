import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;

public class ExitPanel extends JPanel {
    private JTextField textSearchPlate;
    private JTextArea textReceiptArea;
    private JButton buttonPay;
    
    // Logic Variables (Store data between "Calculate" and "Pay" steps)
    private ParkingSpot foundSpot;
    private double hourlyFee;
    private double unpaidFines;
    private double hoursParked;
    private long exitTimeMillis;
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
    // Updated for Member 2's FineManager
    private void calculateExitDetails() {
        String plate = textSearchPlate.getText().trim().toUpperCase();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a license plate.");
            return;
        }

        // 1. Find the vehicle
        foundSpot = ParkingLot.getInstance().findSpotByPlate(plate); // You added this to ParkingLot earlier!

        if (foundSpot == null) {
            JOptionPane.showMessageDialog(this, "Vehicle not found! Is it currently parked?");
            textReceiptArea.setText("Vehicle not found.");
            buttonPay.setEnabled(false);
            return;
        }

        Vehicle v = foundSpot.getCurrentVehicle(); // You added this to ParkingSpot earlier!

        // 2. Calculate Duration
        long entryTime = v.getEntryTime();
        exitTimeMillis = System.currentTimeMillis();
        double durationMs = exitTimeMillis - entryTime;
        hoursParked = durationMs / (1000.0 * 60 * 60);
        if (hoursParked < 0.01) hoursParked = 1.0; // Minimum 1 hour

        // 3. Setup flags for Member 2's logic
        // (In a real app, you'd have a checkbox for 'Card Presented', assuming false for now)
        boolean hasCard = DatabaseHelper.hasHandicappedPermit(plate); // Check if they have a handicapped permit for discounts
        // Check if they parked in a Reserved spot without being a "Reserved" vehicle (Example logic)
        boolean isReservedSpot = foundSpot.getType().equalsIgnoreCase("Reserved"); 
        boolean hasVIPPermit = DatabaseHelper.hasReservedPermit(plate); // Check if they have a reserved permit
        boolean isReservedViolation = isReservedSpot && !hasVIPPermit;
        
        // 4. Calculate Costs
        double rate = foundSpot.getHourlyRate();
        double parkingFee = FineManager.calculateParkingFee(hoursParked, rate, foundSpot.getType(), hasCard);
        double newFines = FineManager.calculateFine(hoursParked, isReservedViolation);
        double oldDebts = FineManager.getUnpaidFines(plate);
        totalAmountDue = parkingFee + newFines + oldDebts;
        
        /*// Calculate Overstay/Violation Fines
        double newFines = FineManager.calculateFine(hours, isReservedViolation);
        
        // Get Old Debts
        unpaidFines = FineManager.getUnpaidFines(plate);

        // Total
        totalAmountDue = parkingFee + newFines + unpaidFines;*/

        // 5. Format Dates
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String strEntry = sdf.format(new Date(entryTime));
        String strExit = sdf.format(new Date(exitTimeMillis));

        // 6. Generate Invoice Text
        StringBuilder sb = new StringBuilder();
        sb.append("========== PARKING INVOICE ==========\n");
        sb.append(String.format("License Plate:   %s\n", plate));
        sb.append(String.format("Spot ID:         %s (%s)\n", foundSpot.getSpotID(), foundSpot.getType()));
        sb.append("-------------------------------------\n");
        sb.append(String.format("Entry Time:      %s\n", strEntry));
        sb.append(String.format("Exit Time:       %s\n", strExit));
        sb.append(String.format("Duration:        %.2f Hours\n", hoursParked));
        sb.append("-------------------------------------\n");
        
        // Fee Breakdown
        int roundedHours = (int) Math.ceil(hoursParked);
        if (roundedHours == 0) roundedHours = 1;
        sb.append(String.format("Parking Fee:     RM %6.2f\n", parkingFee));
        sb.append(String.format("  (Rate: %d hrs x RM %.2f)\n", roundedHours, rate));
        
        if (newFines > 0) {
            sb.append(String.format("Violation Fine:  RM %6.2f\n", newFines));
        }
        if (oldDebts > 0) {
            sb.append(String.format("Prev. Unpaid:    RM %6.2f\n", oldDebts));
        }
        
        sb.append("-------------------------------------\n");
        sb.append(String.format("TOTAL AMOUNT:    RM %6.2f\n", totalAmountDue));
        sb.append("=====================================");

        textReceiptArea.setText(sb.toString());
        buttonPay.setEnabled(true);
    }

    // Steps 7, 8, 9
    private void processPayment() {
        if (foundSpot == null) return;

        // 1. Select Payment Method
        String[] options = {"Cash", "Credit Card"};
        int response = JOptionPane.showOptionDialog(this, 
                "Select Payment Method for RM " + String.format("%.2f", totalAmountDue), 
                "Payment Gateway", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, options, options[0]);

        if (response < 0) return; // User cancelled
        String paymentMethod = options[response];

        // --- CRITICAL FIX START ---
        
        // Step A: Save the data we need BEFORE removing the car
        Vehicle v = foundSpot.getCurrentVehicle();
        String plateToClear = v.getLicensePlate(); 

        // Step B: Now it is safe to remove the car from the backend
        ParkingLot.getInstance().addRevenue(totalAmountDue);
        ParkingLot.getInstance().removeVehicle(foundSpot.getSpotID());

        // Step C: Clear the fines using the saved string
        FineManager.clearFines(plateToClear); 
        
        // --- CRITICAL FIX END ---

        // 3. Generate Final Receipt
        String previousText = textReceiptArea.getText();
        String receipt = previousText.replace("========== PARKING INVOICE ==========", "========== OFFICIAL RECEIPT ==========");
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nPAYMENT DETAILS:\n");
        sb.append(String.format("Method:          %s\n", paymentMethod));
        sb.append(String.format("Amount Paid:     RM %6.2f\n", totalAmountDue));
        sb.append("Balance Due:     RM   0.00\n"); 
        sb.append("\n      THANK YOU FOR VISITING!      \n");
        sb.append("=====================================");

        textReceiptArea.setText(receipt + sb.toString());
        
        JOptionPane.showMessageDialog(this, "Payment Successful (" + paymentMethod + "). Gate Opening...");

        // Reset UI
        textSearchPlate.setText("");
        buttonPay.setEnabled(false);
        foundSpot = null;
    }
}