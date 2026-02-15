import java.io.Serializable;

public class ParkingSpot implements Serializable {
    private String spotID;      // e.g., "F1-01"
    private String type;        // "Compact", "Regular", "Motorcycle", "Handicapped"
    private boolean isOccupied;
    private Vehicle currentVehicle;
    private double hourlyRate;

    public ParkingSpot(String spotID, String type, double hourlyRate) {
        this.spotID = spotID;
        this.type = type;
        this.hourlyRate = hourlyRate;
        this.isOccupied = false;
    }

    // --- LOGIC: CHECK IF VEHICLE FITS ---
    public boolean isSuitableFor(Vehicle v) {
        if (v == null) return false;
        
        // OPTION 1: The Best Way (Link to Member 2's Logic)
        // This ensures both files always agree.
        return FineManager.isVehicleAllowed(this.type, v.getType());

        /* // OPTION 2: The Manual Fix (If you don't want to link files)
        // Just un-comment this if Option 1 gives errors.
        
        String vType = v.getType(); 

        switch (this.type) {
            case "Compact":
                return vType.equalsIgnoreCase("Motorcycle") || vType.equalsIgnoreCase("Bicycle");

            case "Regular":
                return vType.equalsIgnoreCase("Car") || vType.equalsIgnoreCase("SUV") || vType.equalsIgnoreCase("VIP Car");

            case "Handicapped":
                return true; // Visible to all (Honor system)

            case "Reserved":
                return true; // <--- CHANGED: Now visible to all cars!

            default:
                return false;
        }
        */
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

    // CRITICAL: Logic to check if a vehicle fits in this spot
    /*public boolean isSuitableFor(Vehicle v) {
        String vType = v.getType();

        // 1. Motorcycle spots are ONLY for Motorcycles
        if (this.type.equals("Motorcycle")) {
            return vType.equals("Motorcycle");
        }

        // 2. Compact spots are ONLY for Cars (Motorcycles/SUVs usually don't park here in this logic)
        if (this.type.equals("Compact")) {
            return vType.equals("Car");
        }

        // 3. Regular spots fit Cars and SUVs
        if (this.type.equals("Regular")) {
            return vType.equals("Car") || vType.equals("SUV");
        }
        
        // 4. Handicapped spots (Simplified: assuming any vehicle with permit, handled by UI logic)
        // For now, allow any vehicle type if the spot is Handicapped, 
        // assuming the UI checked for the permit.
        return true; 
    }*/

    public void park(Vehicle v) {
        this.currentVehicle = v;
        this.isOccupied = true;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    public Vehicle getVehicle() {
        return this.currentVehicle;
    }

    public void removeVehicle() {
        this.currentVehicle = null;
        this.isOccupied = false;
    }
}