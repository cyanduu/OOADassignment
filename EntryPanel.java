import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class EntryPanel extends JPanel {

    private JComboBox<String> ComboType;
    private JComboBox<String> ComboSpots;
    private JTextField txtPlate;
    private JButton btnPark;

    public EntryPanel() {
        // Use a Grid Layout to split the screen 50/50
        setLayout(new GridLayout(1, 2, 15, 15));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- LEFT PANEL: DRIVER INPUTS ---
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Entry Station"));

        JPanel formPanel = new JPanel(new GridLayout(6, 1, 5, 5)); // Inputs

        // 1. License Plate
        formPanel.add(new JLabel("Enter License Plate:"));
        txtPlate = new JTextField();
        txtPlate.setFont(new Font("Monospaced", Font.BOLD, 14));
        formPanel.add(txtPlate);

        // 2. Vehicle Type
        formPanel.add(new JLabel("Select Vehicle Type:"));
        String[] types = {"Select Vehicle Type:-", "Car", "Motorcycle", "SUV/Truck", "Handicapped Vehicle"};
        ComboType = new JComboBox<>(types);
        formPanel.add(ComboType);

        // 3. Spot Selection
        formPanel.add(new JLabel("Select Parking Spot:"));
        ComboSpots = new JComboBox<>();
        formPanel.add(ComboSpots);

        leftPanel.add(formPanel, BorderLayout.NORTH);

        // Park Button (Big and Clear)
        btnPark = new JButton("ISSUE TICKET & PARK");
        btnPark.setFont(new Font("Arial", Font.BOLD, 14));
        btnPark.setPreferredSize(new Dimension(100, 50));
        leftPanel.add(btnPark, BorderLayout.SOUTH);


        // --- RIGHT PANEL: RULES & REGULATIONS (Permanent Display) ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Parking Regulations & Fines"));

        // Use JEditorPane for clean HTML formatting without "webpage" look
        JEditorPane infoPane = new JEditorPane();
        infoPane.setContentType("text/html");
        infoPane.setEditable(false);
        infoPane.setOpaque(false); // Match background
        
        // PROFESSIONAL, CLEAN CONTENT
        String rulesText = "<html><body style='font-family: Sans-Serif; font-size: 10px;'>" +
                
                // SECTION 1: ZONES
                "<b>1. PARKING ZONES & RATES</b>" +
                "<ul>" +
                "<li><b>Floor 1 (VIP/Reserved):</b> RM 10.00 / hour.<br><i>(Restricted to VIP Pass Holders)</i></li>" +
                "<li><b>Floor 1 (Handicapped):</b> RM 2.00 / hour.<br><i>(Free with Valid Permit)</i></li>" +
                "<li><b>Floors 2 & 3 (Standard):</b> RM 5.00 / hour.<br><i>(Open to all standard vehicles)</i></li>" +
                "<li><b>Floor 4 (Compact):</b> RM 2.00 / hour.<br><i>(Motorcycles & Compact Cars only)</i></li>" +
                "</ul>" +
                "<b>2. VIP ACCESS</b>" +
                "<p>VIP spots are reserved (S01-S15). Unauthorized parking will incur fines.</p>" +
                "</body></html>" +

                // SECTION 2: FINES
                "<b>2. VIOLATION POLICY</b>" +
                "<p>Vehicles parked in unauthorized zones (e.g., Standard Car in VIP Spot) will be subject to fines upon exit.</p>" +
                
                "<b>3. FINE SCHEMES (Subject to Admin Settings)</b>" +
                "<ul>" +
                "<li><b>Fixed Scheme:</b> Flat penalty of RM 50.00.</li>" +
                "<li><b>Progressive Scheme:</b> Penalty increases every 24 hours.</li>" +
                "<li><b>Hourly Scheme:</b> RM 20.00 charged per unauthorized hour.</li>" +
                "</ul>" +
                
                "<br><i>*Lost tickets will incur a maximum daily penalty.</i>" +
                "</body></html>";

        infoPane.setText(rulesText);
        
        // Add scroll pane in case screens are small
        rightPanel.add(new JScrollPane(infoPane), BorderLayout.CENTER);


        // --- ADD PANELS TO MAIN VIEW ---
        add(leftPanel);
        add(rightPanel);


        // --- ACTION LISTENERS ---
        ComboType.addActionListener(e -> updateAvailableSpots());

        btnPark.addActionListener(e -> {
            String plate = txtPlate.getText().trim().toUpperCase();
            String type = (String) ComboType.getSelectedItem();
            String spotID = (String) ComboSpots.getSelectedItem();

            if (plate.isEmpty() || type.equals("Select Vehicle Type:-") || spotID == null) {
                JOptionPane.showMessageDialog(this, "Error: Missing Information.\nPlease fill all fields.", "Entry Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Vehicle v = createVehicle(plate, type);
            if (v == null) return;

            // Park Logic
            Ticket ticket = ParkingLot.getInstance().parkVehicleAtSpot(spotID, v);

            if (ticket != null) {
                JOptionPane.showMessageDialog(this, 
                "Ticket Issued Successfully!\n\n" + ticket.getTicketDetails(), 
                "Access Granted", 
                JOptionPane.INFORMATION_MESSAGE);
                txtPlate.setText("");
                ComboType.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this, "Parking Failed. Spot may have been taken.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // --- LOGIC: SHOW SPOTS ---
    private void updateAvailableSpots() {
        ComboSpots.removeAllItems();
        String selectedType = (String) ComboType.getSelectedItem();
        
        if (selectedType == null || selectedType.equals("Select Vehicle Type:-")) return;

        Vehicle dummy = createVehicle("CHECK", selectedType);
        if (dummy == null) return;

        List<ParkingSpot> allSpots = ParkingLot.getInstance().getSpots();
        
        for (ParkingSpot s : allSpots) {
            // "Loose" Filter: Show the spot if it fits, OR if it's special (so users can see/select them)
            boolean isSpecialSpot = s.getType().equalsIgnoreCase("Reserved") || s.getType().equalsIgnoreCase("Handicapped");
            boolean physicallyFits = s.isSuitableFor(dummy);

            if (!s.isOccupied() && (physicallyFits || isSpecialSpot)) {
                ComboSpots.addItem(s.getSpotID());
            }
        }
    }

    private Vehicle createVehicle(String plate, String type) {
        if (type == null) return null;
        if (type.equals("Car") || type.equals("VIP Car") || type.equals("Handicapped Vehicle")) return new Car(plate);
        if (type.equals("Motorcycle")) return new Motorcycle(plate);
        if (type.equals("SUV/Truck")) return new SUV(plate);
        return null;
    }
}