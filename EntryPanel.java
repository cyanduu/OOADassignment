import java.awt.*;
import java.util.List;
import javax.swing.*;

public class EntryPanel extends JPanel {

    private JComboBox<String> ComboType;
    private JComboBox<String> ComboSpots;
    private JTextField textPlate;
    private JButton buttonPark;

    public EntryPanel() {
        // Use a Grid Layout to split the screen 50/50
        setLayout(new GridLayout(1, 2, 15, 15));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new Color(255, 255, 204)); 

        // --- LEFT PANEL: DRIVER INPUTS ---
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Entry Station"));
        leftPanel.setBackground(new Color(240, 240, 240)); 

        JPanel formPanel = new JPanel(new GridLayout(6, 1, 5, 5)); 

        // 1. License Plate Input
        formPanel.add(new JLabel("Enter License Plate:"));
        textPlate = new JTextField();
        textPlate.setFont(new Font("Monospaced", Font.BOLD, 14));
        formPanel.add(textPlate);

        // 2. Vehicle Type Selection
        formPanel.add(new JLabel("Select Vehicle Type:"));
        String[] types = {"Select Vehicle Type:-", "Car", "Motorcycle", "SUV/Truck", "Handicapped Vehicle"};
        ComboType = new JComboBox<>(types);
        formPanel.add(ComboType);

        // 3. Spot Selection
        formPanel.add(new JLabel("Select Parking Spot:"));
        ComboSpots = new JComboBox<>();
        formPanel.add(ComboSpots);

        leftPanel.add(formPanel, BorderLayout.NORTH);

        // Park Button
        buttonPark = new JButton("ISSUE TICKET & PARK");
        buttonPark.setFont(new Font("Segoe UI", Font.BOLD, 14));
        buttonPark.setBackground(new Color(144, 238, 144)); // Light Green
        buttonPark.setPreferredSize(new Dimension(100, 50));
        leftPanel.add(buttonPark, BorderLayout.SOUTH);


        // --- RIGHT PANEL: RULES & REGULATIONS ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Parking Regulations & Fines"));
        rightPanel.setBackground(new Color(240, 240, 240)); 

        // Use JEditorPane for clean HTML formatting
        JEditorPane infoPane = new JEditorPane();
        infoPane.setContentType("text/html");
        infoPane.setEditable(false);
        infoPane.setBackground(new Color(255, 255, 204));
        
        // Define the rules text using HTML
        String rulesText = "<html><body style='font-family: Sans-Serif; font-size: 10px;'>" +
                
                // SECTION 1: ZONES
                "<b>1. PARKING ZONES & RATES</b>" +
                "<ul style='margin-top: 0px; margin-bottom: 5px;'>" +
                "<li><b>Floor 1 (VIP/Reserved):</b> RM 10.00 / hour.<br><i>(Restricted to VIP Pass Holders)</i></li>" +
                "<li><b>Floor 1 (Handicapped):</b> RM 2.00 / hour.<br><i>(Free with Valid Permit)</i></li>" +
                "<li><b>Floors 2 & 3 (Standard):</b> RM 5.00 / hour.<br><i>(Open to all standard vehicles)</i></li>" +
                "<li><b>Floor 4 (Compact):</b> RM 2.00 / hour.<br><i>(Motorcycles & Compact Cars only)</i></li>" +
                "</ul>" +

                // SECTION 2: VIP ACCESS
                "<b>2. VIP ACCESS</b>" +
                "<div style='margin-left: 20px; margin-bottom: 8px;'>" +
                "VIP spots are reserved (S01-S15). Unauthorized parking will incur fines." +
                "</div>" +

                // SECTION 3: VIOLATION POLICY
                "<b>3. VIOLATION POLICY</b>" +
                "<div style='margin-left: 20px; margin-bottom: 8px;'>" +
                "Vehicles parked in unauthorized zones (e.g., Standard Car in VIP Spot) will be subject to fines upon exit." +
                "</div>" +
                
