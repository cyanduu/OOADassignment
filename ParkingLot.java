import java.util.ArrayList;
import java.util.List;

// 1. Singleton Pattern: Ensures only ONE parking lot exists
// 2. Observer Pattern (Subject): Notifies Admin when things change
public class ParkingLot {
    private static ParkingLot instance;
    private List<ParkingObserver> observers = new ArrayList<>();
    private List<ParkingSpot> spots;
    private double totalRevenue = 0.0;

    // Private constructor (Singleton)
    private ParkingLot() {
    spots = new ArrayList<>();
    
    // Create a realistic mix of spots
    // 5 Motorcycle Spots (M-1 to M-5)
    for (int i = 1; i <= 5; i++) {
        spots.add(new ParkingSpot("M-" + i, "Motorcycle", 1.0)); // RM 1.00/hr
    }
    
    // 5 Compact Spots (C-1 to C-5) - For Cars only
    for (int i = 1; i <= 5; i++) {
        spots.add(new ParkingSpot("C-" + i, "Compact", 2.0)); // RM 2.00/hr
    }

    // 8 Regular Spots (R-1 to R-8) - For Cars and SUVs
    for (int i = 1; i <= 8; i++) {
        spots.add(new ParkingSpot("R-" + i, "Regular", 3.0)); // RM 3.00/hr
    }

    // 2 Handicapped Spots (H-1 to H-2) - Near entrance
    for (int i = 1; i <= 2; i++) {
        spots.add(new ParkingSpot("H-" + i, "Handicapped", 2.0)); // RM 2.00/hr
    }
}

    // Singleton Accessor
    public static synchronized ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }

    // --- Observer Pattern Methods ---
    public void addObserver(ParkingObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers() {
        for (ParkingObserver observer : observers) {
            observer.update();
        }
    }

    // --- Business Logic ---
    public void addRevenue(double amount) {
        this.totalRevenue += amount;
        notifyObservers(); // Trigger update!
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public int getOccupiedCount() {
        int count = 0;
        for (ParkingSpot spot : spots) {
            if (spot.isOccupied()) count++;
        }
        return count;
    }

    // Helper to get spots (Member 3 needs this)
    public List<ParkingSpot> getSpots() {
        return spots;
    }
}