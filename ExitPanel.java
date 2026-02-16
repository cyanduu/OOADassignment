import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class ExitPanel extends JPanel {
    private JTextField textSearchPlate;
    private JTextArea textReceiptArea;
    
    // --- BUTTONS ---
    private JButton buttonCalculate;
    private JButton buttonPayBill;
    private JButton buttonPrintReceipt;
    private JButton buttonOpenGate;
    
    // --- LOGIC VARIABLES ---
    private ParkingSpot foundSpot;
    private double totalAmountDue; // Total owed (Parking + Fines + Debts)
    private double hoursParked;
    private double parkingFee;
    private double newFines;
    private double oldDebts;
    private double rate;
    private long exitTimeMillis;
    
    // Payment State
    private double amountToPay;    // Actual amount user is paying now
    private boolean isFineDeferred;
    private String savedPlate;       
    private long savedEntryTime;     
    private String savedMethod;
    private String savedSpotID;

    public ExitPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Exit & Payment Station"));

        // 1. TOP PANEL: Search & Transaction Buttons
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(new Color(255, 255, 204));
        
        searchPanel.add(new JLabel("Enter License Plate:"));
        textSearchPlate = new JTextField(12);
        searchPanel.add(textSearchPlate);
        
        buttonCalculate = new JButton("Calculate Bill");
        searchPanel.add(buttonCalculate);

        buttonPayBill = new JButton("Pay Bill");
        buttonPayBill.setEnabled(false); 
        searchPanel.add(buttonPayBill);

        buttonPrintReceipt = new JButton("Print Receipt");
        buttonPrintReceipt.setEnabled(false); 
        searchPanel.add(buttonPrintReceipt);
        
        add(searchPanel, BorderLayout.NORTH);

        // 2. CENTER PANEL: Receipt Display
        textReceiptArea = new JTextArea("Enter plate to calculate fee...");
        textReceiptArea.setEditable(false);
        textReceiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(textReceiptArea), BorderLayout.CENTER);

        // 3. BOTTOM PANEL: Open Gate
        buttonOpenGate = new JButton("OPEN GATE");
        buttonOpenGate.setEnabled(false); 
        buttonOpenGate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        buttonOpenGate.setBackground(new Color(144, 238, 144));
        buttonOpenGate.setPreferredSize(new Dimension(200, 50));
        add(buttonOpenGate, BorderLayout.SOUTH);

        // 4. EVENT LISTENERS 
        buttonCalculate.addActionListener(e -> calculateExitDetails());
        buttonPayBill.addActionListener(e -> startPaymentProcess());
        buttonPrintReceipt.addActionListener(e -> simulatePrintReceipt());
        buttonOpenGate.addActionListener(e -> openGateAndReset());
    }

    // --- LOGIC: STEP 1 - CALCULATE BILL ---
    private void calculateExitDetails() {
        String plate = textSearchPlate.getText().trim().toUpperCase();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a license plate.");
            return;
        }

        // Find Vehicle
        foundSpot = ParkingLot.getInstance().findSpotByPlate(plate);

        if (foundSpot == null) {
            JOptionPane.showMessageDialog(this, "Vehicle not found! Is it currently parked?");
            textReceiptArea.setText("Vehicle not found.");
            disableAllButtons();
            return;
        }

        Vehicle v = foundSpot.getCurrentVehicle();

        // Save State (Critical for receipt generation later)
        savedPlate = v.getLicensePlate();
        savedEntryTime = v.getEntryTime();
        savedSpotID = foundSpot.getSpotID();

        // Calculate Duration
        exitTimeMillis = System.currentTimeMillis();
        double durationMs = exitTimeMillis - savedEntryTime;
        
        // --- SIMULATION ADJUSTMENT: 1 Minute = 1 Hour ---
        // Original Math: hoursParked = durationMs / (1000.0 * 60 * 60);
        // New Math: Dividing by 60,000ms (1 minute) so that 60 seconds of real time equals 1 hour of parking.
        double rawHours = durationMs / (1000.0 * 60); 

        // --- CEILING ROUNDING LOGIC ---
        // Requirement: Round up to the nearest hour. (e.g., 1.1 minutes real-time becomes 2 hours simulated)
        hoursParked = Math.ceil(rawHours);
        
        if (hoursParked < 1.0) hoursParked = 1.0; // Minimum charge 1 hour

        // Check Permits (Database)
        boolean hasCard = DatabaseHelper.hasHandicappedPermit(savedPlate);
        boolean isReservedSpot = foundSpot.getType().equalsIgnoreCase("Reserved");
        boolean hasVIPPermit = DatabaseHelper.hasReservedPermit(savedPlate);
        boolean isReservedViolation = isReservedSpot && !hasVIPPermit;
        
        // Calculate Fees (FineManager)
        rate = foundSpot.getHourlyRate();
        
        // Passing the rounded hoursParked to ensure billing reflects full hours
        parkingFee = FineManager.calculateParkingFee(hoursParked, rate, foundSpot.getType(), hasCard);
        newFines = FineManager.calculateFine(hoursParked, isReservedViolation);
        oldDebts = FineManager.getUnpaidFines(savedPlate);
        
        totalAmountDue = parkingFee + newFines + oldDebts;
        
        // Initialize Payment State
        amountToPay = totalAmountDue; 
        isFineDeferred = false;

        // Update UI -> Show Preview Receipt
        updateReceiptArea(false, null);
        
        // Enable Payment, Disable Gate
        buttonPayBill.setEnabled(true);
        buttonOpenGate.setEnabled(false);
        buttonPrintReceipt.setEnabled(false);
    }

    // --- LOGIC: STEP 2 - ASK TO SETTLE FINES ---
    private void startPaymentProcess() {
        double totalFines = newFines + oldDebts;

        // If user has fines, ask if they want to pay them now
        if (totalFines > 0) {
            int choice = JOptionPane.showConfirmDialog(this, 
                "You have outstanding fines of RM " + String.format("%.2f", totalFines) + ".\n" +
                "Do you want to settle them now?", 
                "Outstanding Fines", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (choice == JOptionPane.NO_OPTION) {
                // User defers fines -> Pay only parking fee
                isFineDeferred = true;
                amountToPay = parkingFee; 
            } else {
                // User pays all
                isFineDeferred = false;
                amountToPay = totalAmountDue; 
            }
        }

        showPaymentPopup();
    }

    // --- LOGIC: STEP 3 - SHOW PAYMENT GATEWAY ---
    private void showPaymentPopup() {
        if (foundSpot == null) return;

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Amount to Pay: RM " + String.format("%.2f", amountToPay)));
        
        if (isFineDeferred) {
            JLabel lblDefer = new JLabel("(Fines Deferred to Account)");
            lblDefer.setForeground(Color.RED);
            lblDefer.setFont(new Font("Monospaced", Font.ITALIC, 12));
            panel.add(lblDefer);
        }
        panel.add(new JLabel("Select Method:"));
        
        JRadioButton rbCash = new JRadioButton("Cash", true);
        JRadioButton rbCard = new JRadioButton("Debit/Credit Card");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbCash);
        bg.add(rbCard);
        
        panel.add(rbCash);
        panel.add(rbCard);

        int result = JOptionPane.showConfirmDialog(
            this, panel, "Payment Gateway", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String method = rbCash.isSelected() ? "Cash" : "Debit/Credit Card";
            processPaymentSuccess(method);
        }
    }

    // --- LOGIC: STEP 4 - PROCESS PAYMENT & UPDATE BACKEND ---
    private void processPaymentSuccess(String method) {
        savedMethod = method;

        // Handle Fines Logic
        if (isFineDeferred) {
            // User deferred -> Add NEW fines to debt, keep OLD fines
            if (newFines > 0) {
                FineManager.addFineToAccount(savedPlate, newFines);
            }
            JOptionPane.showMessageDialog(this, "Payment Approved.\nFines have been recorded for future.");
        } else {
            // User paid full -> Clear ALL fines
            FineManager.clearFines(savedPlate);
            JOptionPane.showMessageDialog(this, "Payment Approved via "+ method +"!\nAll fines cleared.");
        }

        // Backend Processing
        ParkingLot.getInstance().addRevenue(amountToPay);
        ParkingLot.getInstance().removeVehicle(foundSpot.getSpotID()); // Frees the spot

        // Generate Official Receipt
        updateReceiptArea(true, method);
        
        // Enable Exit Options
        buttonPayBill.setEnabled(false);
        buttonOpenGate.setEnabled(true);      // Gate Unlocked
        buttonPrintReceipt.setEnabled(true);  // Receipt Available
    }

    // --- LOGIC: STEP 5 - PRINT RECEIPT POPUP ---
    private void simulatePrintReceipt() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        
        double balance = isFineDeferred ? (newFines + oldDebts) : 0.00;

        StringBuilder sb = new StringBuilder();
        sb.append("============= OFFICIAL RECEIPT =============\n");
        sb.append("Entry Time:   ").append(sdf.format(new Date(savedEntryTime))).append("\n");
        sb.append("Exit Time:    ").append(sdf.format(new Date(exitTimeMillis))).append("\n");
        sb.append("Duration:     ").append(String.format("%.2f", hoursParked)).append(" hours\n");
        sb.append("--------------------------------------------\n");
        sb.append("Fee Breakdown:\n");
        sb.append("  Rate:       RM ").append(String.format("%.2f", rate)).append(" /hr\n");
        sb.append("  Parking Fee: RM ").append(String.format("%6.2f", parkingFee)).append("\n");
        
        if (newFines + oldDebts > 0) {
            sb.append("  Fines Due:   RM ").append(String.format("%6.2f", newFines + oldDebts)).append("\n");
        }
        
        sb.append("--------------------------------------------\n");
        sb.append("Total Paid:   RM ").append(String.format("%6.2f", amountToPay)).append("\n");
        sb.append("Method:       ").append(savedMethod).append("\n");
        
        sb.append("Rem. Balance: RM ").append(String.format("%6.2f", balance)).append("\n");
        sb.append("============================================");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Print Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- LOGIC: STEP 6 - OPEN GATE ---
    private void openGateAndReset() {
        JOptionPane.showMessageDialog(this, "Gate Opened. Drive Safely!", "Exit", JOptionPane.INFORMATION_MESSAGE);
        
        // Reset UI for next user
        textSearchPlate.setText("");
        textReceiptArea.setText("Enter plate to calculate fee...");
        disableAllButtons();
        foundSpot = null;
    }

    // --- HELPERS ---
    private void disableAllButtons() {
        buttonPayBill.setEnabled(false);
        buttonOpenGate.setEnabled(false);
        buttonPrintReceipt.setEnabled(false);
    }

    // Updates the central text area.
    // Logic differentiates between "Invoice" (Before Pay) and "Receipt" (After Pay)
    private void updateReceiptArea(boolean isFinal, String paymentMethod) {
        StringBuilder sb = new StringBuilder();
        sb.append("      --- PARKING RECEIPT ---\n");
        if (isFinal) {
            sb.append("Status: PAID via ").append(paymentMethod).append("\n");
        } else {
            sb.append("Status: PENDING PAYMENT\n");
        }
        sb.append("-------------------------------\n");
        sb.append("Plate No   : ").append(savedPlate).append("\n");
        sb.append("Spot ID    : ").append(savedSpotID).append("\n");
        
        // Display the simulated rounded hours
        // This confirms the Ceiling Rounding logic visually (e.g., 1.01 -> 2.0)
        sb.append("Duration   : ").append(String.format("%.1f", hoursParked)).append(" hours\n");
        sb.append("Hourly Rate: RM ").append(String.format("%.2f", rate)).append("\n");
        sb.append("-------------------------------\n");
        
        sb.append("Parking Fee: RM ").append(String.format("%.2f", parkingFee)).append("\n");
        
        if (newFines > 0) {
            sb.append("New Fines  : RM ").append(String.format("%.2f", newFines)).append(" (Violation/Overstay)\n");
        }
        
        if (oldDebts > 0) {
            sb.append("Old Debts  : RM ").append(String.format("%.2f", oldDebts)).append("\n");
        }
        
        sb.append("-------------------------------\n");
        
        if (isFineDeferred) {
            sb.append("TOTAL PAID : RM ").append(String.format("%.2f", amountToPay)).append("\n");
            sb.append("DEFERRED   : RM ").append(String.format("%.2f", totalAmountDue - amountToPay)).append("\n");
        } else {
            sb.append("TOTAL DUE  : RM ").append(String.format("%.2f", totalAmountDue)).append("\n");
        }
        
        sb.append("-------------------------------\n");
        sb.append("      THANK YOU & DRIVE SAFE\n");

        textReceiptArea.setText(sb.toString());
    }
}