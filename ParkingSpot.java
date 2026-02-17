
import java.io.Serializable;

public class ParkingSpot implements Serializable {
    private static final long serialVersionUID = 1L;

    private String spotID;      
    private String type;        
    private boolean isOccupied;
    private Vehicle currentVehicle;
    private double hourlyRate;

    public ParkingSpot(String spotID, String type, double hourlyRate) {
        this.spotID = spotID;
        this.type = type;
        this.hourlyRate = hourlyRate;
        this.isOccupied = false;
    }

    public boolean isSuitableFor(Vehicle v) {
        if (v == null) return false;
        return FineManager.isVehicleAllowed(this.type, v.getType());
    }

    //Parking process
    public void park(Vehicle v) {
        this.currentVehicle = v;
        this.isOccupied = true;
    }

    public void removeVehicle() {
        this.currentVehicle = null;
        this.isOccupied = false;
    }

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

    public Vehicle getVehicle() {
        return this.currentVehicle;
    }
}