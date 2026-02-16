// Represents a Motorcycle in the parking system.
// Inherits from Vehicle to share common properties like license plate and entry time.
public class Motorcycle extends Vehicle {

    // Constructs a new Motorcycle instance.
    public Motorcycle(String licensePlate) {
        super(licensePlate);
    }

    // Returns the specific type "Motorcycle".
    // This is used to determine parking rates (cheaper) and allowed spots (Compact/Motorcycle only).
    @Override
    public String getType() {
        return "Motorcycle";
    }
}