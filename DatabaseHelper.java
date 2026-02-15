import java.sql.*;

public class DatabaseHelper {
    // This creates a local SQLite database file named "parking.db"
    private static final String URL = "jdbc:sqlite:parking.db";

    // 1. Create the SQL Table
    public static void initializeDatabase() {
        String sqlHandicapped = "CREATE TABLE IF NOT EXISTS HandicappedPermits (plate_number VARCHAR(20) PRIMARY KEY);";
        // Make sure this string exists!
        String sqlReserved = "CREATE TABLE IF NOT EXISTS ReservedPermits (plate_number VARCHAR(20) PRIMARY KEY);";
        
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sqlHandicapped);
            // CRITICAL: Make sure this line is here!
            stmt.execute(sqlReserved); 
            
            System.out.println("System: SQL Database Initialized (Handicapped & Reserved).");
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }

    public static boolean registerReservedPlate(String plate) {
        String sql = "INSERT INTO ReservedPermits(plate_number) VALUES(?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plate.toUpperCase().trim());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // --- ADD THESE TWO LINES TO SEE THE REAL ERROR ---
            System.err.println("RESERVED SQL ERROR: " + e.getMessage());
            e.printStackTrace(); 
            // -------------------------------------------------
            return false; 
        }
    }

    // --- 3. NEW: System checks if plate is Reserved VIP ---
    public static boolean hasReservedPermit(String plate) {
        String sql = "SELECT plate_number FROM ReservedPermits WHERE plate_number = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plate.toUpperCase().trim());
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); 
        } catch (SQLException e) {
            return false;
        }
    }

 public static boolean registerHandicappedPlate(String plate) {
        String sql = "INSERT INTO HandicappedPermits(plate_number) VALUES(?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plate.toUpperCase().trim());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // IF IT FAILS, THIS WILL PRINT THE EXACT REASON IN THE TERMINAL
            e.printStackTrace(); 
            return false;
        }
    }

    // 3. System checks if plate gets a discount
    public static boolean hasHandicappedPermit(String plate) {
        String sql = "SELECT plate_number FROM HandicappedPermits WHERE plate_number = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plate.toUpperCase().trim());
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true if the plate is found in the SQL table
        } catch (SQLException e) {
            return false;
        }
    }

    
}