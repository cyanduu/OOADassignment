// Represents a Compact in the parking system.
// Inherits from Vehicle to share common properties like license plate and entry time.
public class Compact extends Vehicle {

    // Constructs a new Compact instance.
    public Compact(String licensePlate) {
        super(licensePlate);
    }

    // Returns the specific type "Compact".
    // This is used to determine parking rates (cheaper) and allowed spots (Compact/Motorcycle only).
    @Override
    public String getType() {
        return "Motorcycle";
    }
}