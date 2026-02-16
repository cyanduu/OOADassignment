import java.util.HashMap;
import java.util.Map;

public class FineManager {

    // --- 1. DATA STORAGE ---
    // Stores debts (License Plate -> Unpaid Amount) if a customer leaves without paying
    private static Map<String, Double> outstandingFines = new HashMap<>();

    // --- 2. CONFIGURATION ---
    public enum FineScheme { FIXED, PROGRESSIVE, HOURLY }
    
    // Default scheme (Admin can change this via AdminPanel)
    private static FineScheme currentScheme = FineScheme.FIXED;

    public static void setFineScheme(FineScheme scheme) {
        currentScheme = scheme;
        System.out.println("System: Fine Scheme changed to " + scheme);
    }

    public static FineScheme getCurrentScheme() {
        return currentScheme;
    }

    // --- 3. CORE LOGIC ---

    // Calculates the standard parking fee based on duration and rate.
    // Implements ceiling rounding (e.g., 1.1 hours counts as 2 hours).
    public static double calculateParkingFee(double hours, double hourlyRate, String spotType, boolean hasHandicappedCard) {
        // Exemption: Free parking for valid permit holders in Handicapped spots
        if (spotType.equalsIgnoreCase("Handicapped") && hasHandicappedCard) {
            return 0.0;
        }
        
        // Standard calculation
        int roundedHours = (int) Math.ceil(hours);  
        if (roundedHours == 0) roundedHours = 1; // Minimum charge is 1 hour
        return roundedHours * hourlyRate;
    }

    // Calculates the fine amount based on the active scheme.
    // Fines trigger on Overstay (> 24 hours) or Reserved Spot Violations.
    public static double calculateFine(double hours, boolean isReservedViolation) {
        boolean isOverstay = hours > 24.0;
        
        // If no rules are broken, return 0
        if (!isOverstay && !isReservedViolation) {
            return 0.0;
        }

        double fineAmount = 0.0;
        double overstayHours = (hours > 24) ? (hours - 24) : 0;

        switch (currentScheme) {
            case FIXED:
                // Flat penalty for any violation
                fineAmount = 50.0;
                break;

            case PROGRESSIVE:
                // Tiered fines: The longer the duration, the higher the penalty
                if (hours <= 24) {
                    fineAmount = 50.0; // Base fine for violation within 24h
                } else if (hours <= 48) {
                    fineAmount = 50.0 + 100.0; // Tier 2
                } else if (hours <= 72) {
                    fineAmount = 50.0 + 100.0 + 150.0; // Tier 3
                } else {
                    fineAmount = 50.0 + 100.0 + 150.0 + 200.0; // Max Tier
                }
                break;

            case HOURLY:
                // Penalty charged per hour of violation
                if (isReservedViolation) {
                    // Violation: Charged for the entire duration
                    fineAmount = Math.ceil(hours) * 20.0;
                } else {
                    // Overstay: Charged only for excess hours
                    fineAmount = Math.ceil(overstayHours) * 20.0;
                }
                break;
        }
        return fineAmount;
    }

    // Helper to calculate total bill (Parking + New Fine + Previous Debts)
    public static double calculateTotalDue(String plate, double hours, double hourlyRate, boolean isReservedViolation, String spotType, boolean hasHandicappedCard) {
        double parkingFee = calculateParkingFee(hours, hourlyRate, spotType, hasHandicappedCard);
        double newFine = calculateFine(hours, isReservedViolation);
        double oldDebt = outstandingFines.getOrDefault(plate, 0.0);

        return parkingFee + newFine + oldDebt;
    }

    // --- 4. DEBT MANAGEMENT ---

    // Clears fines for a specific plate (used upon full payment)
    public static void clearFines(String plate) {
        if (outstandingFines.containsKey(plate)) {
            outstandingFines.remove(plate);
            System.out.println("System: Fines cleared for " + plate);
        }
    }

    // Adds a fine to the record (used when payment is deferred)
    public static void addFineToAccount(String plate, double amount) {
        double current = outstandingFines.getOrDefault(plate, 0.0);
        outstandingFines.put(plate, current + amount);
        System.out.println("System: Fine of RM " + amount + " recorded for " + plate);
    }

    public static double getUnpaidFines(String plate) {
        return outstandingFines.getOrDefault(plate, 0.0);
    }

    public static Map<String, Double> getAllOutstandingFines() {
        return new HashMap<>(outstandingFines);
    }

    public static void setOutstandingFines(Map<String, Double> loadedFines) {
        outstandingFines = loadedFines;
        System.out.println("System: Fines data loaded (" + loadedFines.size() + " records).");
    }

    // --- 5. VEHICLE SUITABILITY CHECK ---
    // Determines if a specific vehicle type is allowed to enter a specific spot type.
    public static boolean isVehicleAllowed(String spotType, String vehicleType) {
        // Motorcycle Spots: Strict (Cars cannot fit)
        if (spotType.equalsIgnoreCase("Motorcycle")) {
            return vehicleType.equalsIgnoreCase("Motorcycle");
        }

        // Compact Spots: Strict (Large vehicles cannot fit)
        if (spotType.equalsIgnoreCase("Compact")) {
            return vehicleType.equalsIgnoreCase("Motorcycle") || vehicleType.equalsIgnoreCase("Car");
        }

        // Regular Spots: No Motorcycles allowed (waste of space)
        if (spotType.equalsIgnoreCase("Regular")) {
            return !vehicleType.equalsIgnoreCase("Motorcycle");
        }

        // Reserved & Handicapped Spots: 
        // We allow all vehicles to 'select' them in the UI. 
        // Authorization is checked at Exit, where fines are applied if the permit is missing.
        if (spotType.equalsIgnoreCase("Reserved") || spotType.equalsIgnoreCase("Handicapped")) {
            return true; 
        }

        return true; 
    }
}