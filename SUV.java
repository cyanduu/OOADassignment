// Represents an SUV (Sports Utility Vehicle) in the parking system.
// Inherits from Vehicle. SUVs are larger and cannot fit in "Compact" or "Motorcycle" spots.
public class SUV extends Vehicle {

    // Constructs a new SUV instance with a unique license plate.
    public SUV(String licensePlate) {
        super(licensePlate);
    }

    // Returns the specific type "SUV".
    // Used to check if the vehicle fits in specific parking spots (e.g., Regular vs Compact).
    @Override
    public String getType() {
        return "SUV";
    }
}