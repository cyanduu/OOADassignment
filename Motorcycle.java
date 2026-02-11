public class Motorcycle extends Vehicle {
    public Motorcycle(String licensePlate) {
        super(licensePlate);
    }

    @Override
    public String getType() {
        return "Motorcycle";
    }
}