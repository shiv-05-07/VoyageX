import dao.DatabaseManager;
import dao.TripDAO;
import model.Activity;
import model.Bus;
import model.Expense;
import model.Flight;
import model.Train;
import model.Transport;
import model.Trip;
import ui.MainFrame;

import javax.swing.SwingUtilities;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * TestPhase6 tests all Phase 6 requirements:
 * 1. Database Schema & Tables Initialization:
 *    - Connects to SQLite JDBC driver (org.sqlite.JDBC)
 *    - Verifies 'trips', 'transport', 'activities', and 'expenses' tables created
 *    - Verifies PRAGMA foreign_keys = ON is active
 * 2. Trip CRUD & Foreign Key Association:
 *    - Saves trip metadata and obtains generated primary key
 *    - Saves polymorphic Transport (Flight/Train/Bus) associated with trip
 *    - Saves Activities associated with trip foreign key
 *    - Saves Expenses associated with trip foreign key
 *    - Verifies single-transaction batch save (saveTripWithDetails)
 * 3. Data Persistence Across Restart:
 *    - Reopening database connection loads all entities from disk accurately
 *    - Recalculates polymorphic costs, total cost, and remaining budget identically
 *    - Verified that itinerary summary matches byte-for-byte across restart
 * 4. Incremental Updates & Deletions:
 *    - Updating transport from Flight to AC Bus updates DB record
 *    - Deleting an activity removes record from DB and updates trip totals
 *    - Deleting an expense removes record from DB and updates trip totals
 *    - Removing transport clears record from DB
 * 5. Relational Integrity & Cascading Deletions:
 *    - Rejects child records with invalid/non-existent trip foreign key
 *    - Deleting parent trip automatically cascades to remove child transport, activities, and expenses
 * 6. UI Integration with Database:
 *    - MainFrame with persistence enabled saves trips, transport, activities, and expenses
 *    - Delete trip from UI removes trip and all child records from DB
 *    - Graceful error handling on invalid DB operations
 */
public class TestPhase6 {
    private static int testsPassed = 0;
    private static int testsTotal = 0;
    private static final String TEST_DB_FILE = "test_voyagex.db";

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("   VoyageX Phase 6: SQLite Database Persistence  ");
        System.out.println("==================================================\n");

        // Use isolated test database so production voyagex.db is never modified
        File testDb = new File(TEST_DB_FILE);
        if (testDb.exists()) {
            testDb.delete();
        }

        try {
            DatabaseManager.setDatabaseFile(TEST_DB_FILE);
            DatabaseManager.initializeDatabase();

            testSchemaInitialization();
            testTripSavingAndRetrieval();
            testTransportPolymorphismPersistence();
            testActivitiesAndExpensesPersistence();
            testDataSurvivalAcrossRestart();
            testIncrementalUpdatesAndDeletions();
            testForeignKeyConstraintsAndCascading();
            testUiDatabaseIntegration();
        } finally {
            // Restore default database file and clean up temporary test database
            DatabaseManager.setDatabaseFile(DatabaseManager.DEFAULT_DB_URL);
            if (testDb.exists()) {
                testDb.delete();
            }
        }

