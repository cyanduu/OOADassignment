// Represents a standard Car in the parking system.
// Inherits basic properties (license plate, entry time) from the abstract Vehicle class.
public class Car extends Vehicle {

    // Constructs a new Car instance with a unique license plate.
    public Car(String licensePlate) {
        super(licensePlate);
    }

    // Returns the specific type of this vehicle.
    // Used by the factory pattern and rate calculation logic.
    @Override
    public String getType() {
        return "Car";
    }
}