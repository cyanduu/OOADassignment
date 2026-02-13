import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParkingLot implements Serializable {
    // --- 1. SINGLETON PATTERN (The missing part!) ---
    private static final long serialVersionUID = 1L;
    private static ParkingLot instance;

    // Private constructor so no one else can use "new ParkingLot()"
    private ParkingLot() {
        this.spots = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.totalRevenue = 0.0;
        initializeSpots();
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
    private transient List<ParkingObserver> observers = new ArrayList<>();
    private double totalRevenue;

    // --- LOGIC: INITIALIZE 4 FLOORS ---
    private void initializeSpots() {
        int floors = 4; // UPDATED: 4 Floors
        int spotsPerFloor = 20;

        for (int f = 1; f <= floors; f++) {
            for (int s = 1; s <= spotsPerFloor; s++) {
                String id = String.format("F%d-S%02d", f, s);
                String type = "Regular";
                double rate = 5.0;

                // --- FLOOR 1: VIP & ACCESSIBILITY ---
                if (f == 1) {
                    if (s <= 5) { 
                        type = "Handicapped"; 
                        rate = 2.0; 
                    } else {
                        type = "Reserved"; 
                        rate = 10.0;
                    }
                }
                
                // --- FLOOR 2: REGULAR ---
                else if (f == 2) {
                    type = "Regular";
                    rate = 5.0;
                }
                
                // --- FLOOR 3: REGULAR (Extra Capacity) ---
                else if (f == 3) {
                    type = "Regular";
                    rate = 5.0;
                }

                // --- FLOOR 4: COMPACT (Small Vehicles) ---
                else if (f == 4) {
                    type = "Compact";
                    rate = 2.0;
                }

                spots.add(new ParkingSpot(id, type, rate));
            }
        }
    }

    // --- 3. DATA METHODS ---
    public void setSpots(List<ParkingSpot> newSpots) {
        this.spots = newSpots;
    }

    public List<ParkingSpot> getSpots() {
        if (this.spots == null) this.spots = new ArrayList<>();
        return this.spots;
    }

    /*public List<ParkingSpot> getAllSpots() {
        // Safety check to avoid crashes if spots is null
        if (this.spots == null) {
            this.spots = new ArrayList<>();
        }
        return this.spots;
    }*/

    // This method is needed for your TestMember1 to work
    public void addSpot(ParkingSpot spot) {
        if (this.spots == null) {
            this.spots = new ArrayList<>();
        }
        this.spots.add(spot);
    }

    // --- 4. OBSERVER & LOGIC ---
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
        // Simple search: Find first empty spot that fits the vehicle
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
