import java.util.HashMap;
import java.util.Map;

public class FineManager {

    private static Map<String, Double> outstandingFines = new HashMap<>();
    public enum FineScheme { FIXED, PROGRESSIVE, HOURLY }
    private static FineScheme currentScheme = FineScheme.FIXED;

    public static void setFineScheme(FineScheme scheme) {
        currentScheme = scheme;
        System.out.println("System: Fine Scheme changed to " + scheme);
    }

    public static FineScheme getCurrentScheme() {
        return currentScheme;
    }

    //calculate parking fee (ceiling rounding)
    public static double calculateParkingFee(double hours, double hourlyRate, String spotType, boolean hasHandicappedCard) {
        //free parking for valid permit holders in Handicapped spots
        if (spotType.equalsIgnoreCase("Handicapped") && hasHandicappedCard) {
            return 0.0;
        }
        
        int roundedHours = (int) Math.ceil(hours);  
        if (roundedHours == 0) roundedHours = 1; // Minimum charge is 1 hour
        return roundedHours * hourlyRate;
    }

    //calculate fine
    public static double calculateFine(double hours, boolean isReservedViolation) {
        boolean isOverstay = hours > 24.0;
        
        if (!isOverstay && !isReservedViolation) {
            return 0.0;
        }

        double fineAmount = 0.0;
        double overstayHours = (hours > 24) ? (hours - 24) : 0;

        switch (currentScheme) {
            case FIXED:
                //flat penalty
                fineAmount = 50.0;
                break;

            case PROGRESSIVE:
                //tiered fines
                if (hours <= 24) {
                    fineAmount = 50.0; 
                } else if (hours <= 48) {
                    fineAmount = 50.0 + 100.0; 
                } else if (hours <= 72) {
                    fineAmount = 50.0 + 100.0 + 150.0; 
                } else {
                    fineAmount = 50.0 + 100.0 + 150.0 + 200.0; 
                }
                break;

            case HOURLY:
                if (isReservedViolation) {
                    fineAmount = Math.ceil(hours) * 20.0;
                } else {
                    fineAmount = Math.ceil(overstayHours) * 20.0;
                }
                break;
        }
        return fineAmount;
    }

    public static double calculateTotalDue(String plate, double hours, double hourlyRate, boolean isReservedViolation, String spotType, boolean hasHandicappedCard) {
        double parkingFee = calculateParkingFee(hours, hourlyRate, spotType, hasHandicappedCard);
        double newFine = calculateFine(hours, isReservedViolation);
        double oldDebt = outstandingFines.getOrDefault(plate, 0.0);

        return parkingFee + newFine + oldDebt;
    }

    public static void clearFines(String plate) {
        if (outstandingFines.containsKey(plate)) {
            outstandingFines.remove(plate);
            System.out.println("System: Fines cleared for " + plate);
        }
    }

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

    public static boolean isVehicleAllowed(String spotType, String vehicleType) {
        if (spotType.equalsIgnoreCase("Motorcycle")) {
            return vehicleType.equalsIgnoreCase("Motorcycle");
        }

        if (spotType.equalsIgnoreCase("Compact")) {
            return vehicleType.equalsIgnoreCase("Motorcycle") || vehicleType.equalsIgnoreCase("Car");
        }

        if (spotType.equalsIgnoreCase("Regular")) {
            return !vehicleType.equalsIgnoreCase("Motorcycle");
        }

        if (spotType.equalsIgnoreCase("Reserved") || spotType.equalsIgnoreCase("Handicapped")) {
            return true; 
        }

        return true; 
    }
}