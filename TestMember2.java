public class TestMember2 {
    public static void main(String[] args) {
        System.out.println("=== MEMBER 2: LOGIC TEST (NO GUI) ===\n");

        // --- TEST 1: VEHICLE SUITABILITY ---
        System.out.println("--- Test 1: Checking Vehicle Suitability ---");
        testSuitability("Regular", "SUV", true);       // Should be TRUE
        testSuitability("Compact", "SUV", false);      // Should be FALSE
        testSuitability("Motorcycle", "Car", false);   // Should be FALSE
        System.out.println();

        // --- TEST 2: STANDARD BILLING (Ceiling Rounding) ---
        System.out.println("--- Test 2: Standard Billing (Ceiling Rounding) ---");
        // 1.1 hours should round to 2 hours. Rate = RM 5.00
        double fee = FineManager.calculateParkingFee(1.1, 5.0, "Regular", false);
        System.out.println("Parked 1.1 hours @ RM 5/hr. Expected: 10.0. Actual: " + fee);
        
        // Handicapped Check
        double freeFee = FineManager.calculateParkingFee(5.0, 2.0, "Handicapped", true);
        System.out.println("Handicapped with Card. Expected: 0.0. Actual: " + freeFee);
        System.out.println();

        // --- TEST 3: FINE SCHEMES (The Core Logic) ---
        System.out.println("--- Test 3: Fine Schemes (Overstay 26 Hours) ---");
        
        // Scenario: Car parked for 26 hours (2 hours overstay). 
        // Rate RM 5.
        // Base Fee: 26 * 5 = 130.
        // We only care about the FINE amount here.

        // A) FIXED SCHEME
        FineManager.setFineScheme(FineManager.FineScheme.FIXED);
        double fineA = FineManager.calculateFine(26.0, false); 
        System.out.println("Scheme: FIXED. Overstay 26h. Expected Fine: 50.0. Actual: " + fineA);

        // B) HOURLY SCHEME
        FineManager.setFineScheme(FineManager.FineScheme.HOURLY);
        // Overstay 2 hours * RM 20 = RM 40
        double fineB = FineManager.calculateFine(26.0, false);
        System.out.println("Scheme: HOURLY. Overstay 26h (2h extra). Expected Fine: 40.0. Actual: " + fineB);

        // C) PROGRESSIVE SCHEME
        FineManager.setFineScheme(FineManager.FineScheme.PROGRESSIVE);
        // First 24h (50) + Next tier (100) = 150
        double fineC = FineManager.calculateFine(26.0, false);
        System.out.println("Scheme: PROGRESSIVE. Overstay 26h. Expected Fine: 150.0. Actual: " + fineC);
        System.out.println();

        // --- TEST 4: DEBT TRACKING ---
        System.out.println("--- Test 4: Debt Tracking ---");
        String plate = "ABC-9999";
        FineManager.addFineToAccount(plate, 100.0);
        System.out.println("Added RM 100 debt to " + plate);
        System.out.println("Current Debt: " + FineManager.getUnpaidFines(plate));
        
        FineManager.clearFines(plate);
        System.out.println("Cleared Debt. Current: " + FineManager.getUnpaidFines(plate));
    }

    // Helper method to make the output cleaner
    private static void testSuitability(String spot, String vehicle, boolean expected) {
        boolean result = FineManager.isVehicleAllowed(spot, vehicle);
        String status = (result == expected) ? "PASS" : "FAIL";
        System.out.println("Spot: " + spot + " | Vehicle: " + vehicle + " -> " + result + " [" + status + "]");
    }
}