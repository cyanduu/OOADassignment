import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParkingLot implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // --- 1. SINGLETON PATTERN ---
    private static ParkingLot instance;

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

    // --- 2. DATA FIELDS ---
    private List<ParkingSpot> spots;
    private transient List<ParkingObserver> observers = new ArrayList<>();
    private double totalRevenue;

    // --- 3. INITIALIZE SPOTS (4 Floors) ---
    private void initializeSpots() {
        int floors = 4;
        int spotsPerFloor = 20;

        for (int f = 1; f <= floors; f++) {
            for (int s = 1; s <= spotsPerFloor; s++) {
                String id = String.format("F%d-S%02d", f, s);
                String type = "Regular";
                double rate = 5.0;

                if (f == 1) {
                    if (s <= 5) { type = "Handicapped"; rate = 2.0; } 
                    else { type = "Reserved"; rate = 10.0; }
                } else if (f == 4) {
                    type = "Compact"; rate = 2.0;
                }

                spots.add(new ParkingSpot(id, type, rate));
            }
        }
    }

    // --- 4. DATA ACCESS METHODS ---
    public void setSpots(List<ParkingSpot> newSpots) {
        this.spots = newSpots;
    }

    public List<ParkingSpot> getSpots() {
        if (this.spots == null) this.spots = new ArrayList<>();
        return this.spots;
    }

    // Alias method to prevent errors if team members use "getAllSpots"
    public List<ParkingSpot> getAllSpots() {
        return getSpots();
    }

    public void addSpot(ParkingSpot spot) {
        getSpots().add(spot);
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void addRevenue(double amount) {
        this.totalRevenue += amount;
        notifyObservers();
    }

    // --- 5. OBSERVER PATTERN ---
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

    // --- 6. CORE LOGIC ---
    
    // Auto-park (for testing)
    public Ticket parkVehicle(Vehicle v) {
        ParkingSpot spot = findAvailableSpot(v);
        if (spot != null) {
            spot.park(v);
            
            // UPDATE 1: Passing extra details to Ticket
            Ticket ticket = new Ticket(
                v.getLicensePlate(), 
                spot.getSpotID(), 
                spot.getType(),      // Spot Type
                v.getType(),         // Vehicle Type
                v.getEntryTime()
            );
            
            notifyObservers();
            return ticket;
        }
        return null;
    }

    // Manual park (for EntryPanel)
    public Ticket parkVehicleAtSpot(String spotID, Vehicle v) {
        for (ParkingSpot spot : getSpots()) {
            if (spot.getSpotID().equals(spotID)) {
                if (!spot.isOccupied() && spot.isSuitableFor(v)) {
                    spot.park(v);
                    
                    // UPDATE 2: Passing extra details to Ticket
                    Ticket ticket = new Ticket(
                        v.getLicensePlate(), 
                        spot.getSpotID(), 
                        spot.getType(),      // Spot Type
                        v.getType(),         // Vehicle Type
                        v.getEntryTime()
                    );
                    
                    notifyObservers();
                    return ticket;
                }
            }
        }
        return null;
    }

    // Find spot by plate (for ExitPanel)
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

    // Internal helper
    private ParkingSpot findAvailableSpot(Vehicle v) {
        for (ParkingSpot spot : getSpots()) {
            if (!spot.isOccupied() && spot.isSuitableFor(v)) {
                return spot;
            }
        }
        return null;
    }
    
    public void removeVehicle(String spotID) {
        for (ParkingSpot s : getSpots()) {
            if (s.getSpotID().equals(spotID)) {
                s.removeVehicle();
                notifyObservers();
                break;
            }
        }
    }

}