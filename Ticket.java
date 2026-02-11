public class Ticket {
    private String ticketID;
    private String licensePlate;
    private String assignedSpotID;
    private long entryTime;

    public Ticket(String licensePlate, String assignedSpotID, long entryTime) {
        this.licensePlate = licensePlate;
        this.assignedSpotID = assignedSpotID;
        this.entryTime = entryTime;
        this.ticketID = generateID();
    }

    private String generateID() {
        // Creates ID like: T-W1234-17098822
        return "T-" + licensePlate + "-" + entryTime;
    }

    public String getTicketDetails() {
        return "Ticket ID: " + ticketID + "\n" +
               "Spot: " + assignedSpotID + "\n" +
               "Time: " + new java.util.Date(entryTime);
    }
}