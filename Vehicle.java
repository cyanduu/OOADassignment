import java.io.Serializable;

// Abstract base class for all vehicle types.
// Implements Serializable to allow vehicle data to be saved to a file.
public abstract class Vehicle implements Serializable {
    protected String licensePlate;
    protected long entryTime;

    public Vehicle(String licensePlate) {
        this.licensePlate = licensePlate;
        this.entryTime = System.currentTimeMillis(); // Capture entry time immediately
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public long getEntryTime() {
        return entryTime;
    }

    // Abstract method that must be implemented by subclasses (Car, Motorcycle, SUV).
    // Used to identify the vehicle type for parking rules and rates.
    public abstract String getType();
}