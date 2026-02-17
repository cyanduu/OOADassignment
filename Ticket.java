
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Ticket implements Serializable {
    private String ticketID;
    private String licensePlate;
    private String assignedSpotID;
    private String spotType;    
    private String vehicleType; 
    private long entryTime;

    public Ticket(String licensePlate, String assignedSpotID, String spotType, String vehicleType, long entryTime) {
        this.licensePlate = licensePlate;
        this.assignedSpotID = assignedSpotID;
        this.spotType = spotType;      
        this.vehicleType = vehicleType; 
        this.entryTime = entryTime;
        this.ticketID = generateID();
    }

    //generate ticket
    private String generateID() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        return "T-" + licensePlate + "-" + sdf.format(new Date(entryTime));
    }

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