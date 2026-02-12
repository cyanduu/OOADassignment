import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParkingLot implements Serializable {
    // 1. Version ID for File Saving (Prevents crashes when code changes) [cite: 148-151]
    private static final long serialVersionUID = 1L;

    // --- Member 1: Data Section ---
    private List<ParkingSpot> spots;
    private double totalRevenue;
    
    // --- Member 4: Observer Pattern Section ---
    // 'transient' is CRITICAL. It tells Java: "Save the revenue and spots to the file, 
    // but DO NOT try to save the Admin Panel window." [cite: 159-160]
    private transient List<ParkingObserver> observers = new ArrayList<>();

    // Constructor: Creates the parking lot structure
    public ParkingLot(int numFloors, int spotsPerFloor) {
        this.spots = new ArrayList<>();
        this.totalRevenue = 0.0;
        
        // Initialize Spots (e.g., F1-R1-S1) [cite: 46]
        // This loop creates the physical layout of your lot
        for (int f = 1; f <= numFloors; f++) {
            for (int s = 1; s <= spotsPerFloor; s++) {
                String id = String.format("F%d-R1-S%d", f, s);
                
                // Example logic: First 5 spots are Compact, next 5 Regular, etc. [cite: 42-44]
                String type = "Regular"; 
                double rate = 5.0;
                
                if (s <= 5) { type = "Compact"; rate = 2.0; }
                else if (s > 15) { type = "Reserved"; rate = 10.0; }
                
                spots.add(new ParkingSpot(id, type, rate));
            }
        }
    }

    // --- Observer Pattern Logic (Member 4) ---

    public void addObserver(ParkingObserver obs) {
        // Safety Check: When loading from a file, 'transient' fields come back as NULL.
        // We must re-create the list to prevent a NullPointerException.
        if (observers == null) {
            observers = new ArrayList<>();
        }
        observers.add(obs);
    }

    public void notifyObservers() {
        // If nobody is listening (or list is null), do nothing
        if (observers == null) return;
        
        for (ParkingObserver obs : observers) {
            obs.onParkingDataChanged();
        }
    }

    // --- Business Logic (Triggers) ---

    // Called when a vehicle pays (Member 3 - Exit Panel)
    public void addRevenue(double amount) {
        this.totalRevenue += amount;
        notifyObservers(); // Automatically updates the Admin Panel!
    }

    // Called when a vehicle enters (Member 3 - Entry Panel)
    public boolean parkVehicle(String spotID, Vehicle vehicle) {
        for (ParkingSpot s : spots) {
            if (s.getSpotID().equals(spotID) && !s.isOccupied()) {
                s.park(vehicle);
                notifyObservers(); // Notify Admin Panel to decrease vacancy count
                return true;
            }
        }
        return false;
    }

    // Called when a vehicle exits
    // Inside ParkingLot.java
public void removeVehicle(String spotID) {
    for (ParkingSpot s : spots) {
        if (s.getSpotID().equals(spotID)) {
            s.removeVehicle();
            notifyObservers();
            break;
        }
    }
}
    
    // --- Getters for Reports ---
    
    public double getTotalRevenue() { 
        return totalRevenue; 
    }
    
    public List<ParkingSpot> getAllSpots() { 
        return spots; 
    }
}