import java.io.Serializable;

public class ParkingSpot implements Serializable {
    private static final long serialVersionUID = 1L; // Add Version ID

    private String spotID;
    private String type; // "Compact", "Regular", "Handicapped", "Reserved" 
    private boolean isOccupied;
    private Vehicle currentVehicle;
    private double hourlyRate;

    public ParkingSpot(String spotID, String type, double hourlyRate) {
        this.spotID = spotID;
        this.type = type;
        this.hourlyRate = hourlyRate;
        this.isOccupied = false;
    }

    public boolean isOccupied() { return isOccupied; }
    public String getSpotID() { return spotID; }
    public String getType() { return type; }
    public Vehicle getCurrentVehicle() { return currentVehicle; } // Needed for AdminPanel

    // CRITICAL: Logic fixed to match Assignment Page 2 [cite: 57-60]
    public boolean isSuitableFor(Vehicle v) {
        String vType = v.getType(); // Assumes Vehicle class returns "Car", "Motorcycle", "SUV"

        // Rule 1: Handicapped Spots
        // Technically only for Handicapped Vehicles (checked via permit in UI)
        if (this.type.equals("Handicapped")) {
            return vType.equals("Handicapped Vehicle"); 
        }

        // Rule 2: Compact Spots [cite: 42, 57, 58]
        // "Compact: For small vehicles (motorcycles, bicycles)" AND "Car - Can park in Compact"
        if (this.type.equals("Compact")) {
            return vType.equals("Motorcycle") || vType.equals("Car");
        }

        // Rule 3: Regular Spots [cite: 58, 59]
        // "Regular: For regular cars" AND "SUV - Can park in Regular"
        if (this.type.equals("Regular")) {
            return vType.equals("Car") || vType.equals("SUV");
        }

        // Rule 4: Reserved Spots [cite: 44]
        // Usually requires specific permission, but physically any car/SUV fits.
        // We will allow logic to pass here, but UI should check for "VIP Status".
        if (this.type.equals("Reserved")) {
            return !vType.equals("Motorcycle"); // Assuming bikes don't use VIP spots
        }

        return false; 
    }

    public void park(Vehicle v) {
        this.currentVehicle = v;
        this.isOccupied = true;
    }

    public void removeVehicle() {
        this.currentVehicle = null;
        this.isOccupied = false;
    }
}