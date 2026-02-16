// Observer Interface for the Observer Pattern.
// Components that need to update when parking data changes (like the Admin Panel) implement this.
public interface ParkingObserver {
    
    // Called automatically by ParkingLot whenever a vehicle enters or exits.
    void onParkingDataChanged();
}