                // SECTION 4: FINE SCHEMES
                "<b>4. FINE SCHEMES (Subject to Admin Settings)</b>" +
                "<ul style='margin-top: 0px; margin-bottom: 0px;'>" +
                "<li><b>Fixed Scheme:</b> Flat penalty of RM 50.00.</li>" +
                "<li><b>Progressive Scheme:</b> Penalty increases every 24 hours.</li>" +
                "<li><b>Hourly Scheme:</b> RM 20.00 charged per unauthorized hour.</li>" +
                "</ul>" +
                
                "<div style='margin-top: 5px; margin-left: 5px;'><i>*Lost tickets will incur a maximum daily penalty.</i></div>" +
                "</body></html>";

        infoPane.setText(rulesText);
        
        // Add scroll pane for better visibility on small screens
        rightPanel.add(new JScrollPane(infoPane), BorderLayout.CENTER);


        // --- ADD PANELS TO MAIN VIEW ---
        add(leftPanel);
        add(rightPanel);


        // --- ACTION LISTENERS ---
        
        // Update available spots when vehicle type changes
        ComboType.addActionListener(e -> updateAvailableSpots());

        // Handle Park Button Click
        buttonPark.addActionListener(e -> {
            String plate = textPlate.getText().trim().toUpperCase();
            String type = (String) ComboType.getSelectedItem();
            String spotID = (String) ComboSpots.getSelectedItem();

            if (plate.isEmpty() || type.equals("Select Vehicle Type:-") || spotID == null) {
                JOptionPane.showMessageDialog(this, "Error: Missing Information.\nPlease fill all fields.", "Entry Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verify that the vehicle is not already currently parked
            if (ParkingLot.getInstance().findSpotByPlate(plate) != null) {
                JOptionPane.showMessageDialog(this, 
                    "Error: Vehicle with plate " + plate + " is already inside the parking lot!", 
                    "Duplicate Entry", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            Vehicle v = createVehicle(plate, type);
            if (v == null) return;

            // Attempt to park the vehicle in the backend
            Ticket ticket = ParkingLot.getInstance().parkVehicleAtSpot(spotID, v);

            if (ticket != null) {
                JOptionPane.showMessageDialog(this, 
                "Ticket Issued Successfully!\n\n" + ticket.getTicketDetails(), 
                "Access Granted", 
                JOptionPane.INFORMATION_MESSAGE);
                
                // Reset form
                textPlate.setText("");
                ComboType.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this, "Parking Failed. Spot may have been taken.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // Filters and displays available spots based on vehicle type and spot rules
    private void updateAvailableSpots() {
        ComboSpots.removeAllItems();
        String selectedType = (String) ComboType.getSelectedItem();
        
        if (selectedType == null || selectedType.equals("Select Vehicle Type:-")) return;

        Vehicle dummy = createVehicle("CHECK", selectedType);
        if (dummy == null) return;

        List<ParkingSpot> allSpots = ParkingLot.getInstance().getSpots();
        
        for (ParkingSpot s : allSpots) {
            // Allow users to see special spots (VIP/Handicapped) even if they might not fit strictly, 
            // so they can choose to break the rules (and get fined).
            boolean isSpecialSpot = s.getType().equalsIgnoreCase("Reserved") || s.getType().equalsIgnoreCase("Handicapped");
            boolean physicallyFits = s.isSuitableFor(dummy);

            if (!s.isOccupied() && (physicallyFits || isSpecialSpot)) {
                ComboSpots.addItem(s.getSpotID());
            }
        }
    }

    // Factory method to create specific Vehicle objects based on selection
    private Vehicle createVehicle(String plate, String type) {
        if (type == null) return null;
        if (type.equals("Car") || type.equals("VIP Car") || type.equals("Handicapped Vehicle")) return new Car(plate);
        if (type.equals("Motorcycle")) return new Motorcycle(plate);
        if (type.equals("SUV/Truck")) return new SUV(plate);
        return null;
    }
}