import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DataManager {
    // The name of the file where data will be stored
    private static final String PARKING_FILE = "parking_system_data.dat";
    private static final String FINES_FILE = "fines.dat";

    //SAVES the current state of all parking spots to the file.
    public static void saveState(List<ParkingSpot> spots) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(PARKING_FILE))) {
            out.writeObject(spots);
            System.out.println("System state successfully saved to " + PARKING_FILE);
        } catch (IOException e) {
            System.err.println("Error saving system state: " + e.getMessage());
        }
    }

    //LOADS the saved parking spots from the file.
    @SuppressWarnings("unchecked")
    public static List<ParkingSpot> loadState() {
        File file = new File(PARKING_FILE);
        
        // If it's the first time running the app, no file exists yet.
        if (!file.exists()) {
            System.out.println("No existing save file found. Starting with empty parking lot.");
            return new ArrayList<>(); 
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            List<ParkingSpot> loadedSpots = (List<ParkingSpot>) in.readObject();
            System.out.println("System state successfully loaded.");
            return loadedSpots;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading system state: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    //METHOD FOR FINES
    public static void saveFines(Map<String, Double> fines) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FINES_FILE))) {
            oos.writeObject(fines);
            System.out.println("Data Saved: Fines written to " + FINES_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Double> loadFines() {
        File file = new File(FINES_FILE);
        if (!file.exists()) return new HashMap<>(); // Return empty map if no file

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<String, Double>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

}