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
        String vType = v.getType(); // "Car", "Motorcycle", "SUV", "Bicycle", "VIP"

        switch (this.type) {
            case "Compact":
                // Requirement: For small vehicles (motorcycles, bicycles)
                return vType.equalsIgnoreCase("Motorcycle") || vType.equalsIgnoreCase("Bicycle");

            case "Regular":
                // Requirement: For regular cars (and usually SUVs fit here too)
                return vType.equalsIgnoreCase("Car") || vType.equalsIgnoreCase("SUV");

            case "Handicapped":
                // Requirement: Reserved for handicapped.
                // Physically, any car fits, but logically only those with permits.
                // We allow all to "fit" physically, but billing handles the "Free" part.
                return true; 

            case "Reserved":
                // Requirement: For VIP customers
                // Only allow if the vehicle is explicitly marked as VIP/Reserved
                return vType.equalsIgnoreCase("VIP");

            default:
                return false;
        }
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