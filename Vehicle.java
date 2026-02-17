
import java.io.Serializable;

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

    public abstract String getType();
}