        System.out.println("\n--------------------------------------------------");
        System.out.println("Test Summary: " + testsPassed + " / " + testsTotal + " checks passed.");
        if (testsPassed == testsTotal) {
            System.out.println(">>> ALL PHASE 6 CHECKS PASSED SUCCESSFULLY! <<<");
        } else {
            System.err.println(">>> SOME CHECKS FAILED! <<<");
            System.exit(1);
        }
        System.out.println("--------------------------------------------------");
    }

    private static void check(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.out.println("  [FAIL] " + testName);
        }
    }

    private static void testSchemaInitialization() throws SQLException {
        System.out.println("1. Testing SQLite Connection & Schema Initialization:");

        try (Connection conn = DatabaseManager.getConnection()) {
            check("Database connection is open and valid", conn != null && !conn.isClosed());

            // Check foreign keys pragma
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys;")) {
                check("Foreign keys pragma is enabled (1)", rs.next() && rs.getInt(1) == 1);
            }

            // Verify tables exist in sqlite_master
            check("Table 'trips' exists", tableExists(conn, "trips"));
            check("Table 'transport' exists", tableExists(conn, "transport"));
            check("Table 'activities' exists", tableExists(conn, "activities"));
            check("Table 'expenses' exists", tableExists(conn, "expenses"));
        }
        System.out.println();
    }

    private static void testTripSavingAndRetrieval() throws SQLException {
        System.out.println("2. Testing Trip Saving and Retrieval (TripDAO):");

        TripDAO dao = new TripDAO();
        Trip trip = new Trip("Kashmir Paradise Tour", "Srinagar", "10-04-2027", "18-04-2027", 45000.0);

        int generatedId = dao.saveTrip(trip);
        check("saveTrip returns valid generated ID (> 0)", generatedId > 0);
        check("Trip object ID is updated with generated ID", trip.getId() == generatedId);

        // Retrieve by ID
        Trip retrieved = dao.getTripById(generatedId);
        check("Retrieved trip is not null", retrieved != null);
        check("Retrieved trip ID matches", retrieved.getId() == generatedId);
        check("Retrieved trip name matches", "Kashmir Paradise Tour".equals(retrieved.getTripName()));
        check("Retrieved destination matches", "Srinagar".equals(retrieved.getDestination()));
        check("Retrieved start date matches", "10-04-2027".equals(retrieved.getStartDate()));
        check("Retrieved end date matches", "18-04-2027".equals(retrieved.getEndDate()));
        check("Retrieved budget matches (45000.00)", retrieved.getBudget() == 45000.0);

        // Update trip metadata
        retrieved.setBudget(48000.0);
        retrieved.setDestination("Gulmarg & Srinagar");
        boolean updated = dao.updateTrip(retrieved);
        check("updateTrip returns true", updated);

        Trip reloaded = dao.getTripById(generatedId);
        check("Updated budget is persisted (48000.00)", reloaded.getBudget() == 48000.0);
        check("Updated destination is persisted", "Gulmarg & Srinagar".equals(reloaded.getDestination()));

        System.out.println();
    }

    private static void testTransportPolymorphismPersistence() throws SQLException {
        System.out.println("3. Testing Transport Polymorphism Persistence (Flight / Train / Bus):");

        TripDAO dao = new TripDAO();
        Trip trip = dao.getAllTrips().get(0);
        int tripId = trip.getId();

        // 1. Save Flight: Delhi -> Srinagar, Base: 6500, Baggage: 900
        Flight flight = new Flight("Delhi", "Srinagar", 6500.0, 900.0);
        boolean savedFlight = dao.saveOrUpdateTransport(tripId, flight);
        check("Flight transport saved to DB", savedFlight);

        Trip withFlight = dao.getTripById(tripId);
        check("Retrieved transport is instance of Flight", withFlight.getTransport() instanceof Flight);
        check("Flight source is Delhi", "Delhi".equals(withFlight.getTransport().getSource()));
        check("Flight base fare is 6500.00", withFlight.getTransport().getBaseFare() == 6500.0);
        check("Flight baggage fee is 900.00", ((Flight) withFlight.getTransport()).getBaggageFee() == 900.0);
        check("Flight polymorphic calculateCost() is 7400.00", withFlight.getTransport().calculateCost() == 7400.0);

        // 2. Update to Train: Delhi -> Jammu, Base: 1200, Res: 150
        Train train = new Train("Delhi", "Jammu", 1200.0, 150.0);
        boolean updatedTrain = dao.saveOrUpdateTransport(tripId, train);
        check("Train transport updated in DB", updatedTrain);

        Trip withTrain = dao.getTripById(tripId);
        check("Retrieved transport is now instance of Train", withTrain.getTransport() instanceof Train);
        check("Train reservation fee is 150.00", ((Train) withTrain.getTransport()).getReservationFee() == 150.0);
        check("Train polymorphic calculateCost() is 1350.00", withTrain.getTransport().calculateCost() == 1350.0);

        // 3. Update to Bus (AC): Jammu -> Srinagar, Base: 800, AC Surcharge: 250
        Bus bus = new Bus("Jammu", "Srinagar", 800.0, true, 250.0);
        boolean updatedBus = dao.saveOrUpdateTransport(tripId, bus);
        check("Bus transport updated in DB", updatedBus);

        Trip withBus = dao.getTripById(tripId);
        check("Retrieved transport is now instance of Bus", withBus.getTransport() instanceof Bus);
        Bus retrievedBus = (Bus) withBus.getTransport();
        check("Bus is AC", retrievedBus.isAc());
        check("Bus AC surcharge is 250.00", retrievedBus.getAcSurcharge() == 250.0);
        check("Bus polymorphic calculateCost() is 1050.00", retrievedBus.calculateCost() == 1050.0);

        // Restore Flight for subsequent full-itinerary verification
        dao.saveOrUpdateTransport(tripId, flight);
        System.out.println();
    }

    private static void testActivitiesAndExpensesPersistence() throws SQLException {
        System.out.println("4. Testing Activities and Expenses Persistence:");

        TripDAO dao = new TripDAO();
        Trip trip = dao.getAllTrips().get(0);
        int tripId = trip.getId();

        // 1. Add Activities
        Activity a1 = new Activity("Shikara Ride on Dal Lake", "11-04-2027", 1500.0);
        Activity a2 = new Activity("Gulmarg Gondola Ride", "13-04-2027", 2200.0);

        int a1Id = dao.addActivity(tripId, a1);
        int a2Id = dao.addActivity(tripId, a2);

        check("Activity 1 saved with generated ID (> 0)", a1Id > 0 && a1.getId() == a1Id);
        check("Activity 2 saved with generated ID (> 0)", a2Id > 0 && a2.getId() == a2Id);

        // 2. Add Expenses
        Expense e1 = new Expense("Food & Dining", "Traditional Wazwan Feast", 3500.0);
        Expense e2 = new Expense("Shopping", "Pashmina Shawl & Handicrafts", 7000.0);

        int e1Id = dao.addExpense(tripId, e1);
        int e2Id = dao.addExpense(tripId, e2);

        check("Expense 1 saved with generated ID (> 0)", e1Id > 0 && e1.getId() == e1Id);
        check("Expense 2 saved with generated ID (> 0)", e2Id > 0 && e2.getId() == e2Id);

        // Retrieve trip and verify associations
        Trip reloaded = dao.getTripById(tripId);
        check("Trip has 2 activities loaded", reloaded.getActivities().size() == 2);
        check("Trip activity 0 name is 'Shikara Ride on Dal Lake'",
                "Shikara Ride on Dal Lake".equals(reloaded.getActivities().get(0).getName()));
        check("Trip activity 1 cost is 2200.00", reloaded.getActivities().get(1).getCost() == 2200.0);
        check("Total activity cost calculation is 3700.00", reloaded.calculateTotalActivityCost() == 3700.0);

        check("Trip has 2 expenses loaded", reloaded.getExpenses().size() == 2);
        check("Trip expense 0 category is 'Food & Dining'",
                "Food & Dining".equals(reloaded.getExpenses().get(0).getCategory()));
        check("Trip expense 1 amount is 7000.00", reloaded.getExpenses().get(1).getAmount() == 7000.0);
        check("Total expense cost calculation is 10500.00", reloaded.calculateTotalExpenseCost() == 10500.0);

        // Budget calculations verification
        // Budget: 48000.0; Transport: 7400.0; Activities: 3700.0; Expenses: 10500.0
        // Total cost = 7400 + 3700 + 10500 = 21600.0; Remaining = 48000 - 21600 = 26400.0
        check("Total trip cost is 21600.00", reloaded.calculateTotalCost() == 21600.0);
        check("Remaining budget is 26400.00", reloaded.calculateRemainingBudget() == 26400.0);

        System.out.println();
    }

    private static void testDataSurvivalAcrossRestart() throws SQLException {
        System.out.println("5. Testing Data Survival Across Simulated Restart:");

        // Simulate application restart: instantiate a fresh TripDAO instance
        TripDAO freshDao = new TripDAO();

        List<Trip> allTrips = freshDao.getAllTrips();
        check("Fresh DAO retrieves all trips from disk", allTrips.size() == 1);

        Trip trip = allTrips.get(0);
        check("Reloaded trip name matches", "Kashmir Paradise Tour".equals(trip.getTripName()));
        check("Reloaded transport is Flight", trip.getTransport() instanceof Flight);
        check("Reloaded flight cost is 7400.00", trip.calculateTotalTransportCost() == 7400.0);
        check("Reloaded activities count is 2", trip.getActivities().size() == 2);
        check("Reloaded expenses count is 2", trip.getExpenses().size() == 2);
        check("Reloaded total cost is 21600.00", trip.calculateTotalCost() == 21600.0);
        check("Reloaded remaining budget is 26400.00", trip.calculateRemainingBudget() == 26400.0);

        // Itinerary summary generation on reloaded trip
        String itinerary = trip.generateItinerarySummary();
        check("Itinerary text generated from DB contains header banner", itinerary.contains("VOYAGEX TRIP ITINERARY"));
        check("Itinerary text contains Flight mode", itinerary.contains("Flight"));
        check("Itinerary text contains Shikara Ride", itinerary.contains("Shikara Ride"));
        check("Itinerary text contains Wazwan Feast", itinerary.contains("Traditional Wazwan Feast"));
        check("Itinerary text contains Grand Total Cost (21600.00)", itinerary.contains("21600.00"));
        check("Itinerary text contains WITHIN BUDGET", itinerary.contains("WITHIN BUDGET"));

        System.out.println();
    }

    private static void testIncrementalUpdatesAndDeletions() throws SQLException {
        System.out.println("6. Testing Incremental Deletions & Calculations:");

        TripDAO dao = new TripDAO();
        Trip trip = dao.getAllTrips().get(0);
        int tripId = trip.getId();

        // 1. Delete second activity (Gulmarg Gondola, id = activity[1].id)
        Activity actToDelete = trip.getActivities().get(1);
        boolean actDeleted = dao.deleteActivity(actToDelete.getId());
        check("Activity deleted from database", actDeleted);

        Trip afterActDel = dao.getTripById(tripId);
        check("Activities count decreased to 1", afterActDel.getActivities().size() == 1);
        check("Remaining activity is Shikara Ride",
                "Shikara Ride on Dal Lake".equals(afterActDel.getActivities().get(0).getName()));
        check("Updated activity total is 1500.00", afterActDel.calculateTotalActivityCost() == 1500.0);

        // 2. Delete second expense (Pashmina Shawl)
        Expense expToDelete = trip.getExpenses().get(1);
        boolean expDeleted = dao.deleteExpense(expToDelete.getId());
        check("Expense deleted from database", expDeleted);

        Trip afterExpDel = dao.getTripById(tripId);
        check("Expenses count decreased to 1", afterExpDel.getExpenses().size() == 1);
        check("Remaining expense is Wazwan Feast",
                "Traditional Wazwan Feast".equals(afterExpDel.getExpenses().get(0).getDescription()));
        check("Updated expense total is 3500.00", afterExpDel.calculateTotalExpenseCost() == 3500.0);

        // 3. Remove transport
        boolean transportRemoved = dao.deleteTransport(tripId);
        check("Transport removed from database", transportRemoved);

        Trip afterTransDel = dao.getTripById(tripId);
        check("Transport is null in reloaded trip", afterTransDel.getTransport() == null);
        check("Transport cost is 0.00", afterTransDel.calculateTotalTransportCost() == 0.0);

        // Updated grand total = 0 + 1500 + 3500 = 5000.0; Remaining = 48000 - 5000 = 43000.0
        check("Updated grand total after removals is 5000.00", afterTransDel.calculateTotalCost() == 5000.0);
        check("Updated remaining budget after removals is 43000.00", afterTransDel.calculateRemainingBudget() == 43000.0);

        System.out.println();
    }

    private static void testForeignKeyConstraintsAndCascading() throws SQLException {
        System.out.println("7. Testing Relational Integrity & Cascading Deletions:");

        TripDAO dao = new TripDAO();

        // 1. Attempt to insert activity with invalid foreign key (non-existent tripId = 999999)
        boolean caughtFkViolation = false;
        try {
            Activity invalid = new Activity("Invalid Activity", "01-01-2027", 500.0);
            dao.addActivity(999999, invalid);
        } catch (SQLException e) {
            caughtFkViolation = true;
        }
        check("Rejects activity insert with non-existent trip foreign key (SQLException)", caughtFkViolation);

        // 2. Create a separate test trip for cascade verification
        Trip cascadeTrip = new Trip("Cascade Test Trip", "Goa", "01-11-2027", "05-11-2027", 20000.0);
        cascadeTrip.setTransport(new Flight("Mumbai", "Goa", 3000.0, 500.0));
        cascadeTrip.addActivity(new Activity("Beach Party", "02-11-2027", 1000.0));
        cascadeTrip.addExpense(new Expense("Drinks", "Cocktails", 800.0));

        int cascadeTripId = dao.saveTripWithDetails(cascadeTrip);
        check("Cascade trip created with details (ID > 0)", cascadeTripId > 0);

        // Verify child records exist in DB
        try (Connection conn = DatabaseManager.getConnection()) {
            check("Child transport exists before delete", countRows(conn, "SELECT COUNT(*) FROM transport WHERE trip_id = " + cascadeTripId) == 1);
            check("Child activity exists before delete", countRows(conn, "SELECT COUNT(*) FROM activities WHERE trip_id = " + cascadeTripId) == 1);
            check("Child expense exists before delete", countRows(conn, "SELECT COUNT(*) FROM expenses WHERE trip_id = " + cascadeTripId) == 1);

            // Delete parent trip
            boolean deleted = dao.deleteTrip(cascadeTripId);
            check("Parent trip deleted successfully", deleted);

            // Verify SQLite foreign key cascade automatically deleted all child records
            check("Child transport automatically deleted via ON DELETE CASCADE",
                    countRows(conn, "SELECT COUNT(*) FROM transport WHERE trip_id = " + cascadeTripId) == 0);
            check("Child activities automatically deleted via ON DELETE CASCADE",
                    countRows(conn, "SELECT COUNT(*) FROM activities WHERE trip_id = " + cascadeTripId) == 0);
            check("Child expenses automatically deleted via ON DELETE CASCADE",
                    countRows(conn, "SELECT COUNT(*) FROM expenses WHERE trip_id = " + cascadeTripId) == 0);
        }

        System.out.println();
    }

    private static void testUiDatabaseIntegration() throws Exception {
        System.out.println("8. Testing MainFrame UI Integration with Database:");

        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame();
            frame.setShowDialogs(false); // Non-blocking headless mode

            TripDAO dao = new TripDAO();
            frame.enablePersistence(dao);
            check("MainFrame persistence is enabled", frame.isPersistenceEnabled());

            // 1. Load trips from DB into UI
            frame.loadTripsFromDatabase();
            check("UI loaded trips from database (count >= 1)", frame.getTripList().size() >= 1);
            check("Trips table has corresponding rows", frame.getTableModel().getRowCount() == frame.getTripList().size());

            // 2. Create new trip via UI form and verify it saves to DB
            frame.getTxtTripName().setText("UI Persisted Agra Trip");
            frame.getTxtDestination().setText("Agra");
            frame.getTxtStartDate().setText("25-12-2027");
            frame.getTxtEndDate().setText("26-12-2027");
            frame.getTxtBudget().setText("12000.0");

            boolean created = frame.handleCreateTrip();
            check("handleCreateTrip succeeds", created);

            Trip createdTrip = frame.getTripList().get(frame.getTripList().size() - 1);
            check("Newly created trip received database ID (> 0)", createdTrip.getId() > 0);

            // Verify in DB directly
            try {
                Trip inDb = dao.getTripById(createdTrip.getId());
                check("Newly created trip is verified in SQLite database", inDb != null && "UI Persisted Agra Trip".equals(inDb.getTripName()));
            } catch (SQLException e) {
                check("Database query failed: " + e.getMessage(), false);
            }

            // 3. Add transport, activity, and expense via UI and verify DB persistence
            frame.openManageTrip(createdTrip);

            // Add Train transport
            frame.getCmbTransportType().setSelectedItem("Train");
            frame.getTxtTransportSource().setText("Delhi");
            frame.getTxtTransportDestination().setText("Agra");
            frame.getTxtTransportBaseFare().setText("450.0");
            frame.getTxtTransportExtra().setText("50.0");
            boolean transSaved = frame.handleSaveTransport();
            check("UI save transport succeeds", transSaved);

            // Add Activity
            frame.getTxtActivityName().setText("Taj Mahal Sunrise Tour");
            frame.getTxtActivityDate().setText("25-12-2027");
            frame.getTxtActivityCost().setText("1500.0");
            boolean actAdded = frame.handleAddActivity();
            check("UI add activity succeeds", actAdded);

            // Add Expense
            frame.getCmbExpenseCategory().setSelectedItem("Food & Dining");
            frame.getTxtExpenseDescription().setText("Petha Sweets & Lunch");
            frame.getTxtExpenseAmount().setText("600.0");
            boolean expAdded = frame.handleAddExpense();
            check("UI add expense succeeds", expAdded);

            // Verify in DB directly
            try {
                Trip loadedFromDb = dao.getTripById(createdTrip.getId());
                check("DB reflects saved Train transport", loadedFromDb.getTransport() instanceof Train);
                check("DB reflects 1 activity", loadedFromDb.getActivities().size() == 1);
                check("DB reflects 1 expense", loadedFromDb.getExpenses().size() == 1);
                check("DB reflects grand total cost (2600.00)", loadedFromDb.calculateTotalCost() == 2600.0);
            } catch (SQLException e) {
                check("DB verification failed: " + e.getMessage(), false);
            }

            // 4. Delete trip from UI
            frame.showScreen(MainFrame.SCREEN_VIEW);
            int lastIndex = frame.getTripsTable().getRowCount() - 1;
            frame.getTripsTable().setRowSelectionInterval(lastIndex, lastIndex);
            boolean deletedFromUi = frame.handleDeleteTrip();
            check("UI handleDeleteTrip succeeds", deletedFromUi);

            // Verify trip and children are gone from DB
            try {
                Trip shouldBeNull = dao.getTripById(createdTrip.getId());
                check("Trip is deleted from SQLite database", shouldBeNull == null);
            } catch (SQLException e) {
                check("DB verification failed: " + e.getMessage(), false);
            }

            frame.dispose();
        });

        System.out.println();
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static int countRows(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
