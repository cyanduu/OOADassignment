public class SUV extends Vehicle {
    public SUV(String licensePlate) {
        super(licensePlate);
    }

    @Override
    public String getType() {
        return "SUV";
    }
}