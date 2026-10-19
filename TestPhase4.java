import model.Activity;
import model.Bus;
import model.Expense;
import model.Flight;
import model.Train;
import model.Trip;
import ui.MainFrame;

import javax.swing.SwingUtilities;
import java.util.List;

/**
 * TestPhase4 tests all Phase 4 requirements:
 * 1. Complete Trip Itinerary Generation:
 *    - Structured plain-text itinerary generated from model
 *    - Contains trip metadata (name, destination, dates, budget)
 *    - Contains transportation breakdown (Flight, Train, Bus with AC surcharge)
 *    - Contains scheduled activities list, dates, costs, and subtotal
 *    - Contains incidental expenses list, categories, descriptions, amounts, and subtotal
 *    - Contains financial & budget overview with grand total and remaining balance
 *    - Contains budget health status ("WITHIN BUDGET" vs. "OVER BUDGET")
 * 2. UI Integration & Live Synchronization:
 *    - "Complete Itinerary" tab present in manageTabbedPane (tab 4)
 *    - JTextArea txtItinerarySummary is read-only and displays updated summary
 *    - "View Full Itinerary" button navigates directly to Itinerary tab
 *    - Dynamic updates: adding/removing transport, activity, or expense updates itinerary immediately
 *    - "Refresh Itinerary" button refreshes content
 * 3. Input Validation & Edge Cases:
 *    - Trip validation: empty fields, non-numeric budget, non-positive budget, chronologically invalid dates
 *    - Transport validation: empty fields, negative base fare, non-numeric fare, negative extra fee
 *    - Activity validation: empty fields, non-numeric cost, negative cost, invalid calendar format
 *    - Activity date range check: rejects dates before trip start or after trip end; accepts boundary dates
 *    - Expense validation: empty description, negative amount, non-numeric amount
 *    - Safe removal handling when no table row is selected
 * 4. Model & State Integrity:
 *    - In-memory state updates reflect accurately across all components
 */
public class TestPhase4 {
    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("   VoyageX Phase 4: Itinerary & Finalization     ");
        System.out.println("==================================================\n");

        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame();
            frame.setShowDialogs(false); // Disable blocking modal dialogs during automated tests

            // Setup trips for testing
            Trip trip1 = new Trip("Kerala Backwaters", "Kochi", "10-12-2026", "16-12-2026", 30000.0);
            Trip trip2 = new Trip("Day Trip Agra", "Agra", "25-12-2026", "25-12-2026", 8000.0);
            frame.addTrip(trip1);
            frame.addTrip(trip2);

            testModelItineraryGeneration(trip1);
            testItineraryTabAndUiIntegration(frame, trip1);
            testDynamicItinerarySynchronization(frame, trip1);
            testActivityDateRangeValidation(frame, trip1, trip2);
            testComprehensiveInputValidationAndEdgeCases(frame, trip1);
            testViewItineraryDirectNavigation(frame);

