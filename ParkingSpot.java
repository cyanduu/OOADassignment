import java.io.Serializable;

// Represents a physical parking spot in the parking lot.
// Implements Serializable to allow saving the state to a file.
public class ParkingSpot implements Serializable {
    private static final long serialVersionUID = 1L;

    private String spotID;      // Unique Identifier (e.g., "F1-S01")
    private String type;        // "Compact", "Regular", "Reserved", "Handicapped"
    private boolean isOccupied;
    private Vehicle currentVehicle;
    private double hourlyRate;

    public ParkingSpot(String spotID, String type, double hourlyRate) {
        this.spotID = spotID;
        this.type = type;
        this.hourlyRate = hourlyRate;
        this.isOccupied = false;
    }

    // --- LOGIC: CHECK IF VEHICLE FITS ---
    // Delegates the logic to FineManager to ensure consistency across the system.
    // This checks if the vehicle type (e.g., "Car") is allowed in this spot type (e.g., "Compact").
    public boolean isSuitableFor(Vehicle v) {
        if (v == null) return false;
        return FineManager.isVehicleAllowed(this.type, v.getType());
    }

    // --- PARKING ACTIONS ---

    public void park(Vehicle v) {
        this.currentVehicle = v;
        this.isOccupied = true;
    }

    public void removeVehicle() {
        this.currentVehicle = null;
        this.isOccupied = false;
    }

    // --- GETTERS ---

    public boolean isOccupied() {
        return isOccupied;
    }

    public String getSpotID() {
        return spotID;
    }

    public String getType() {
        return type;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    // Alias method for compatibility with other modules
    public Vehicle getVehicle() {
        return this.currentVehicle;
    }
}