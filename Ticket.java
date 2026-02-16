import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

// Represents a parking ticket issued to a vehicle upon entry.
// Stores details about the vehicle, assigned spot, and entry timestamp.
public class Ticket implements Serializable {
    private String ticketID;
    private String licensePlate;
    private String assignedSpotID;
    private String spotType;    
    private String vehicleType; 
    private long entryTime;

    // Constructor to initialize ticket details and generate a unique ID.
    public Ticket(String licensePlate, String assignedSpotID, String spotType, String vehicleType, long entryTime) {
        this.licensePlate = licensePlate;
        this.assignedSpotID = assignedSpotID;
        this.spotType = spotType;      
        this.vehicleType = vehicleType; 
        this.entryTime = entryTime;
        this.ticketID = generateID();
    }

    // Generates a unique Ticket ID based on license plate and timestamp.
    // Format: T-[PLATE]-YYYYMMDDHHMM
    private String generateID() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        return "T-" + licensePlate + "-" + sdf.format(new Date(entryTime));
    }

    // --- GETTERS ---

    public String getTicketID() { 
        return ticketID; 
    }

    public String getSpotID() { 
        return assignedSpotID; 
    }

    public String getSpotType() { 
        return spotType; 
    }

    public String getVehicleType() { 
        return vehicleType; 
    }

    public long getEntryTime() { 
        return entryTime; 
    }
    
    // Returns the entry time formatted as a readable string.
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(new Date(entryTime));
    }

    // Returns a summary string useful for printing or displaying in dialogs.
    public String getTicketDetails() {
        return "Ticket ID: " + ticketID + "\n" +
               "Spot: " + assignedSpotID + " (" + spotType + ")\n" +
               "Vehicle: " + vehicleType + "\n" +
               "Time: " + getFormattedTime();
    }
}