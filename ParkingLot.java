import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParkingLot implements Serializable {
    private static final long serialVersionUID = 1L;
    
    //singleton instance
    private static ParkingLot instance;

    //data fields
    private List<ParkingSpot> spots;
    private double totalRevenue;
    private transient List<ParkingObserver> observers = new ArrayList<>();
    private List<Transaction> transactionHistory;

    private ParkingLot() {
        this.spots = new ArrayList<>();
        this.transactionHistory = new ArrayList<>();
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

    //initialize floors and spots
    private void initializeSpots() {
        int floors = 4;
        int spotsPerFloor = 20;

        for (int f = 1; f <= floors; f++) {
            for (int s = 1; s <= spotsPerFloor; s++) {
                String id = String.format("F%d-S%02d", f, s);
                String type = "Regular";
                double rate = 5.0;

                //FLOOR 1: VIP & HANDICAPPED
                if (f == 1) {
                    if (s <= 15) { 
                        //S01 - S15: VIP Reserved
                        type = "Reserved"; 
                        rate = 10.0; 
                    } else { 
                        //S16 - S20: Handicapped
                        type = "Handicapped"; 
                        rate = 2.0; 
                    }
                } 
                //FLOOR 4: COMPACT
                else if (f == 4) {
                    type = "Compact"; 
                    rate = 2.0;
                }
                //FLOORS 2 & 3: STANDARD (Default)

                spots.add(new ParkingSpot(id, type, rate));
            }
        }
    }

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

    public void addTransaction(Transaction t) {
        if (transactionHistory == null) transactionHistory = new ArrayList<>();
        transactionHistory.add(t);
        notifyObservers();
    }

    public double getTotalRevenue() {
        if (transactionHistory == null) return 0.0;
        
        //total up transaction
        double sum = 0;
        for (Transaction t : transactionHistory) {
            sum += t.getAmount();
        }
        return sum;
    }

    public List<Transaction> getHistory() {
        if (transactionHistory == null) transactionHistory = new ArrayList<>();
        return transactionHistory;
    }
    
    public void setHistory(List<Transaction> loadedHistory) {
        this.transactionHistory = loadedHistory;
    }

    public void addRevenue(double amount) {
        this.totalRevenue += amount;
        notifyObservers();
    }

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

    //user select spot
    public Ticket parkVehicleAtSpot(String spotID, Vehicle v) {
        for (ParkingSpot spot : getSpots()) {
            if (spot.getSpotID().equals(spotID)) {
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

    //Exit
    //Finds a parking spot containing a specific license plate
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

    public void removeVehicle(String spotID) {
        for (ParkingSpot s : getSpots()) {
            if (s.getSpotID().equals(spotID)) {
                s.removeVehicle();
                notifyObservers();
                break;
            }
        }
    }

    private ParkingSpot findAvailableSpot(Vehicle v) {
        for (ParkingSpot spot : getSpots()) {
            if (!spot.isOccupied() && spot.isSuitableFor(v)) {
                return spot;
            }
        }
        return null;
    }
}