import java.util.HashMap;
import java.util.Map;

public class FineManager {

    // --- 1. DATA STORAGE (License Plate -> Unpaid Amount) ---
    // Stores debts if a customer leaves without paying the fine
    private static Map<String, Double> outstandingFines = new HashMap<>();

    // --- 2. CONFIGURATION ---
    public enum FineScheme { FIXED, PROGRESSIVE, HOURLY }
    
    // Default scheme (Admin can change this)
    private static FineScheme currentScheme = FineScheme.FIXED;

    public static void setFineScheme(FineScheme scheme) {
        currentScheme = scheme;
        System.out.println("System: Fine Scheme changed to " + scheme);
    }

    public static FineScheme getCurrentScheme() {
        return currentScheme;
    }

    // --- 3. CORE LOGIC ---

    /**
     * Calculates the standard parking fee (excluding fines).
     * Rule: Ceiling rounding (Round UP to nearest hour).
     */

    /**
     * Calculates fee with the Handicapped Exception.
     * @param hours Duration of stay
     * @param hourlyRate The spot's rate (e.g., RM 2.0)
     * @param spotType The type of spot ("Handicapped", "Regular", etc.)
     * @param hasHandicappedCard Boolean flag from UI
     */

    public static double calculateParkingFee(double hours, double hourlyRate, String spotType, boolean hasHandicappedCard) {
        // Requirement: FREE only if handicapped card holder parks in handicapped spot
        if (spotType.equalsIgnoreCase("Handicapped") && hasHandicappedCard) {
            return 0.0;
        }
        //standard ceiling rounding
        int roundedHours = (int) Math.ceil(hours);  
        if (roundedHours == 0) roundedHours = 1; // Minimum 1 hour
        return roundedHours * hourlyRate;
    }


    /**
     * Calculates ONLY the fine amount based on the rules.
     * Fines apply if:
     * a) Stay > 24 hours (Overstay)
     * b) Reserved spot violation
     */
    public static double calculateFine(double hours, boolean isReservedViolation) {
        boolean isOverstay = hours > 24.0;
        
        // If no rules broken, no fine.
        if (!isOverstay && !isReservedViolation) {
            return 0.0;
        }

        double fineAmount = 0.0;
        double overstayHours = (hours > 24) ? (hours - 24) : 0;

        switch (currentScheme) {
            case FIXED:
                // Option A: Flat RM 50 fine
                fineAmount = 50.0;
                break;

            case PROGRESSIVE:
                // Option B: Tiered fines
                if (hours <= 24) {
                    fineAmount = 50.0; // Reserved violation < 24h
                } else if (hours <= 48) {
                    fineAmount = 50.0 + 100.0; // First 24h + Next tier
                } else if (hours <= 72) {
                    fineAmount = 50.0 + 100.0 + 150.0;
                } else {
                    fineAmount = 50.0 + 100.0 + 150.0 + 200.0; // Max tier
                }
                break;

            case HOURLY:
                // Option C: RM 20 per hour for overstaying
                if (isReservedViolation) {
                    // If reserved violation, pay fine for WHOLE duration
                    fineAmount = Math.ceil(hours) * 20.0;
                } else {
                    // If just overstay, pay fine for EXCESS hours
                    fineAmount = Math.ceil(overstayHours) * 20.0;
                }
                break;
        }
        return fineAmount;
    }

    /**
     * MAIN METHOD called by ExitPanel.
     * Returns the total Bill (Parking + New Fine + Old Debt).
     */
    public static double calculateTotalDue(String plate, double hours, double hourlyRate, boolean isReservedViolation, String spotType, boolean hasHandicappedCard) {
        double parkingFee = calculateParkingFee(hours, hourlyRate, spotType, hasHandicappedCard);
        double newFine = calculateFine(hours, isReservedViolation);
        double oldDebt = outstandingFines.getOrDefault(plate, 0.0);

        return parkingFee + newFine + oldDebt;
    }

    // --- 4. DEBT MANAGEMENT (Linked to Plate) ---

    // Call this if the user pays successfully
    public static void clearFines(String plate) {
        if (outstandingFines.containsKey(plate)) {
            outstandingFines.remove(plate);
            System.out.println("System: Fines cleared for " + plate);
        }
    }

    // Call this if user leaves without paying (Deferred payment)
    public static void addFineToAccount(String plate, double amount) {
        double current = outstandingFines.getOrDefault(plate, 0.0);
        outstandingFines.put(plate, current + amount);
        System.out.println("System: Fine of RM " + amount + " added to account " + plate);
    }

    public static double getUnpaidFines(String plate) {
        return outstandingFines.getOrDefault(plate, 0.0);
    }

    // --- 5. VEHICLE SUITABILITY CHECK (Requirement) ---
    // Used by Member 1 (ParkingLot) or Member 3 (UI)
    public static boolean isVehicleAllowed(String spotType, String vehicleType) {
        if (spotType.equalsIgnoreCase("Motorcycle")) {
            return vehicleType.equalsIgnoreCase("Motorcycle");
        }
        if (spotType.equalsIgnoreCase("Compact")) {
            return vehicleType.equalsIgnoreCase("Car");
        }
        if (spotType.equalsIgnoreCase("Regular")) {
            // Allows Car AND SUV
            return vehicleType.equalsIgnoreCase("Car") || vehicleType.equalsIgnoreCase("SUV");
        }
        return true; // "Reserved" or "Handicapped" handled by Permit logic
    }
}