import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExitPanel extends JPanel {
    private JTextField textSearchPlate;
    private JTextArea textReceiptArea;
    
    // --- BUTTONS (Class Level so we can enable/disable them) ---
    private JButton buttonCalculate;
    private JButton buttonPayBill;
    private JButton buttonPrintReceipt;
    private JButton buttonOpenGate;
    
    // --- LOGIC VARIABLES ---
    private ParkingSpot foundSpot;
    private double totalAmountDue;
    private double hoursParked;
    private double parkingFee;
    private double newFines;
    private double oldDebts;
    private double rate;
    private long exitTimeMillis;
    private double amountToPay;
    private boolean isFineDeferred;
    private String savedPlate;       
    private long savedEntryTime;     
    private String savedMethod;

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

        // Requirement: "Put Pay Bill beside Calculate Bill"
        buttonPayBill = new JButton("Pay Bill");
        buttonPayBill.setEnabled(false); 
        searchPanel.add(buttonPayBill);

        // Requirement: Print Receipt Option
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
        // Requirement: "Open Gate only can be pressed if user alr paid"
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

    
    // LOGIC: STEP 1 - CALCULATE BILL
    private void calculateExitDetails() {
        String plate = textSearchPlate.getText().trim().toUpperCase();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a license plate.");
            return;
        }

        // 1. Find Vehicle
        foundSpot = ParkingLot.getInstance().findSpotByPlate(plate);

        if (foundSpot == null) {
            JOptionPane.showMessageDialog(this, "Vehicle not found! Is it currently parked?");
            textReceiptArea.setText("Vehicle not found.");
            disableAllButtons();
            return;
        }

        Vehicle v = foundSpot.getCurrentVehicle();

        // 2. Calculate Duration
        long entryTime = v.getEntryTime();
        exitTimeMillis = System.currentTimeMillis();
        double durationMs = exitTimeMillis - entryTime;
        hoursParked = durationMs / (1000.0 * 60 * 60);
        if (hoursParked < 0.01) hoursParked = 1.0; // Min 1 hour

        // 3. Check Rules/Permits (DatabaseHelper)
        boolean hasCard = DatabaseHelper.hasHandicappedPermit(plate);
        boolean isReservedSpot = foundSpot.getType().equalsIgnoreCase("Reserved");
        boolean hasVIPPermit = DatabaseHelper.hasReservedPermit(plate);
        boolean isReservedViolation = isReservedSpot && !hasVIPPermit;
        
        // 4. Calculate Fees (FineManager)
        rate = foundSpot.getHourlyRate();
        parkingFee = FineManager.calculateParkingFee(hoursParked, rate, foundSpot.getType(), hasCard);
        newFines = FineManager.calculateFine(hoursParked, isReservedViolation);
        oldDebts = FineManager.getUnpaidFines(plate);
        totalAmountDue = parkingFee + newFines + oldDebts;
        
        savedPlate = plate;
        savedEntryTime = entryTime;
        amountToPay = totalAmountDue; 
        isFineDeferred = false;

        // 5. Update UI -> Show Preview Receipt
        updateReceiptArea(false, null);
        
        // State Change: Enable Payment, Disable Gate
        buttonPayBill.setEnabled(true);
        buttonOpenGate.setEnabled(false);
        buttonPrintReceipt.setEnabled(false);
    }

    //LOGIC: STEP 1.5 - ASK TO SETTLE FINES
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
                // USER CHOSE TO DEFER (Pay Later)
                isFineDeferred = true;
                amountToPay = parkingFee; // Pay ONLY parking fee
            } else {
                // USER CHOSE TO PAY ALL
                isFineDeferred = false;
                amountToPay = totalAmountDue; // Pay Fees + Fines
            }
        }

        // Proceed to the normal payment popup
        showPaymentPopup();
    }

    // LOGIC: STEP 2 - SHOW PAYMENT POPUP
    private void showPaymentPopup() {
        if (foundSpot == null) return;

        //Custom Layout for "Cash or Card"
        JPanel panel = new JPanel(new GridLayout(0, 1));

        panel.add(new JLabel("Amount to Pay: RM " + String.format("%.2f", amountToPay)));
        
        //Show a warning if they are deferring payment
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

    
    // LOGIC: STEP 3 - PROCESS PAYMENT & UPDATE RECEIPT
    private void processPaymentSuccess(String method) {
        // A. Capture Data BEFORE removing vehicle
        Vehicle v = foundSpot.getCurrentVehicle();
        String plate = v.getLicensePlate();

        savedMethod = method;

        if (isFineDeferred) {
            // User deferred -> Add NEW fines to debt, do NOT clear OLD fines
            if (newFines > 0) {
                FineManager.addFineToAccount(plate, newFines);
            }
            JOptionPane.showMessageDialog(this, "Payment Approved.\nFines have been recorded for future.");
        } else {
            // User paid full -> Clear ALL fines
            FineManager.clearFines(plate);
            JOptionPane.showMessageDialog(this, "Payment Approved via "+ method +"!\nAll fines cleared.");
        }

        // B. Backend Processing
        ParkingLot.getInstance().addRevenue(totalAmountDue);
        ParkingLot.getInstance().removeVehicle(foundSpot.getSpotID()); // Frees the spot

        // C. Update Receipt to "OFFICIAL RECEIPT"
        updateReceiptArea(true, method);
        
        JOptionPane.showMessageDialog(this, "Payment Approved via "+ method +"!");

        // D. State Change: Enable Gate & Print
        buttonPayBill.setEnabled(false);
        buttonOpenGate.setEnabled(true);      // GATE UNLOCKED
        buttonPrintReceipt.setEnabled(true);  // RECEIPT AVAILABLE
    }

    
    // LOGIC: STEP 4 - PRINT RECEIPT
    
    private void simulatePrintReceipt() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        int roundedHours = (int) Math.ceil(hoursParked);
        if (roundedHours == 0) roundedHours = 1;
        
        double balance = isFineDeferred ? (newFines + oldDebts) : 0.00;

        // Build the text message for the Popup
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
        
        if (balance > 0) {
            sb.append("Rem. Balance: RM ").append(String.format("%6.2f", balance)).append("\n");
        } else {
            sb.append("Rem. Balance: RM   0.00\n");
        }
        sb.append("============================================");

        // Show it in a Message Dialog
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Print Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    
    // LOGIC: STEP 5 - OPEN GATE & RESET
    private void openGateAndReset() {
        JOptionPane.showMessageDialog(this, "Gate Opened. Drive Safely!", "Exit", JOptionPane.INFORMATION_MESSAGE);
        
        // Reset UI for next user
        textSearchPlate.setText("");
        textReceiptArea.setText("Enter plate to calculate fee...");
        disableAllButtons();
        foundSpot = null;
    }

    
    // HELPERS
    private void disableAllButtons() {
        buttonPayBill.setEnabled(false);
        buttonOpenGate.setEnabled(false);
        buttonPrintReceipt.setEnabled(false);
    }

    private void updateReceiptArea(boolean isPaid, String method) {
        // Fallback plate/entry logic in case vehicle is already removed
        String plate = (foundSpot != null && foundSpot.getCurrentVehicle() != null) 
                       ? foundSpot.getCurrentVehicle().getLicensePlate() 
                       : textSearchPlate.getText().trim().toUpperCase();
                       
        long entryTime = (foundSpot != null && foundSpot.getCurrentVehicle() != null)
                         ? foundSpot.getCurrentVehicle().getEntryTime()
                         : System.currentTimeMillis() - (long)(hoursParked * 3600000);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        
        StringBuilder sb = new StringBuilder();
        sb.append(isPaid ? "========== OFFICIAL RECEIPT ==========\n" : "========== PARKING INVOICE ==========\n");
        sb.append(String.format("License Plate:   %s\n", plate));
        if (foundSpot != null) {
            sb.append(String.format("Spot ID:         %s (%s)\n", foundSpot.getSpotID(), foundSpot.getType()));
        }
        sb.append("--------------------------------------\n");
        sb.append(String.format("Entry Time:      %s\n", sdf.format(new Date(entryTime))));
        sb.append(String.format("Exit Time:       %s\n", sdf.format(new Date(exitTimeMillis))));
        sb.append(String.format("Duration:        %.2f Hours\n", hoursParked));
        sb.append("--------------------------------------\n");
        
        int roundedHours = (int) Math.ceil(hoursParked);
        if (roundedHours == 0) roundedHours = 1;

        sb.append(String.format("Parking Fee:     RM %6.2f\n", parkingFee));
        sb.append(String.format("  (Rate: %d hrs x RM %.2f)\n", roundedHours, rate));
        
        if (newFines > 0) sb.append(String.format("Violation Fine:  RM %6.2f\n", newFines));
        if (oldDebts > 0) sb.append(String.format("Prev. Unpaid:    RM %6.2f\n", oldDebts));
        
        sb.append("--------------------------------------\n");
        sb.append(String.format("TOTAL AMOUNT:    RM %6.2f\n", totalAmountDue));
        
        if (isPaid) {
            sb.append("--------------------------------------\n");
            // [NEW] Show actual amount paid vs outstanding
            sb.append(String.format("Paid Amount:     RM %6.2f\n", amountToPay));
            sb.append(String.format("Payment Method:  %s\n", method));
            
            double balance = totalAmountDue - amountToPay;
            sb.append(String.format("Outstanding:     RM %6.2f\n", balance));
            
            sb.append("\n        THANK YOU!        \n");
            sb.append("======================================");
        } else {
            sb.append("======================================");
        }

        textReceiptArea.setText(sb.toString());
    }
}