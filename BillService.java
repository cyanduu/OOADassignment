public class BillService {
    public static double calculateFee(double hours, double hourlyRate) {
        // Simple Logic: Minimum 1 hour charge
        if (hours < 1.0) hours = 1.0;
        return hours * hourlyRate;
    }
}