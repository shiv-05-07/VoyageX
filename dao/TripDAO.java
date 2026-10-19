package dao;

import model.Activity;
import model.Bus;
import model.Expense;
import model.Flight;
import model.Train;
import model.Transport;
import model.Trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * TripDAO (Data Access Object) handles all CRUD operations for trips, transportation,
 * activities, and expenses in the SQLite database.
 * 
 * Demonstrates:
 * - Data Access Object (DAO) pattern isolating database logic from UI components.
 * - Parameterized Queries: PreparedStatement preventing SQL injection attacks.
 * - ResultSet traversal and column mapping to domain models.
 * - Primary Key generation retrieval via Statement.RETURN_GENERATED_KEYS.
 * - Transaction Management: commit and rollback with setAutoCommit(false).
 * - Cascading Deletions: relying on SQLite foreign key constraints.
 * - Try-with-resources statement ensuring robust closure of database resources.
 */
public class TripDAO {

    /**
     * Saves a new Trip record to the database and sets the generated ID on the trip object.
     * 
     * @param trip the Trip object to insert
     * @return the generated database ID
     * @throws SQLException if a database error occurs
     */
    public int saveTrip(Trip trip) throws SQLException {
        if (trip == null) {
            throw new IllegalArgumentException("Trip cannot be null.");
        }

        String sql = "INSERT INTO trips (trip_name, destination, start_date, end_date, budget) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, trip.getTripName());
            pstmt.setString(2, trip.getDestination());
            pstmt.setString(3, trip.getStartDate());
            pstmt.setString(4, trip.getEndDate());
            pstmt.setDouble(5, trip.getBudget());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating trip failed, no rows affected.");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    trip.setId(id);
                    return id;
                } else {
                    throw new SQLException("Creating trip failed, no generated ID obtained.");
                }
            }
        }
    }

    /**
     * Saves a complete Trip along with its transport, activities, and expenses inside a single transaction.
     * 
     * @param trip the full Trip object to save
     * @return the generated trip ID
     * @throws SQLException if a database error occurs
     */
    public int saveTripWithDetails(Trip trip) throws SQLException {
        if (trip == null) {
            throw new IllegalArgumentException("Trip cannot be null.");
        }

        String tripSql = "INSERT INTO trips (trip_name, destination, start_date, end_date, budget) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction
            try {
                int tripId;
                try (PreparedStatement pstmt = conn.prepareStatement(tripSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, trip.getTripName());
                    pstmt.setString(2, trip.getDestination());
                    pstmt.setString(3, trip.getStartDate());
                    pstmt.setString(4, trip.getEndDate());
                    pstmt.setDouble(5, trip.getBudget());
                    pstmt.executeUpdate();

                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            tripId = rs.getInt(1);
                            trip.setId(tripId);
                        } else {
                            throw new SQLException("Creating trip failed, no ID obtained.");
                        }
                    }
                }

                // Save transport if present
                if (trip.getTransport() != null) {
                    insertOrUpdateTransportInternal(conn, tripId, trip.getTransport());
                }

                // Save activities
                for (Activity activity : trip.getActivities()) {
                    insertActivityInternal(conn, tripId, activity);
                }

                // Save expenses
                for (Expense expense : trip.getExpenses()) {
                    insertExpenseInternal(conn, tripId, expense);
                }

                conn.commit(); // Commit transaction
                return tripId;
            } catch (SQLException ex) {
                conn.rollback(); // Rollback on failure to prevent partial/corrupted data
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Retrieves all saved trips from the database with their associated transport,
     * activities, and expenses.
     * 
     * @return list of fully populated Trip objects
     * @throws SQLException if a database access error occurs
     */
    public List<Trip> getAllTrips() throws SQLException {
        List<Trip> trips = new ArrayList<Trip>();
        String sql = "SELECT id, trip_name, destination, start_date, end_date, budget FROM trips ORDER BY id ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("trip_name");
                String dest = rs.getString("destination");
                String start = rs.getString("start_date");
                String end = rs.getString("end_date");
                double budget = rs.getDouble("budget");

                Trip trip = new Trip(id, name, dest, start, end, budget);

                // Load associated child records
                trip.setTransport(loadTransportForTrip(conn, id));
                for (Activity a : loadActivitiesForTrip(conn, id)) {
                    trip.addActivity(a);
                }
                for (Expense e : loadExpensesForTrip(conn, id)) {
                    trip.addExpense(e);
                }

                trips.add(trip);
            }
        }
        return trips;
    }

    /**
     * Retrieves a single Trip by its primary key ID.
     * 
     * @param tripId the primary key of the trip
     * @return the Trip object with all child records, or null if not found
     * @throws SQLException if a database error occurs
     */
    public Trip getTripById(int tripId) throws SQLException {
        String sql = "SELECT id, trip_name, destination, start_date, end_date, budget FROM trips WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tripId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("trip_name");
                    String dest = rs.getString("destination");
                    String start = rs.getString("start_date");
                    String end = rs.getString("end_date");
                    double budget = rs.getDouble("budget");

                    Trip trip = new Trip(id, name, dest, start, end, budget);
                    trip.setTransport(loadTransportForTrip(conn, id));
                    for (Activity a : loadActivitiesForTrip(conn, id)) {
                        trip.addActivity(a);
                    }
                    for (Expense e : loadExpensesForTrip(conn, id)) {
                        trip.addExpense(e);
                    }
                    return trip;
                }
            }
        }
        return null;
    }

    /**
     * Updates trip metadata in the database.
     * 
     * @param trip the trip with updated values
     * @return true if updated successfully
     * @throws SQLException if a database error occurs
     */
    public boolean updateTrip(Trip trip) throws SQLException {
        if (trip == null || trip.getId() <= 0) {
            return false;
        }

        String sql = "UPDATE trips SET trip_name = ?, destination = ?, start_date = ?, end_date = ?, budget = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, trip.getTripName());
            pstmt.setString(2, trip.getDestination());
            pstmt.setString(3, trip.getStartDate());
            pstmt.setString(4, trip.getEndDate());
            pstmt.setDouble(5, trip.getBudget());
            pstmt.setInt(6, trip.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a trip and cascades deletion to its transport, activities, and expenses.
     * 
     * @param tripId the ID of the trip to delete
     * @return true if the trip was deleted
     * @throws SQLException if a database error occurs
     */
    public boolean deleteTrip(int tripId) throws SQLException {
        String sql = "DELETE FROM trips WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tripId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Saves or updates the transportation record for a specific trip.
     * 
     * @param tripId the ID of the trip
     * @param transport the Transport object (Flight, Train, or Bus)
     * @return true if saved successfully
     * @throws SQLException if a database error occurs
     */
    public boolean saveOrUpdateTransport(int tripId, Transport transport) throws SQLException {
        if (transport == null) {
            return deleteTransport(tripId);
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            return insertOrUpdateTransportInternal(conn, tripId, transport);
        }
    }

    /**
     * Deletes the transportation record for a specific trip.
     * 
     * @param tripId the ID of the trip
     * @return true if deleted
     * @throws SQLException if a database error occurs
     */
    public boolean deleteTransport(int tripId) throws SQLException {
        String sql = "DELETE FROM transport WHERE trip_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tripId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Adds an activity for a specific trip and sets the generated ID on the activity.
     * 
     * @param tripId the ID of the trip
     * @param activity the Activity object
     * @return the generated activity ID
     * @throws SQLException if a database error occurs
     */
    public int addActivity(int tripId, Activity activity) throws SQLException {
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null.");
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            return insertActivityInternal(conn, tripId, activity);
        }
    }

    /**
     * Deletes an activity record by its primary key.
     * 
     * @param activityId the primary key of the activity
     * @return true if deleted
     * @throws SQLException if a database error occurs
     */
    public boolean deleteActivity(int activityId) throws SQLException {
        String sql = "DELETE FROM activities WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, activityId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Adds an expense for a specific trip and sets the generated ID on the expense.
     * 
     * @param tripId the ID of the trip
     * @param expense the Expense object
     * @return the generated expense ID
     * @throws SQLException if a database error occurs
     */
    public int addExpense(int tripId, Expense expense) throws SQLException {
        if (expense == null) {
            throw new IllegalArgumentException("Expense cannot be null.");
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            return insertExpenseInternal(conn, tripId, expense);
        }
    }

    /**
     * Deletes an expense record by its primary key.
     * 
     * @param expenseId the primary key of the expense
     * @return true if deleted
     * @throws SQLException if a database error occurs
     */
    public boolean deleteExpense(int expenseId) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, expenseId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // --- Internal Helper Methods (Shared across direct calls and transactions) ---

    private boolean insertOrUpdateTransportInternal(Connection conn, int tripId, Transport transport) throws SQLException {
        String sql = "INSERT OR REPLACE INTO transport (trip_id, transport_type, source, destination, base_fare, extra_fee, is_ac) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tripId);
            pstmt.setString(2, transport.getTransportType());
            pstmt.setString(3, transport.getSource());
            pstmt.setString(4, transport.getDestination());
            pstmt.setDouble(5, transport.getBaseFare());

            if (transport instanceof Flight) {
                pstmt.setDouble(6, ((Flight) transport).getBaggageFee());
                pstmt.setInt(7, 0);
            } else if (transport instanceof Train) {
                pstmt.setDouble(6, ((Train) transport).getReservationFee());
                pstmt.setInt(7, 0);
            } else if (transport instanceof Bus) {
                Bus bus = (Bus) transport;
                pstmt.setDouble(6, bus.getAcSurcharge());
                pstmt.setInt(7, bus.isAc() ? 1 : 0);
            } else {
                pstmt.setDouble(6, 0.0);
                pstmt.setInt(7, 0);
            }

            return pstmt.executeUpdate() > 0;
        }
    }

    private int insertActivityInternal(Connection conn, int tripId, Activity activity) throws SQLException {
        String sql = "INSERT INTO activities (trip_id, name, date, cost) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, tripId);
            pstmt.setString(2, activity.getName());
            pstmt.setString(3, activity.getDate());
            pstmt.setDouble(4, activity.getCost());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    activity.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    private int insertExpenseInternal(Connection conn, int tripId, Expense expense) throws SQLException {
        String sql = "INSERT INTO expenses (trip_id, category, description, amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, tripId);
            pstmt.setString(2, expense.getCategory());
            pstmt.setString(3, expense.getDescription());
            pstmt.setDouble(4, expense.getAmount());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    expense.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    private Transport loadTransportForTrip(Connection conn, int tripId) throws SQLException {
        String sql = "SELECT transport_type, source, destination, base_fare, extra_fee, is_ac FROM transport WHERE trip_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tripId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("transport_type");
                    String source = rs.getString("source");
                    String dest = rs.getString("destination");
                    double baseFare = rs.getDouble("base_fare");
                    double extraFee = rs.getDouble("extra_fee");
                    boolean isAc = rs.getInt("is_ac") == 1;

                    if ("Flight".equalsIgnoreCase(type)) {
                        return new Flight(source, dest, baseFare, extraFee);
                    } else if ("Train".equalsIgnoreCase(type)) {
                        return new Train(source, dest, baseFare, extraFee);
                    } else if ("Bus".equalsIgnoreCase(type)) {
                        return new Bus(source, dest, baseFare, isAc, extraFee);
                    }
                }
            }
        }
        return null;
    }

    private List<Activity> loadActivitiesForTrip(Connection conn, int tripId) throws SQLException {
        List<Activity> list = new ArrayList<Activity>();
        String sql = "SELECT id, name, date, cost FROM activities WHERE trip_id = ? ORDER BY id ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tripId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String date = rs.getString("date");
                    double cost = rs.getDouble("cost");
                    list.add(new Activity(id, name, date, cost));
                }
            }
        }
        return list;
    }

    private List<Expense> loadExpensesForTrip(Connection conn, int tripId) throws SQLException {
        List<Expense> list = new ArrayList<Expense>();
        String sql = "SELECT id, category, description, amount FROM expenses WHERE trip_id = ? ORDER BY id ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tripId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String cat = rs.getString("category");
                    String desc = rs.getString("description");
                    double amount = rs.getDouble("amount");
                    list.add(new Expense(id, cat, desc, amount));
                }
            }
        }
        return list;
    }
}
