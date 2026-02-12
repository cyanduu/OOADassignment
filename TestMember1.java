import java.util.List;

public class TestMember1 {
    public static void main(String[] args) {
        // 1. Load Data
        List<ParkingSpot> savedSpots = DataManager.loadState();
        ParkingLot.getInstance().setSpots(savedSpots);

        // 2. If empty, add test data
        if (ParkingLot.getInstance().getSpots().isEmpty()) {
            System.out.println("Creating new spots...");
            ParkingLot.getInstance().addSpot(new ParkingSpot("A1", "Compact", 2.0));
            ParkingLot.getInstance().addSpot(new ParkingSpot("B1", "Motorcycle", 1.0));
        }

        // 3. Print status
        System.out.println("Current Spots in System:");
        for (ParkingSpot s : ParkingLot.getInstance().getSpots()) {
            System.out.println("- " + s.getSpotID() + " [" + s.getType() + "]");
        }

        // 4. Save Data
        DataManager.saveState(ParkingLot.getInstance().getSpots());
    }
}