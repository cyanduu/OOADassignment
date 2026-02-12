import java.util.ArrayList;
import java.util.List;

public class ParkingLot {
    // --- 1. SINGLETON PATTERN (The missing part!) ---
    private static ParkingLot instance;

    // Private constructor so no one else can use "new ParkingLot()"
    private ParkingLot() {
        this.spots = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.totalRevenue = 0.0;
    }

    // This is the method your error says is "undefined"
    public static ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }

    // --- 2. DATA FIELDS ---
    private List<ParkingSpot> spots;
    private List<ParkingObserver> observers;
    private double totalRevenue;

    // --- 3. DATA METHODS ---
    public void setSpots(List<ParkingSpot> newSpots) {
        this.spots = newSpots;
    }

    public List<ParkingSpot> getAllSpots() {
        // Safety check to avoid crashes if spots is null
        if (this.spots == null) {
            this.spots = new ArrayList<>();
        }
        return this.spots;
    }

    // This method is needed for your TestMember1 to work
    public void addSpot(ParkingSpot spot) {
        if (this.spots == null) {
            this.spots = new ArrayList<>();
        }
        this.spots.add(spot);
    }

    // --- 4. OBSERVER & LOGIC ---
    public void addObserver(ParkingObserver observer) {
        this.observers.add(observer);
    }

    private void notifyObservers() {
        for (ParkingObserver observer : observers) {
            observer.onParkingDataChanged();
        }
    }

    public void addRevenue(double amount) {
        this.totalRevenue += amount;
        notifyObservers();
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
    
    // Core parking logic
    public Ticket parkVehicle(Vehicle v) {
        ParkingSpot spot = findAvailableSpot(v);
        if (spot != null) {
            spot.park(v);
            Ticket ticket = new Ticket(v.getLicensePlate(), spot.getSpotID(), v.getEntryTime());
            notifyObservers();
            return ticket;
        }
        return null;
    }

    private ParkingSpot findAvailableSpot(Vehicle v) {
        if (spots == null) return null;
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied() && spot.isSuitableFor(v)) {
                return spot;
            }
        }
        return null;
    }
}