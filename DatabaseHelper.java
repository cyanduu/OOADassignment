import java.sql.*;

public class DatabaseHelper {
    private static final String URL = "jdbc:sqlite:parking.db";

    public static void initializeDatabase() {
        String sqlHandicapped = "CREATE TABLE IF NOT EXISTS HandicappedPermits (plate_number VARCHAR(20) PRIMARY KEY);";
        String sqlReserved = "CREATE TABLE IF NOT EXISTS ReservedPermits (plate_number VARCHAR(20) PRIMARY KEY);";
        
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sqlHandicapped);
            stmt.execute(sqlReserved);
            
            System.out.println("System: Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database Initialization Error: " + e.getMessage());
        }
    }

    //register vip
    public static boolean registerReservedPlate(String plate) {
        String sql = "INSERT INTO ReservedPermits(plate_number) VALUES(?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, plate.toUpperCase().trim());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering Reserved plate: " + e.getMessage());
            return false; 
        }
    }

    //check if the vehicle in vip database
    public static boolean hasReservedPermit(String plate) {
        String sql = "SELECT plate_number FROM ReservedPermits WHERE plate_number = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, plate.toUpperCase().trim());
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); 
        } catch (SQLException e) {
            System.err.println("Error checking Reserved permit: " + e.getMessage());
            return false;
        }
    }

    //register handicapped permit
    public static boolean registerHandicappedPlate(String plate) {
        String sql = "INSERT INTO HandicappedPermits(plate_number) VALUES(?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, plate.toUpperCase().trim());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering Handicapped plate: " + e.getMessage());
            return false;
        }
    }

    //check if the vehicle in handicapped database
    public static boolean hasHandicappedPermit(String plate) {
        String sql = "SELECT plate_number FROM HandicappedPermits WHERE plate_number = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, plate.toUpperCase().trim());
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); 
        } catch (SQLException e) {
            System.err.println("Error checking Handicapped permit: " + e.getMessage());
            return false;
        }
    }
}