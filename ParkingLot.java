import java.util.ArrayList;
import java.util.List;

public class ParkingLot {
    private static ParkingLot instance;
    private List<ParkingSpot> spots;

    private ParkingLot() {
        this.spots = new ArrayList<>();
    }

    public static ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }

    public void setSpots(List<ParkingSpot> newSpots) {
        this.spots = newSpots;
    }

    public List<ParkingSpot> getSpots() {
        return this.spots;
    }

    public void addSpot(ParkingSpot spot) {
        this.spots.add(spot);
    }
}