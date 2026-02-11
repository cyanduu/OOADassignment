
import java.io.Serializable;

public abstract class Vehicle implements Serializable {
    protected String licensePlate;
    protected long entryTime;

    public Vehicle(String licensePlate) {
        this.licensePlate = licensePlate;
        this.entryTime = System.currentTimeMillis(); // Records exact time of entry
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public long getEntryTime() {
        return entryTime;
    }

    // Abstract method: forces subclasses to define their own type
    public abstract String getType();
}