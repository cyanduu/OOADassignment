import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EntryPanel extends JPanel {
    private JTextField textPlate;
    private JComboBox<String> ComboType;
    private JComboBox<String> ComboSpots;
    private JTextArea textTicketOutput;

    public EntryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Vehicle Entry Station"));

        //Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        inputPanel.add(new JLabel("License Plate:"));
        textPlate = new JTextField();
        inputPanel.add(textPlate);

        //Vehicle Selection
        inputPanel.add(new JLabel("Vehicle Type:"));
        String[] types = {"Car", "Motorcycle", "SUV/Truck", "Handicapped Vehicle"};
        ComboType = new JComboBox<>(types);
        ComboType.insertItemAt("Select Vehicle Type:-", 0);
        ComboType.setSelectedIndex(0);
        inputPanel.add(ComboType);

        //Spot Selection
        inputPanel.add(new JLabel("Select Available Spots:"));
        ComboSpots = new JComboBox<>();
        inputPanel.add(ComboSpots);

        //Generate Ticket
        JButton buttonTicket = new JButton("Generate Ticket");
        inputPanel.add(new JLabel("")); 
        inputPanel.add(buttonTicket);
        
        add(inputPanel, BorderLayout.NORTH);

        // --- CENTER: Ticket Display ---
        textTicketOutput = new JTextArea();
        textTicketOutput.setEditable(false);
        textTicketOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textTicketOutput.setBorder(BorderFactory.createTitledBorder("Generated Ticket"));
        add(new JScrollPane(textTicketOutput), BorderLayout.CENTER);

        // --- LOGIC: Dynamic Spot Filtering ---
        // Requirement 1: Update available spots whenever the vehicle type changes
        ComboType.addActionListener(e -> updateAvailableSpots());
        buttonTicket.addActionListener(e -> processEntry());

        // Initial spot load
        updateAvailableSpots();
    }

    private void updateAvailableSpots() {
        ComboSpots.removeAllItems();
        String selectedType = (String) ComboType.getSelectedItem();
        
        // Create a dummy vehicle to test suitability
        Vehicle dummy = createVehicle("CHECK", selectedType);
        
        // Fetch all spots from Backend
        List<ParkingSpot> allSpots = ParkingLot.getInstance().getAllSpots();
        
        for (ParkingSpot s : allSpots) {
            // Requirement 1: Only show spots that are NOT occupied and are SUITABLE
            if (!s.isOccupied() && s.isSuitableFor(dummy)) {
                ComboSpots.addItem(s.getSpotID());
            }
        }
    }

    //Validation Check
    private void processEntry() {
        String plate = textPlate.getText().trim().toUpperCase();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "License plate cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE
                );
            return;
        }

        String selectedType = (String) ComboType.getSelectedItem();
        if (selectedType == null) { 
            JOptionPane.showMessageDialog(this, "No vehicle selected!", "Error", JOptionPane.ERROR_MESSAGE
                );
            return;
        }

        String selectedSpot = (String) ComboSpots.getSelectedItem();
        if (selectedSpot == null) {
            JOptionPane.showMessageDialog(this, "No suitable spot selected!", "Error", JOptionPane.ERROR_MESSAGE
                );
            return;
        }

        String type = (String) ComboType.getSelectedItem();
        Vehicle v = createVehicle(plate, type);

        // Requirement 3 & 4: Record entry and mark spot occupied
        Ticket ticket = ParkingLot.getInstance().parkVehicleAtSpot(selectedSpot, v);

        if (ticket != null) {
            // Requirement 5: Generate and display ticket in T-PLATE-TIMESTAMP format
            textTicketOutput.setText("=== PARKING TICKET ===\n" + ticket.getTicketDetails());
            textPlate.setText("");
            updateAvailableSpots(); // Refresh the list so the taken spot disappears
            JOptionPane.showMessageDialog(this, "Parking Successful! Ticket Generated.");
        } else {
            JOptionPane.showMessageDialog(this, "Parking failed. Please try again.");
        }
    }

    // Helper to handle Polymorphism (Car, Motorcycle, SUV)
    private Vehicle createVehicle(String plate, String type) {
        if (type.equals("Car")) return new Car(plate);
        if (type.equals("Motorcycle")) return new Motorcycle(plate);
        if (type.equals("SUV")) return new SUV(plate);
        return null;
    }
}