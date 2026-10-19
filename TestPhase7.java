import interfaces.Billable;
import exception.InvalidBudgetException;
import exception.InvalidTripDataException;
import model.Activity;
import model.Bus;
import model.Expense;
import model.Flight;
import model.Train;
import model.Transport;
import model.Trip;
import service.ExportService;
import ui.MainFrame;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TestPhase7 is the automated verification suite for Phase 7:
 * Java Syllabus Gap Review and Implementation.
 * 
 * Verifies:
 * 1. interfaces.Billable user-defined interface and Java 8 default method.
 * 2. Polymorphic Billable references across Transport, Activity, Expense, and Trip.
 * 3. Java 8 Stream API in Trip (filter, mapToDouble, sum, Collectors.toList).
 * 4. exception.InvalidTripDataException and exception.InvalidBudgetException hierarchy.
 * 5. MainFrame.validateTripInputsStrict() throwing custom exceptions.
 * 6. Multithreading in ExportService (Thread lifecycle, Runnable, daemon status, join).
 * 7. Thread synchronization (synchronized counter methods across concurrent threads).
 * 8. Asynchronous export callback delivery and Swing EDT safety.
 */
public class TestPhase7 {

    private static int passedChecks = 0;
    private static int failedChecks = 0;

    private static void check(String description, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + description);
            passedChecks++;
        } else {
            System.err.println("  [FAIL] " + description);
            failedChecks++;
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   VoyageX Phase 7: Syllabus Gap Implementation   ");
        System.out.println("==================================================");

        try {
            testBillableInterface();
            testStreamApiInTrip();
            testCustomExceptions();
            testMultithreadingAndSynchronization();
            testUIIntegration();

            System.out.println("\n--------------------------------------------------");
            System.out.println("Test Summary: " + passedChecks + " / " + (passedChecks + failedChecks) + " checks passed.");
            if (failedChecks == 0) {
                System.out.println(">>> ALL PHASE 7 CHECKS PASSED SUCCESSFULLY! <<<");
            } else {
                System.err.println(">>> SOME PHASE 7 CHECKS FAILED! <<<");
                System.exit(1);
            }
            System.out.println("--------------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testBillableInterface() {
        System.out.println("\n1. Testing User-Defined Interface (interfaces.Billable):");

        Flight flight = new Flight("Mumbai", "Goa", 4500.0, 750.0);
        Train train = new Train("Delhi", "Agra", 1200.0, 150.0);
        Bus bus = new Bus("Pune", "Mahabaleshwar", 800.0, true, 200.0);
        Activity activity = new Activity(1, "Scuba Diving", "15-11-2026", 3500.0);
        Expense expense = new Expense(1, "Food", "Seafood Dinner", 1800.0);

        check("Flight implements Billable", flight instanceof Billable);
        check("Train implements Billable", train instanceof Billable);
        check("Bus implements Billable", bus instanceof Billable);
        check("Transport implements Billable", flight instanceof Transport && flight instanceof Billable);
        check("Activity implements Billable", activity instanceof Billable);
        check("Expense implements Billable", expense instanceof Billable);

        // Polymorphic invocation through Billable interface reference
        Billable billableFlight = flight;
        Billable billableTrain = train;
        Billable billableBus = bus;
        Billable billableActivity = activity;
        Billable billableExpense = expense;

        check("Billable flight calculates cost (5250.00)", billableFlight.calculateCost() == 5250.0);
        check("Billable train calculates cost (1350.00)", billableTrain.calculateCost() == 1350.0);
        check("Billable bus calculates cost (1000.00)", billableBus.calculateCost() == 1000.0);
        check("Billable activity calculates cost (3500.00)", billableActivity.calculateCost() == 3500.0);
        check("Billable expense calculates cost (1800.00)", billableExpense.calculateCost() == 1800.0);

        // Java 8 default method verification
        check("Flight default method getFormattedCost() is 'INR 5250.00'",
                "INR 5250.00".equals(billableFlight.getFormattedCost()));
        check("Activity default method getFormattedCost() is 'INR 3500.00'",
                "INR 3500.00".equals(billableActivity.getFormattedCost()));
        check("Expense default method getFormattedCost() is 'INR 1800.00'",
                "INR 1800.00".equals(billableExpense.getFormattedCost()));

        // Trip itself implements Billable
        Trip trip = new Trip("Goa Retreat", "Goa", "10-11-2026", "20-11-2026", 30000.0);
        trip.setTransport(flight);
        trip.addActivity(activity);
        trip.addExpense(expense);

        check("Trip implements Billable", trip instanceof Billable);
        Billable billableTrip = trip;
        check("Billable trip calculateCost() matches total cost (10550.00)",
                billableTrip.calculateCost() == 10550.0);
        check("Trip default method getFormattedCost() is 'INR 10550.00'",
                "INR 10550.00".equals(billableTrip.getFormattedCost()));
    }

    private static void testStreamApiInTrip() {
        System.out.println("\n2. Testing Java 8 Stream API in Trip:");

        Trip trip = new Trip("Rajasthan Heritage", "Jaipur", "01-12-2026", "05-12-2026", 50000.0);
        trip.setTransport(new Flight("Delhi", "Jaipur", 4000.0, 500.0)); // 4500.0

        Activity a1 = new Activity("Amber Fort", "02-12-2026", 1200.0);
        Activity a2 = new Activity("Hawa Mahal", "02-12-2026", 800.0);
        Activity a3 = new Activity("City Palace", "03-12-2026", 1500.0);
        Activity a4 = new Activity("Chokhi Dhani", "04-12-2026", 2000.0);

        trip.addActivity(a1);
        trip.addActivity(a2);
        trip.addActivity(a3);
        trip.addActivity(a4); // Activities total = 5500.0

        Expense e1 = new Expense("Food", "Traditional Thali", 1200.0);
        Expense e2 = new Expense("Food", "Lassi & Sweets", 350.0);
        Expense e3 = new Expense("Shopping", "Jaipuri Quilts", 3500.0);
        Expense e4 = new Expense("Lodging", "Heritage Haveli Stay", 8000.0);
        Expense e5 = new Expense("Food", "Rooftop Dinner", 1450.0);

        trip.addExpense(e1);
        trip.addExpense(e2);
        trip.addExpense(e3);
        trip.addExpense(e4);
        trip.addExpense(e5); // Expenses total = 14500.0

        // Grand Total: 4500 + 5500 + 14500 = 24500.0
        List<Billable> allItems = trip.getAllBillableItems();
        check("getAllBillableItems returns 10 items (1 transport + 4 activities + 5 expenses)",
                allItems.size() == 10);

        // Stream sum via mapToDouble
        double streamTotalCost = trip.calculateTotalCostViaStream();
        check("calculateTotalCostViaStream() equals 24500.00", streamTotalCost == 24500.0);
        check("Stream total cost matches traditional calculateTotalCost() exactly",
                streamTotalCost == trip.calculateTotalCost());

        // Stream filter activities by date
        List<Activity> dec2Activities = trip.getActivitiesByDate("02-12-2026");
        check("getActivitiesByDate('02-12-2026') returns 2 activities", dec2Activities.size() == 2);
        check("First activity on 02-12-2026 is Amber Fort",
                dec2Activities.size() >= 1 && "Amber Fort".equals(dec2Activities.get(0).getName()));
        check("Second activity on 02-12-2026 is Hawa Mahal",
                dec2Activities.size() >= 2 && "Hawa Mahal".equals(dec2Activities.get(1).getName()));

        List<Activity> dec3Activities = trip.getActivitiesByDate("03-12-2026");
        check("getActivitiesByDate('03-12-2026') returns 1 activity", dec3Activities.size() == 1);

        List<Activity> nonExistentDateActivities = trip.getActivitiesByDate("15-12-2026");
        check("getActivitiesByDate with non-matching date returns empty list",
                nonExistentDateActivities != null && nonExistentDateActivities.isEmpty());

        check("getActivitiesByDate with null date returns empty list safely",
                trip.getActivitiesByDate(null) != null && trip.getActivitiesByDate(null).isEmpty());

        // Stream filter expenses by category
        List<Expense> foodExpenses = trip.getExpensesByCategory("Food");
        check("getExpensesByCategory('Food') returns 3 expenses", foodExpenses.size() == 3);

        List<Expense> shoppingExpenses = trip.getExpensesByCategory("shopping"); // Case-insensitive
        check("getExpensesByCategory('shopping') is case-insensitive and returns 1 expense", shoppingExpenses.size() == 1);

        List<Expense> missingCatExpenses = trip.getExpensesByCategory("Medical");
        check("getExpensesByCategory with missing category returns empty list",
                missingCatExpenses != null && missingCatExpenses.isEmpty());

        check("getExpensesByCategory with null category returns empty list safely",
                trip.getExpensesByCategory(null) != null && trip.getExpensesByCategory(null).isEmpty());

        // Stream sum by category
        double foodCost = trip.calculateExpenseCostByCategory("Food");
        check("calculateExpenseCostByCategory('Food') is 3000.00 (1200 + 350 + 1450)",
                foodCost == 3000.0);

        double lodgingCost = trip.calculateExpenseCostByCategory("Lodging");
        check("calculateExpenseCostByCategory('Lodging') is 8000.00", lodgingCost == 8000.0);

        double zeroCost = trip.calculateExpenseCostByCategory("MissingCategory");
        check("calculateExpenseCostByCategory with non-existent category returns 0.00", zeroCost == 0.0);
    }

    private static void testCustomExceptions() {
        System.out.println("\n3. Testing Custom Exception Subclasses (Unit 3):");

        // Verify exception inheritance hierarchy
        InvalidTripDataException dataEx = new InvalidTripDataException("Test Data Exception");
        InvalidBudgetException budgetEx = new InvalidBudgetException("Test Budget Exception");

        check("InvalidTripDataException is subclass of Exception", dataEx instanceof Exception);
        check("InvalidBudgetException is subclass of InvalidTripDataException", budgetEx instanceof InvalidTripDataException);
        check("InvalidBudgetException is subclass of Exception", budgetEx instanceof Exception);

        // Exception message and cause constructor verification
        Throwable cause = new NumberFormatException("Bad number");
        InvalidBudgetException budgetWithCause = new InvalidBudgetException("Invalid budget input", cause);
        check("Exception message preserved", "Invalid budget input".equals(budgetWithCause.getMessage()));
        check("Exception cause preserved", cause.equals(budgetWithCause.getCause()));

        MainFrame frame = new MainFrame();
        frame.setShowDialogs(false);

        // 1. Missing required field -> InvalidTripDataException
        boolean caughtEmptyName = false;
        try {
            frame.validateTripInputsStrict("", "Manali", "10-10-2026", "15-10-2026", "20000");
        } catch (InvalidTripDataException e) {
            caughtEmptyName = true;
            check("Empty name throws InvalidTripDataException with correct message",
                    e.getMessage().contains("All fields are required"));
        }
        check("Empty name threw InvalidTripDataException", caughtEmptyName);

        // 2. Missing destination -> InvalidTripDataException
        boolean caughtEmptyDest = false;
        try {
            frame.validateTripInputsStrict("Trek", "   ", "10-10-2026", "15-10-2026", "20000");
        } catch (InvalidTripDataException e) {
            caughtEmptyDest = true;
        }
        check("Empty destination threw InvalidTripDataException", caughtEmptyDest);

        // 3. Non-numeric budget -> InvalidBudgetException
        boolean caughtNonNumBudget = false;
        try {
            frame.validateTripInputsStrict("Trek", "Manali", "10-10-2026", "15-10-2026", "not_a_number");
        } catch (InvalidBudgetException e) {
            caughtNonNumBudget = true;
            check("Non-numeric budget throws InvalidBudgetException with number format message",
                    e.getMessage().contains("valid number"));
            check("InvalidBudgetException carries NumberFormatException cause",
                    e.getCause() instanceof NumberFormatException);
        } catch (InvalidTripDataException e) {
            // Should have been caught by the more specific InvalidBudgetException
            caughtNonNumBudget = false;
        }
        check("Non-numeric budget threw InvalidBudgetException", caughtNonNumBudget);

        // 4. Negative budget -> InvalidBudgetException
        boolean caughtNegBudget = false;
        try {
            frame.validateTripInputsStrict("Trek", "Manali", "10-10-2026", "15-10-2026", "-5000");
        } catch (InvalidBudgetException e) {
            caughtNegBudget = true;
            check("Negative budget message indicates greater than zero",
                    e.getMessage().contains("greater than zero"));
        } catch (InvalidTripDataException e) {
            caughtNegBudget = false;
        }
        check("Negative budget threw InvalidBudgetException", caughtNegBudget);

        // 5. Zero budget -> InvalidBudgetException
        boolean caughtZeroBudget = false;
        try {
            frame.validateTripInputsStrict("Trek", "Manali", "10-10-2026", "15-10-2026", "0");
        } catch (InvalidBudgetException e) {
            caughtZeroBudget = true;
        } catch (InvalidTripDataException e) {
            caughtZeroBudget = false;
        }
        check("Zero budget threw InvalidBudgetException", caughtZeroBudget);

        // 6. Invalid date format -> InvalidTripDataException
        boolean caughtBadDateFormat = false;
        try {
            frame.validateTripInputsStrict("Trek", "Manali", "2026-10-10", "2026-10-15", "20000");
        } catch (InvalidTripDataException e) {
            caughtBadDateFormat = true;
            check("Invalid date format message indicates DD-MM-YYYY",
                    e.getMessage().contains("DD-MM-YYYY"));
        }
        check("Invalid date format threw InvalidTripDataException", caughtBadDateFormat);

        // 7. Non-chronological date order -> InvalidTripDataException
        boolean caughtBadDateOrder = false;
        try {
            frame.validateTripInputsStrict("Trek", "Manali", "20-10-2026", "10-10-2026", "20000");
        } catch (InvalidTripDataException e) {
            caughtBadDateOrder = true;
            check("Bad date order message indicates earlier than start date",
                    e.getMessage().contains("earlier than start date"));
        }
        check("Bad date order threw InvalidTripDataException", caughtBadDateOrder);

        // 8. Valid inputs -> No exception thrown
        boolean noExThrown = true;
        try {
            frame.validateTripInputsStrict("Trek", "Manali", "10-10-2026", "20-10-2026", "25000.50");
        } catch (InvalidTripDataException e) {
            noExThrown = false;
        }
        check("Valid trip inputs throw no exception", noExThrown);

        // 9. Backward-compatibility of validateTripInputs() string return
        check("validateTripInputs() returns null for valid inputs",
                frame.validateTripInputs("Trek", "Manali", "10-10-2026", "20-10-2026", "25000.50") == null);
        check("validateTripInputs() returns error string for invalid budget",
                frame.validateTripInputs("Trek", "Manali", "10-10-2026", "20-10-2026", "-100") != null);

        frame.dispose();
    }

    private static void testMultithreadingAndSynchronization() throws Exception {
        System.out.println("\n4. Testing Multithreading & Synchronization (Unit 3):");

        // 1. Thread Synchronization Test
        ExportService.resetExportsCount();
        check("Initial exports count is 0", ExportService.getTotalExportsCount() == 0);

        int numThreads = 10;
        int incrementsPerThread = 50;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    ExportService.recordExport(); // Synchronized method
                }
            }, "SyncTestThread-" + i);
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join(); // Wait for all threads to terminate
        }

        check("Synchronized counter incremented correctly across 10 concurrent threads (500)",
                ExportService.getTotalExportsCount() == (numThreads * incrementsPerThread));

        // 2. Asynchronous Background Export Test
        Trip trip = new Trip("Kerala Backwaters", "Alleppey", "05-11-2026", "12-11-2026", 35000.0);
        trip.setTransport(new Flight("Delhi", "Kochi", 6000.0, 800.0));
        trip.addActivity(new Activity("Houseboat Cruise", "06-11-2026", 4500.0));
        trip.addExpense(new Expense("Food", "Kerala Sadya", 1200.0));

        File asyncExportFile = new File("test_async_itinerary.txt");
        if (asyncExportFile.exists()) {
            asyncExportFile.delete();
        }

        AtomicBoolean callbackInvoked = new AtomicBoolean(false);
        AtomicBoolean callbackSuccess = new AtomicBoolean(false);
        AtomicReference<String> callbackMessage = new AtomicReference<>("");

        Thread worker = ExportService.exportItineraryAsync(trip, asyncExportFile, (success, message) -> {
            callbackInvoked.set(true);
            callbackSuccess.set(success);
            callbackMessage.set(message);
        });

        check("Asynchronous export worker thread is not null", worker != null);
        check("Worker thread name is 'VoyageX-ExportWorker'", "VoyageX-ExportWorker".equals(worker.getName()));
        check("Worker thread is non-daemon", !worker.isDaemon());

        // Wait for worker thread to complete execution
        worker.join(5000);

        check("Worker thread has terminated (isAlive == false)", !worker.isAlive());
        check("Asynchronous exported file exists on disk", asyncExportFile.exists());
        check("Asynchronous exported file length > 0", asyncExportFile.length() > 0);

        // Wait brief moment for EDT callback invocation
        Thread.sleep(200);
        check("Asynchronous callback was invoked", callbackInvoked.get());
        check("Asynchronous callback reported success", callbackSuccess.get());
        check("Asynchronous callback message is informative",
                callbackMessage.get().contains("test_async_itinerary.txt"));

        // Verify written file content
        boolean foundTripName = false;
        boolean foundHouseboat = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(asyncExportFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Kerala Backwaters")) foundTripName = true;
                if (line.contains("Houseboat Cruise")) foundHouseboat = true;
            }
        }
        check("Exported file contains trip name", foundTripName);
        check("Exported file contains activity name", foundHouseboat);

        // Clean up test file
        if (asyncExportFile.exists()) {
            asyncExportFile.delete();
        }
    }

    private static void testUIIntegration() throws Exception {
        System.out.println("\n5. Testing MainFrame UI Integration with Phase 7 Features:");

        AtomicReference<MainFrame> frameRef = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame();
            frame.setShowDialogs(false);
            frameRef.set(frame);
        });

        MainFrame frame = frameRef.get();

        // Test handleCreateTrip() with invalid inputs triggers catch block and returns false
        SwingUtilities.invokeAndWait(() -> {
            frame.getTxtTripName().setText(""); // Invalid empty name
            frame.getTxtDestination().setText("Goa");
            frame.getTxtStartDate().setText("01-11-2026");
            frame.getTxtEndDate().setText("05-11-2026");
            frame.getTxtBudget().setText("20000");

            boolean created = frame.handleCreateTrip();
            check("handleCreateTrip() returns false when validation fails via custom exception", !created);

            frame.getTxtTripName().setText("Goa Trip");
            frame.getTxtBudget().setText("-500"); // Invalid negative budget
            boolean createdNeg = frame.handleCreateTrip();
            check("handleCreateTrip() returns false on negative budget via InvalidBudgetException", !createdNeg);

            frame.getTxtBudget().setText("25000"); // Valid
            boolean createdValid = frame.handleCreateTrip();
            check("handleCreateTrip() succeeds on valid inputs", createdValid);
        });

        // Test asynchronous export helper in MainFrame
        File uiAsyncFile = new File("test_ui_async_itinerary.txt");
        if (uiAsyncFile.exists()) {
            uiAsyncFile.delete();
        }

        AtomicBoolean uiCallbackInvoked = new AtomicBoolean(false);
        AtomicReference<Thread> uiThreadRef = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            // Select the created trip
            if (frame.getTripList().size() > 0) {
                frame.setSelectedTrip(frame.getTripList().get(0));
            }
            Thread t = frame.exportItineraryAsyncToFile(uiAsyncFile, (success, msg) -> {
                uiCallbackInvoked.set(success);
            });
            uiThreadRef.set(t);
        });

        Thread uiThread = uiThreadRef.get();
        if (uiThread != null) {
            uiThread.join(5000);
            Thread.sleep(200);
            check("MainFrame exportItineraryAsyncToFile created worker thread and finished",
                    uiAsyncFile.exists() && uiCallbackInvoked.get());
        } else {
            check("MainFrame exportItineraryAsyncToFile created worker thread", false);
        }

        if (uiAsyncFile.exists()) {
            uiAsyncFile.delete();
        }

        SwingUtilities.invokeAndWait(frame::dispose);
    }
}
