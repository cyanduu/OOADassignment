import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Ticket implements Serializable {
    private String ticketID;
    private String licensePlate;
    private String assignedSpotID;
    private String spotType;    // <--- NEW
    private String vehicleType; // <--- NEW
    private long entryTime;

    // Updated Constructor
    public Ticket(String licensePlate, String assignedSpotID, String spotType, String vehicleType, long entryTime) {
        this.licensePlate = licensePlate;
        this.assignedSpotID = assignedSpotID;
        this.spotType = spotType;       // <--- Save it
        this.vehicleType = vehicleType; // <--- Save it
        this.entryTime = entryTime;
        this.ticketID = generateID();
    }

    private String generateID() {
        // ID Format: T-PLATE-TIMESTAMP (e.g., T-W1234-202310270900)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        return "T-" + licensePlate + "-" + sdf.format(new Date(entryTime));
    }

    // --- GETTERS ---
    public String getTicketID() { return ticketID; }
    public String getSpotID() { return assignedSpotID; }
    public String getSpotType() { return spotType; }       // <--- NEW Getter
    public String getVehicleType() { return vehicleType; } // <--- NEW Getter
    public long getEntryTime() { return entryTime; }
    
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(new Date(entryTime));
    }

    public String getTicketDetails() {
        return "Ticket ID: " + ticketID + "\n" +
               "Spot: " + assignedSpotID + " (" + spotType + ")\n" +
               "Vehicle: " + vehicleType + "\n" +
               "Time: " + getFormattedTime();
    }
}