            frame.dispose(); // Release Swing window resources
        });

        System.out.println("\n--------------------------------------------------");
        System.out.println("Test Summary: " + testsPassed + " / " + testsTotal + " checks passed.");
        if (testsPassed == testsTotal) {
            System.out.println(">>> ALL PHASE 4 CHECKS PASSED SUCCESSFULLY! <<<");
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

    private static void testModelItineraryGeneration(Trip trip) {
        System.out.println("1. Testing Model Itinerary Generation (trip.generateItinerarySummary):");

        // Set up trip components
        trip.setTransport(new Flight("Mumbai", "Kochi", 5500.0, 800.0));
        trip.getActivities().clear();
        trip.addActivity(new Activity("Houseboat Cruise", "11-12-2026", 7500.0));
        trip.addActivity(new Activity("Kathakali Dance Show", "13-12-2026", 1200.0));

        trip.getExpenses().clear();
        trip.addExpense(new Expense("Food & Dining", "Seafood Dinner", 2200.0));
        trip.addExpense(new Expense("Shopping", "Spices and Souvenirs", 1500.0));

        String summary = trip.generateItinerarySummary();
        check("Summary is not null and not empty", summary != null && !summary.isEmpty());
        check("Summary contains header banner", summary.contains("VOYAGEX TRIP ITINERARY"));
        check("Summary contains trip name", summary.contains("Kerala Backwaters"));
        check("Summary contains destination", summary.contains("Kochi"));
        check("Summary contains travel dates", summary.contains("10-12-2026 to 16-12-2026"));
        check("Summary contains allocated budget (30000.00)", summary.contains("30000.00"));

        // Transportation section
        check("Summary contains TRANSPORTATION section", summary.contains("1. TRANSPORTATION"));
        check("Summary contains Flight mode", summary.contains("Flight"));
        check("Summary contains route Mumbai -> Kochi", summary.contains("Mumbai -> Kochi"));
        check("Summary contains flight base fare (5500.00)", summary.contains("5500.00"));
        check("Summary contains baggage fee (800.00)", summary.contains("800.00"));
        check("Summary contains calculated transport cost (6300.00)", summary.contains("6300.00"));

        // Activities section
        check("Summary contains SCHEDULED ACTIVITIES section", summary.contains("2. SCHEDULED ACTIVITIES (2)"));
        check("Summary contains Houseboat Cruise", summary.contains("Houseboat Cruise"));
        check("Summary contains activity date 11-12-2026", summary.contains("11-12-2026"));
        check("Summary contains Kathakali Dance Show", summary.contains("Kathakali Dance Show"));
        check("Summary contains total activities cost (8700.00)", summary.contains("8700.00"));

        // Incidental Expenses section
        check("Summary contains INCIDENTAL EXPENSES section", summary.contains("3. INCIDENTAL EXPENSES (2)"));
        check("Summary contains Seafood Dinner", summary.contains("Seafood Dinner"));
        check("Summary contains Spices and Souvenirs", summary.contains("Spices and Souvenirs"));
        check("Summary contains total incidental cost (3700.00)", summary.contains("3700.00"));

        // Financial & Budget Overview
        // Total cost = 6300 + 8700 + 3700 = 18700.0; Remaining = 30000 - 18700 = 11300.0
        check("Summary contains grand total cost (18700.00)", summary.contains("18700.00"));
        check("Summary contains remaining budget (11300.00)", summary.contains("11300.00"));
        check("Summary indicates WITHIN BUDGET", summary.contains("WITHIN BUDGET"));

        // Test Over-Budget in Itinerary
        trip.addExpense(new Expense("Emergency", "Medical Expense", 20000.0));
        String overSummary = trip.generateItinerarySummary();
        check("Summary indicates OVER BUDGET when costs exceed budget", overSummary.contains("OVER BUDGET"));
        check("Summary shows negative remaining balance", overSummary.contains("-8700.00"));

        // Clean up emergency expense
        trip.getExpenses().remove(trip.getExpenses().size() - 1);
        System.out.println();
    }

    private static void testItineraryTabAndUiIntegration(MainFrame frame, Trip trip) {
        System.out.println("2. Testing Itinerary Tab & UI Integration in MainFrame:");

        frame.openManageTrip(trip);

        check("ManageTabbedPane has 5 tabs", frame.getManageTabbedPane().getTabCount() == 5);
        check("Tab index 4 is 'Complete Itinerary'",
                "Complete Itinerary".equals(frame.getManageTabbedPane().getTitleAt(4)));

        check("txtItinerarySummary component is initialized", frame.getTxtItinerarySummary() != null);
        check("txtItinerarySummary is read-only", !frame.getTxtItinerarySummary().isEditable());
        check("btnRefreshItinerary button is initialized", frame.getBtnRefreshItinerary() != null);
        check("btnViewItinerary button is initialized", frame.getBtnViewItinerary() != null);

        // Switch to Itinerary tab
        frame.getManageTabbedPane().setSelectedIndex(4);
        check("Active tab is index 4", frame.getManageTabbedPane().getSelectedIndex() == 4);

        String uiText = frame.getTxtItinerarySummary().getText();
        check("UI itinerary contains trip name", uiText.contains(trip.getTripName()));
        check("UI itinerary contains destination", uiText.contains(trip.getDestination()));
        check("UI itinerary matches model generateItinerarySummary()",
                uiText.equals(trip.generateItinerarySummary()));

        System.out.println();
    }

    private static void testDynamicItinerarySynchronization(MainFrame frame, Trip trip) {
        System.out.println("3. Testing Dynamic Itinerary Synchronization (Live Updates):");

        frame.openManageTrip(trip);
        double initialCost = trip.calculateTotalCost();

        // 1. Add new activity via UI and verify itinerary reflects it
        frame.getTxtActivityName().setText("Sunset Beach Walk");
        frame.getTxtActivityDate().setText("12-12-2026");
        frame.getTxtActivityCost().setText("500.0");
        boolean actAdded = frame.handleAddActivity();
        check("Sunset Beach Walk activity added", actAdded);

        String textAfterAct = frame.getTxtItinerarySummary().getText();
        check("Itinerary text contains 'Sunset Beach Walk' immediately",
                textAfterAct.contains("Sunset Beach Walk"));
        check("Itinerary updated scheduled activities count to 3",
                textAfterAct.contains("2. SCHEDULED ACTIVITIES (3)"));

        // 2. Add new expense via UI and verify itinerary reflects it
        frame.getCmbExpenseCategory().setSelectedItem("Local Transport");
        frame.getTxtExpenseDescription().setText("Auto Rickshaw Fares");
        frame.getTxtExpenseAmount().setText("600.0");
        boolean expAdded = frame.handleAddExpense();
        check("Auto Rickshaw expense added", expAdded);

        String textAfterExp = frame.getTxtItinerarySummary().getText();
        check("Itinerary text contains 'Auto Rickshaw Fares' immediately",
                textAfterExp.contains("Auto Rickshaw Fares"));
        check("Itinerary updated expenses count to 3",
                textAfterExp.contains("3. INCIDENTAL EXPENSES (3)"));

        // 3. Remove transport and verify itinerary reflects removal
        boolean transportRemoved = frame.handleRemoveTransport();
        check("Transport removed", transportRemoved);
        check("Trip transport is null", trip.getTransport() == null);

        String textAfterTransportRem = frame.getTxtItinerarySummary().getText();
        check("Itinerary shows 'No transportation arranged yet'",
                textAfterTransportRem.contains("No transportation arranged yet"));
        check("Itinerary shows transport cost INR 0.00",
                textAfterTransportRem.contains("Total Cost : INR 0.00"));

        // 4. Change transport to Bus with AC surcharge and verify itinerary
        frame.getCmbTransportType().setSelectedItem("Bus");
        frame.getTxtTransportSource().setText("Mumbai");
        frame.getTxtTransportDestination().setText("Kochi");
        frame.getTxtTransportBaseFare().setText("2500.0");
        frame.getTxtTransportExtra().setText("500.0");
        frame.getChkBusAc().setSelected(true);
        boolean busSaved = frame.handleSaveTransport();
        check("AC Bus saved", busSaved);

        String textAfterBus = frame.getTxtItinerarySummary().getText();
        check("Itinerary reflects 'Bus' mode", textAfterBus.contains("Mode       : Bus"));
        check("Itinerary reflects 'AC Bus'", textAfterBus.contains("AC Bus"));
        check("Itinerary reflects AC surcharge (500.00)", textAfterBus.contains("500.00"));
        check("Itinerary reflects bus cost (3000.00)", textAfterBus.contains("3000.00"));

        // 5. Remove an activity and verify itinerary update
        int actCountBefore = trip.getActivities().size();
        frame.getActivitiesTable().setRowSelectionInterval(0, 0); // select first activity
        frame.handleRemoveActivity();
        check("Activity count decreased by 1", trip.getActivities().size() == actCountBefore - 1);
        String textAfterActRem = frame.getTxtItinerarySummary().getText();
        check("Itinerary updated activities count to 2", textAfterActRem.contains("2. SCHEDULED ACTIVITIES (2)"));

        // 6. Test Refresh Itinerary button
        frame.getBtnRefreshItinerary().doClick();
        check("Refresh button updates text accurately",
                frame.getTxtItinerarySummary().getText().equals(trip.generateItinerarySummary()));

        System.out.println();
    }

    private static void testActivityDateRangeValidation(MainFrame frame, Trip multiDayTrip, Trip dayTrip) {
        System.out.println("4. Testing Activity Date Range Validation against Trip Dates:");

        // multiDayTrip: 10-12-2026 to 16-12-2026
        frame.setSelectedTrip(multiDayTrip);

        // Case 1: Date before trip start
        String errBefore = frame.validateActivityInputs("Early Bird", "09-12-2026", "500", multiDayTrip);
        check("Rejects activity date before trip start date (09-12-2026 < 10-12-2026)",
                errBefore != null && errBefore.contains("must fall within trip dates"));

        // Case 2: Date after trip end
        String errAfter = frame.validateActivityInputs("Late Night", "17-12-2026", "500", multiDayTrip);
        check("Rejects activity date after trip end date (17-12-2026 > 16-12-2026)",
                errAfter != null && errAfter.contains("must fall within trip dates"));

        // Case 3: Date on start date boundary (10-12-2026) -> must be valid
        String errOnStart = frame.validateActivityInputs("Arrival Dinner", "10-12-2026", "1000", multiDayTrip);
        check("Accepts activity date on exact start date (10-12-2026)", errOnStart == null);

        // Case 4: Date on end date boundary (16-12-2026) -> must be valid
        String errOnEnd = frame.validateActivityInputs("Farewell Lunch", "16-12-2026", "1000", multiDayTrip);
        check("Accepts activity date on exact end date (16-12-2026)", errOnEnd == null);

        // Case 5: Date in middle of trip (13-12-2026) -> must be valid
        String errMid = frame.validateActivityInputs("Mid-trip Tour", "13-12-2026", "1500", multiDayTrip);
        check("Accepts activity date inside trip window (13-12-2026)", errMid == null);

        // Case 6: Day trip where startDate == endDate (25-12-2026)
        frame.setSelectedTrip(dayTrip);
        String errDayTripSame = frame.validateActivityInputs("Taj Visit", "25-12-2026", "1200", dayTrip);
        check("Day trip: accepts activity on the same day (25-12-2026)", errDayTripSame == null);

        String errDayTripPrev = frame.validateActivityInputs("Early Taj", "24-12-2026", "1200", dayTrip);
        check("Day trip: rejects activity on the previous day (24-12-2026)",
                errDayTripPrev != null && errDayTripPrev.contains("must fall within trip dates"));

        String errDayTripNext = frame.validateActivityInputs("Late Taj", "26-12-2026", "1200", dayTrip);
        check("Day trip: rejects activity on the following day (26-12-2026)",
                errDayTripNext != null && errDayTripNext.contains("must fall within trip dates"));

        // Case 7: Invalid calendar format for activity
        String errInvalidCal = frame.validateActivityInputs("Invalid Date", "32-12-2026", "500", dayTrip);
        check("Rejects invalid calendar date (32-12-2026)",
                errInvalidCal != null && errInvalidCal.contains("DD-MM-YYYY"));

        // Reset selected trip to multiDayTrip
        frame.setSelectedTrip(multiDayTrip);
        System.out.println();
    }

    private static void testComprehensiveInputValidationAndEdgeCases(MainFrame frame, Trip trip) {
        System.out.println("5. Testing Comprehensive Input Validation & Edge Cases:");

        // 1. Trip Validation
        check("Trip: rejects null trip name",
                frame.validateTripInputs(null, "Delhi", "01-01-2027", "05-01-2027", "10000") != null);
        check("Trip: rejects empty trip name",
                frame.validateTripInputs("   ", "Delhi", "01-01-2027", "05-01-2027", "10000") != null);
        check("Trip: rejects empty destination",
                frame.validateTripInputs("Tour", "", "01-01-2027", "05-01-2027", "10000") != null);
        check("Trip: rejects negative budget",
                frame.validateTripInputs("Tour", "Delhi", "01-01-2027", "05-01-2027", "-500") != null);
        check("Trip: rejects zero budget",
                frame.validateTripInputs("Tour", "Delhi", "01-01-2027", "05-01-2027", "0") != null);
        check("Trip: rejects non-numeric budget",
                frame.validateTripInputs("Tour", "Delhi", "01-01-2027", "05-01-2027", "TenThousand") != null);
        check("Trip: rejects inverted dates",
                frame.validateTripInputs("Tour", "Delhi", "10-01-2027", "05-01-2027", "10000") != null);

        // 2. Transport Validation
        check("Transport: rejects empty source",
                frame.validateTransportInputs("Flight", "", "Kochi", "5000", "500", false) != null);
        check("Transport: rejects empty destination",
                frame.validateTransportInputs("Flight", "Mumbai", "", "5000", "500", false) != null);
        check("Transport: rejects non-numeric base fare",
                frame.validateTransportInputs("Flight", "Mumbai", "Kochi", "abc", "500", false) != null);
        check("Transport: rejects negative base fare",
                frame.validateTransportInputs("Flight", "Mumbai", "Kochi", "-100", "500", false) != null);
        check("Transport: rejects negative extra fee",
                frame.validateTransportInputs("Flight", "Mumbai", "Kochi", "5000", "-50", false) != null);
        check("Transport: rejects non-numeric extra fee",
                frame.validateTransportInputs("Flight", "Mumbai", "Kochi", "5000", "xyz", false) != null);

        // 3. Activity Validation
        check("Activity: rejects empty name",
                frame.validateActivityInputs("", "12-12-2026", "500", trip) != null);
        check("Activity: rejects non-numeric cost",
                frame.validateActivityInputs("Tour", "12-12-2026", "five hundred", trip) != null);
        check("Activity: rejects negative cost",
                frame.validateActivityInputs("Tour", "12-12-2026", "-100", trip) != null);

        // 4. Expense Validation
        check("Expense: rejects empty description",
                frame.validateExpenseInputs("Food", "", "500") != null);
        check("Expense: rejects non-numeric amount",
                frame.validateExpenseInputs("Food", "Dinner", "free") != null);
        check("Expense: rejects negative amount",
                frame.validateExpenseInputs("Food", "Dinner", "-250") != null);

        // 5. Safe handling when no row is selected
        frame.getActivitiesTable().clearSelection();
        boolean remActNoSel = frame.handleRemoveActivity();
        check("handleRemoveActivity handles unselected row safely (returns false without crashing)", !remActNoSel);

        frame.getExpensesTable().clearSelection();
        boolean remExpNoSel = frame.handleRemoveExpense();
        check("handleRemoveExpense handles unselected row safely (returns false without crashing)", !remExpNoSel);

        System.out.println();
    }

    private static void testViewItineraryDirectNavigation(MainFrame frame) {
        System.out.println("6. Testing View Full Itinerary Navigation from Trips List:");

        frame.showScreen(MainFrame.SCREEN_VIEW);

        // Case 1: Attempt to click View Itinerary when no trip is selected
        frame.getTripsTable().clearSelection();
        boolean viewNoSel = frame.handleViewItinerary();
        check("Refuses to open itinerary when no trip is selected", !viewNoSel);

        // Case 2: Select first trip and click View Itinerary
        frame.getTripsTable().setRowSelectionInterval(0, 0);
        boolean viewWithSel = frame.handleViewItinerary();
        check("Successfully handles View Full Itinerary for selected trip", viewWithSel);
        check("Navigates directly to MANAGE screen", frame.getSelectedTrip() != null);
        check("Sets active tab directly to Complete Itinerary (tab 4)",
                frame.getManageTabbedPane().getSelectedIndex() == 4);

        System.out.println();
    }
}
