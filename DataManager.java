import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    // The name of the file where data will be stored
    private static final String FILE_NAME = "parking_system_data.dat";

    /**
     * SAVES the current state of all parking spots to the file.
     * Member 4 (System Integrator) will call this when the app closes or a car parks.
     */
    public static void saveState(List<ParkingSpot> spots) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(spots);
            System.out.println("System state successfully saved to " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("Error saving system state: " + e.getMessage());
        }
    }

    /**
     * LOADS the saved parking spots from the file.
     * Member 4 will call this right when the application starts up.
     */
    @SuppressWarnings("unchecked")
    public static List<ParkingSpot> loadState() {
        File file = new File(FILE_NAME);
        
        // If it's the first time running the app, no file exists yet.
        if (!file.exists()) {
            System.out.println("No existing save file found. Starting with empty parking lot.");
            return new ArrayList<>(); 
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            List<ParkingSpot> loadedSpots = (List<ParkingSpot>) in.readObject();
            System.out.println("System state successfully loaded.");
            return loadedSpots;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading system state: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}