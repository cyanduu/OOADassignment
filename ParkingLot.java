import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Main system class managing all parking spots.
// Implements Singleton pattern to ensure only one parking lot exists.
public class ParkingLot implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // --- SINGLETON INSTANCE ---
    private static ParkingLot instance;

    // --- DATA FIELDS ---
    private List<ParkingSpot> spots;
    private double totalRevenue;
    
    // Observers list is transient so it is NOT saved to the file (avoids serialization errors)
    private transient List<ParkingObserver> observers = new ArrayList<>();

    private ParkingLot() {
        this.spots = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.totalRevenue = 0.0;
        initializeSpots();
    }

    public static ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }

    // --- INITIALIZATION ---
    // Sets up the 4 floors with specific spot types and rates.
    private void initializeSpots() {
        int floors = 4;
        int spotsPerFloor = 20;

        for (int f = 1; f <= floors; f++) {
            for (int s = 1; s <= spotsPerFloor; s++) {
                String id = String.format("F%d-S%02d", f, s);
                String type = "Regular";
                double rate = 5.0;

                // FLOOR 1: VIP & HANDICAPPED
                if (f == 1) {
                    if (s <= 15) { 
                        // S01 - S15: VIP Reserved
                        type = "Reserved"; 
                        rate = 10.0; 
                    } else { 
                        // S16 - S20: Handicapped
                        type = "Handicapped"; 
                        rate = 2.0; 
                    }
                } 
                // FLOOR 4: COMPACT
                else if (f == 4) {
                    type = "Compact"; 
                    rate = 2.0;
                }
                // FLOORS 2 & 3: STANDARD (Default)

                spots.add(new ParkingSpot(id, type, rate));
            }
        }
    }

    // --- DATA ACCESS ---

    public void setSpots(List<ParkingSpot> newSpots) {
        this.spots = newSpots;
    }

    public List<ParkingSpot> getSpots() {
        if (this.spots == null) this.spots = new ArrayList<>();
        return this.spots;
    }

    public List<ParkingSpot> getAllSpots() {
        return getSpots();
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void addRevenue(double amount) {
        this.totalRevenue += amount;
        notifyObservers();
    }

    // --- OBSERVER PATTERN ---
    // Allows UI components (like AdminPanel) to auto-refresh when data changes.
    public void addObserver(ParkingObserver obs) {
        if (observers == null) observers = new ArrayList<>();
        observers.add(obs);
    }

    private void notifyObservers() {
        if (observers == null) return;
        for (ParkingObserver obs : observers) {
            obs.onParkingDataChanged();
        }
    }

    // --- CORE LOGIC: PARKING ---

    // Manual Park: User selects a specific spot ID (EntryPanel)
    public Ticket parkVehicleAtSpot(String spotID, Vehicle v) {
        for (ParkingSpot spot : getSpots()) {
            if (spot.getSpotID().equals(spotID)) {
                // Check if spot is empty and if vehicle fits (or is allowed via policy)
                if (!spot.isOccupied() && spot.isSuitableFor(v)) {
                    spot.park(v);
                    
                    Ticket ticket = new Ticket(
                        v.getLicensePlate(), 
                        spot.getSpotID(), 
                        spot.getType(), 
                        v.getType(), 
                        v.getEntryTime()
                    );
                    
                    notifyObservers();
                    return ticket;
                }
            }
        }
        return null;
    }

    // Auto Park: Finds the first available spot (for testing or simulation)
    public Ticket parkVehicle(Vehicle v) {
        ParkingSpot spot = findAvailableSpot(v);
        if (spot != null) {
            spot.park(v);
            
            Ticket ticket = new Ticket(
                v.getLicensePlate(), 
                spot.getSpotID(), 
                spot.getType(), 
                v.getType(), 
                v.getEntryTime()
            );
            
            notifyObservers();
            return ticket;
        }
        return null;
    }

    // --- CORE LOGIC: EXIT & UTILS ---

    // Finds a parking spot containing a specific license plate
    public ParkingSpot findSpotByPlate(String plate) {
        for (ParkingSpot spot : getSpots()) {
            if (spot.isOccupied() && spot.getCurrentVehicle() != null) {
                if (spot.getCurrentVehicle().getLicensePlate().equalsIgnoreCase(plate)) {
                    return spot;
                }
            }
        }
        return null;
    }

    // Removes a vehicle from a spot (ExitPanel)
    public void removeVehicle(String spotID) {
        for (ParkingSpot s : getSpots()) {
            if (s.getSpotID().equals(spotID)) {
                s.removeVehicle();
                notifyObservers();
                break;
            }
        }
    }

    // Internal helper to find first suitable spot
    private ParkingSpot findAvailableSpot(Vehicle v) {
        for (ParkingSpot spot : getSpots()) {
            if (!spot.isOccupied() && spot.isSuitableFor(v)) {
                return spot;
            }
        }
        return null;
    }
}