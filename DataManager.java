import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private static final String PARKING_FILE = "parking_system_data.dat";
    private static final String FINES_FILE = "fines.dat";
    private static final String REVENUE_FILE = "revenue.dat";

    public static void saveState(List<ParkingSpot> spots) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(PARKING_FILE))) {
            out.writeObject(spots);
            System.out.println("System: Parking state successfully saved.");
        } catch (IOException e) {
            System.err.println("Error saving parking state: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<ParkingSpot> loadState() {
        File file = new File(PARKING_FILE);
        
        if (!file.exists()) {
            System.out.println("System: No previous parking data found. Starting fresh.");
            return new ArrayList<>(); 
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            List<ParkingSpot> loadedSpots = (List<ParkingSpot>) in.readObject();
            System.out.println("System: Parking state successfully loaded.");
            return loadedSpots;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading parking state: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void saveFines(Map<String, Double> fines) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FINES_FILE))) {
            oos.writeObject(fines);
            System.out.println("System: Fine records saved.");
        } catch (IOException e) {
            System.err.println("Error saving fines: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Double> loadFines() {
        File file = new File(FINES_FILE);
        if (!file.exists()) return new HashMap<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<String, Double>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading fines: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static void saveHistory(List<Transaction> history) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(REVENUE_FILE))) {
            oos.writeObject(history);
            System.out.println("Data Saved: Transaction history written to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Transaction> loadHistory() {
        File file = new File(REVENUE_FILE);
        if (!file.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Transaction>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
}