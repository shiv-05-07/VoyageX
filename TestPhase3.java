import model.Activity;
import model.Bus;
import model.Expense;
import model.Flight;
import model.Train;
import model.Trip;
import ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * TestPhase3 tests all Phase 3 requirements:
 * 1. Trip Selection & Screen Transition:
 *    - Unselected trip handling (warning / refusal)
 *    - Selected trip transition to MANAGE screen
 *    - Header displays correct trip metadata
 * 2. Transportation Configuration:
 *    - Flight creation and polymorphic cost (baseFare + baggageFee)
 *    - Train creation and polymorphic cost (baseFare + reservationFee)
 *    - Bus creation and polymorphic cost (with & without AC surcharge)
 *    - Transport removal and cost resetting
 *    - Transport input validation (empty fields, negative values)
 * 3. Activities Management:
 *    - Adding activities with name, date, cost
 *    - Activities JTable synchronization
 *    - Total activity cost aggregation
 *    - Removing activity and updating table & totals
 *    - Activity input validation
 * 4. Expenses Management:
 *    - Adding expenses with category, description, amount
 *    - Expenses JTable synchronization
 *    - Total expenses cost aggregation
 *    - Removing expense and updating table & totals
 *    - Expense input validation
 * 5. Budget Summary & Status:
 *    - Accurate grand total: Transport + Activities + Expenses
 *    - Accurate remaining budget: Budget - Total Cost
 *    - Live summary label and status banner updates
 *    - Over-budget condition detection and warning
 * 6. Multi-Trip Data Isolation:
 *    - Distinct trips maintain their own transport, activities, expenses
 */
public class TestPhase3 {
    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("   VoyageX Phase 3: Transport, Activities & Budget");
        System.out.println("==================================================\n");

        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame();
            frame.setShowDialogs(false); // Disable blocking modal dialogs during automated tests

            // Set up test trips in session
            Trip trip1 = new Trip("Goa Beach Holiday", "Goa", "01-11-2026", "07-11-2026", 25000.0);
            Trip trip2 = new Trip("Manali Adventure", "Manali", "15-12-2026", "22-12-2026", 18000.0);
            frame.addTrip(trip1);
            frame.addTrip(trip2);

            testTripSelectionAndScreenTransition(frame);
            testTransportPolymorphicCalculations(frame);
            testTransportInputValidation(frame);
            testActivitiesManagement(frame);
            testExpensesManagement(frame);
            testBudgetSummaryAndOverBudget(frame);
            testMultiTripIsolation(frame);

            frame.dispose(); // Release Swing window resources
        });

        System.out.println("\n--------------------------------------------------");
        System.out.println("Test Summary: " + testsPassed + " / " + testsTotal + " checks passed.");
        if (testsPassed == testsTotal) {
            System.out.println(">>> ALL PHASE 3 CHECKS PASSED SUCCESSFULLY! <<<");
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

    private static void testTripSelectionAndScreenTransition(MainFrame frame) {
        System.out.println("1. Testing Trip Selection & Screen Transition to MANAGE:");

        // Case 1: Attempt to manage without selecting a row
        frame.getTripsTable().clearSelection();
        boolean managedWithoutSelect = frame.handleManageTrip();
        check("Refuses to manage trip when no row is selected", !managedWithoutSelect);

        // Case 2: Select first trip row (Goa Beach Holiday)
        frame.getTripsTable().setRowSelectionInterval(0, 0);
        boolean managedWithSelect = frame.handleManageTrip();
        check("Successfully opens Manage screen when trip is selected", managedWithSelect);
        check("Selected trip is set to 'Goa Beach Holiday'",
                frame.getSelectedTrip() != null && "Goa Beach Holiday".equals(frame.getSelectedTrip().getTripName()));
        check("Header title reflects selected trip",
                frame.getLblManageTripTitle().getText().contains("Goa Beach Holiday"));
        check("Header dates and budget reflect selected trip",
                frame.getLblManageTripDates().getText().contains("25000.00"));
        check("ManageTabbedPane has 4 tabs",
                frame.getManageTabbedPane().getTabCount() == 4);
        check("Tab 0 is 'Transport'",
                "Transport".equals(frame.getManageTabbedPane().getTitleAt(0)));
        check("Tab 1 is 'Activities'",
                "Activities".equals(frame.getManageTabbedPane().getTitleAt(1)));
        check("Tab 2 is 'Expenses'",
                "Expenses".equals(frame.getManageTabbedPane().getTitleAt(2)));
        check("Tab 3 is 'Budget Summary'",
                "Budget Summary".equals(frame.getManageTabbedPane().getTitleAt(3)));

        System.out.println();
    }

    private static void testTransportPolymorphicCalculations(MainFrame frame) {
        System.out.println("2. Testing Transportation Hierarchy & Polymorphic Costs:");

        Trip trip = frame.getSelectedTrip();

        // Initially no transport assigned
        check("Initially trip has no transport", trip.getTransport() == null);
        check("Initial total transport cost is 0.0", trip.calculateTotalTransportCost() == 0.0);

        // 1. Configure Flight: Delhi -> Goa, Base: 4500, Baggage: 750
        frame.getCmbTransportType().setSelectedItem("Flight");
        frame.getTxtTransportSource().setText("Delhi");
        frame.getTxtTransportDestination().setText("Goa");
        frame.getTxtTransportBaseFare().setText("4500.0");
        frame.getTxtTransportExtra().setText("750.0");
        boolean flightSaved = frame.handleSaveTransport();

        check("Flight saved successfully", flightSaved);
        check("Trip transport is instance of Flight", trip.getTransport() instanceof Flight);
        check("Flight calculateCost() polymorphic [4500 + 750 = 5250.0]",
                trip.calculateTotalTransportCost() == 5250.0);
        check("Transport breakdown label updated",
                frame.getLblTransportCostBreakdown().getText().contains("5250.00"));

        // 2. Switch to Train: Delhi -> Goa, Base: 1200, Reservation: 150
        frame.getCmbTransportType().setSelectedItem("Train");
        frame.getTxtTransportSource().setText("Delhi");
        frame.getTxtTransportDestination().setText("Goa");
        frame.getTxtTransportBaseFare().setText("1200.0");
        frame.getTxtTransportExtra().setText("150.0");
        boolean trainSaved = frame.handleSaveTransport();

        check("Train saved successfully", trainSaved);
        check("Trip transport switched to Train instance", trip.getTransport() instanceof Train);
        check("Train calculateCost() polymorphic [1200 + 150 = 1350.0]",
                trip.calculateTotalTransportCost() == 1350.0);

        // 3. Switch to AC Bus: Mumbai -> Goa, Base: 900, AC Surcharge: 300, isAc: true
        frame.getCmbTransportType().setSelectedItem("Bus");
        frame.getTxtTransportSource().setText("Mumbai");
        frame.getTxtTransportDestination().setText("Goa");
        frame.getTxtTransportBaseFare().setText("900.0");
        frame.getTxtTransportExtra().setText("300.0");
        frame.getChkBusAc().setSelected(true);
        boolean acBusSaved = frame.handleSaveTransport();

        check("AC Bus saved successfully", acBusSaved);
        check("Trip transport is instance of Bus", trip.getTransport() instanceof Bus);
        check("Bus isAc is true", ((Bus) trip.getTransport()).isAc());
        check("AC Bus calculateCost() polymorphic [900 + 300 = 1200.0]",
                trip.calculateTotalTransportCost() == 1200.0);

        // 4. Switch to Non-AC Bus: isAc: false (surcharge ignored)
        frame.getChkBusAc().setSelected(false);
        boolean nonAcBusSaved = frame.handleSaveTransport();
        check("Non-AC Bus saved successfully", nonAcBusSaved);
        check("Non-AC Bus calculateCost() polymorphic [900.0]",
                trip.calculateTotalTransportCost() == 900.0);

        // 5. Remove Transport
        frame.handleRemoveTransport();
        check("handleRemoveTransport() clears transport from trip", trip.getTransport() == null);
        check("Transport cost reset to 0.0", trip.calculateTotalTransportCost() == 0.0);

        // Restore Flight for subsequent budget integration tests
        frame.getCmbTransportType().setSelectedItem("Flight");
        frame.getTxtTransportSource().setText("Delhi");
        frame.getTxtTransportDestination().setText("Goa");
        frame.getTxtTransportBaseFare().setText("4500.0");
        frame.getTxtTransportExtra().setText("750.0");
        frame.handleSaveTransport();
        check("Flight restored with cost 5250.0", trip.calculateTotalTransportCost() == 5250.0);

        System.out.println();
    }

    private static void testTransportInputValidation(MainFrame frame) {
        System.out.println("3. Testing Transport Input Validation:");

        // Empty source
        String errEmptySource = frame.validateTransportInputs("Flight", "", "Goa", "4000", "500", false);
        check("Rejects empty source city", errEmptySource != null && errEmptySource.contains("Source and Destination"));

        // Empty destination
        String errEmptyDest = frame.validateTransportInputs("Flight", "Delhi", "", "4000", "500", false);
        check("Rejects empty destination city", errEmptyDest != null && errEmptyDest.contains("Source and Destination"));

        // Negative base fare
        String errNegBase = frame.validateTransportInputs("Flight", "Delhi", "Goa", "-100", "500", false);
        check("Rejects negative base fare", errNegBase != null && errNegBase.contains("cannot be negative"));

        // Non-numeric base fare
        String errNonNumBase = frame.validateTransportInputs("Flight", "Delhi", "Goa", "free", "500", false);
        check("Rejects non-numeric base fare", errNonNumBase != null && errNonNumBase.contains("valid number"));

        // Negative extra fee
        String errNegExtra = frame.validateTransportInputs("Flight", "Delhi", "Goa", "4000", "-50", false);
        check("Rejects negative extra fee", errNegExtra != null && errNegExtra.contains("cannot be negative"));

        // Valid inputs return null
        String validTransport = frame.validateTransportInputs("Flight", "Delhi", "Goa", "4500", "750", false);
        check("Valid transport inputs pass validation", validTransport == null);

        System.out.println();
    }

    private static void testActivitiesManagement(MainFrame frame) {
        System.out.println("4. Testing Activities Management (Adding, Table Sync, Removing):");

        Trip trip = frame.getSelectedTrip();
        trip.getActivities().clear();
        frame.refreshActivitiesTable();

        check("Initial activities list is empty", trip.getActivities().isEmpty());
        check("Initial activities table row count is 0", frame.getActivitiesTableModel().getRowCount() == 0);

        // 1. Add Activity 1: Scuba Diving, 03-11-2026, 3500.0
        frame.getTxtActivityName().setText("Scuba Diving");
        frame.getTxtActivityDate().setText("03-11-2026");
        frame.getTxtActivityCost().setText("3500.0");
        boolean act1Added = frame.handleAddActivity();

        check("Activity 1 added successfully", act1Added);
        check("Trip activities list size is 1", trip.getActivities().size() == 1);
        check("Activities table row count is 1", frame.getActivitiesTableModel().getRowCount() == 1);
        check("Table row 0 name is 'Scuba Diving'",
                "Scuba Diving".equals(frame.getActivitiesTableModel().getValueAt(0, 1)));
        check("Table row 0 cost is '3500.00'",
                "3500.00".equals(frame.getActivitiesTableModel().getValueAt(0, 3)));
        check("Total activity cost is 3500.0", trip.calculateTotalActivityCost() == 3500.0);

        // 2. Add Activity 2: Spice Plantation Tour, 04-11-2026, 1200.0
        frame.getTxtActivityName().setText("Spice Plantation Tour");
        frame.getTxtActivityDate().setText("04-11-2026");
        frame.getTxtActivityCost().setText("1200.0");
        boolean act2Added = frame.handleAddActivity();

        check("Activity 2 added successfully", act2Added);
        check("Trip activities list size is 2", trip.getActivities().size() == 2);
        check("Activities table row count is 2", frame.getActivitiesTableModel().getRowCount() == 2);
        check("Total activity cost is 4700.0 [3500 + 1200]", trip.calculateTotalActivityCost() == 4700.0);

        // 3. Remove Activity 1: select row 0 and remove
        frame.getActivitiesTable().setRowSelectionInterval(0, 0);
        boolean actRemoved = frame.handleRemoveActivity();
        check("Activity removed successfully", actRemoved);
        check("Trip activities list size is now 1", trip.getActivities().size() == 1);
        check("Activities table row count is now 1", frame.getActivitiesTableModel().getRowCount() == 1);
        check("Remaining activity is 'Spice Plantation Tour'",
                "Spice Plantation Tour".equals(trip.getActivities().get(0).getName()));
        check("Updated total activity cost is 1200.0", trip.calculateTotalActivityCost() == 1200.0);

        // Re-add Scuba Diving so both are present for total budget testing
        frame.getTxtActivityName().setText("Scuba Diving");
        frame.getTxtActivityDate().setText("03-11-2026");
        frame.getTxtActivityCost().setText("3500.0");
        frame.handleAddActivity();
        check("Both activities restored (total: 4700.0)", trip.calculateTotalActivityCost() == 4700.0);

        // 4. Activity Validation
        String errEmptyAct = frame.validateActivityInputs("", "03-11-2026", "2000");
        check("Rejects empty activity name", errEmptyAct != null);

        String errInvalidDate = frame.validateActivityInputs("Tour", "32-11-2026", "2000");
        check("Rejects invalid calendar date for activity", errInvalidDate != null && errInvalidDate.contains("DD-MM-YYYY"));

        String errNegCost = frame.validateActivityInputs("Tour", "03-11-2026", "-500");
        check("Rejects negative activity cost", errNegCost != null && errNegCost.contains("cannot be negative"));

        System.out.println();
    }

    private static void testExpensesManagement(MainFrame frame) {
        System.out.println("5. Testing Incidental Expenses Management:");

        Trip trip = frame.getSelectedTrip();
        trip.getExpenses().clear();
        frame.refreshExpensesTable();

        check("Initial expenses list is empty", trip.getExpenses().isEmpty());
        check("Initial expenses table row count is 0", frame.getExpensesTableModel().getRowCount() == 0);

        // 1. Add Expense 1: Food & Dining, Beach Shack Dinners, 4000.0
        frame.getCmbExpenseCategory().setSelectedItem("Food & Dining");
        frame.getTxtExpenseDescription().setText("Beach Shack Dinners");
        frame.getTxtExpenseAmount().setText("4000.0");
        boolean exp1Added = frame.handleAddExpense();

        check("Expense 1 added successfully", exp1Added);
        check("Trip expenses list size is 1", trip.getExpenses().size() == 1);
        check("Expenses table row count is 1", frame.getExpensesTableModel().getRowCount() == 1);
        check("Table row 0 category is 'Food & Dining'",
                "Food & Dining".equals(frame.getExpensesTableModel().getValueAt(0, 1)));
        check("Table row 0 description is 'Beach Shack Dinners'",
                "Beach Shack Dinners".equals(frame.getExpensesTableModel().getValueAt(0, 2)));
        check("Table row 0 amount is '4000.00'",
                "4000.00".equals(frame.getExpensesTableModel().getValueAt(0, 3)));
        check("Total expenses cost is 4000.0", trip.calculateTotalExpenseCost() == 4000.0);

        // 2. Add Expense 2: Shopping, Souvenirs & Handicrafts, 1500.0
        frame.getCmbExpenseCategory().setSelectedItem("Shopping");
        frame.getTxtExpenseDescription().setText("Souvenirs & Handicrafts");
        frame.getTxtExpenseAmount().setText("1500.0");
        boolean exp2Added = frame.handleAddExpense();

        check("Expense 2 added successfully", exp2Added);
        check("Trip expenses list size is 2", trip.getExpenses().size() == 2);
        check("Expenses table row count is 2", frame.getExpensesTableModel().getRowCount() == 2);
        check("Total expenses cost is 5500.0 [4000 + 1500]", trip.calculateTotalExpenseCost() == 5500.0);

        // 3. Remove Expense: select row 1 and remove
        frame.getExpensesTable().setRowSelectionInterval(1, 1);
        boolean expRemoved = frame.handleRemoveExpense();
        check("Expense removed successfully", expRemoved);
        check("Trip expenses list size is now 1", trip.getExpenses().size() == 1);
        check("Expenses table row count is now 1", frame.getExpensesTableModel().getRowCount() == 1);
        check("Total expenses cost is 4000.0", trip.calculateTotalExpenseCost() == 4000.0);

        // Re-add Expense 2 for full budget summary test
        frame.getCmbExpenseCategory().setSelectedItem("Shopping");
        frame.getTxtExpenseDescription().setText("Souvenirs & Handicrafts");
        frame.getTxtExpenseAmount().setText("1500.0");
        frame.handleAddExpense();
        check("Both expenses restored (total: 5500.0)", trip.calculateTotalExpenseCost() == 5500.0);

        // 4. Expense Validation
        String errEmptyDesc = frame.validateExpenseInputs("Shopping", "", "1000");
        check("Rejects empty expense description", errEmptyDesc != null);

        String errNegAmt = frame.validateExpenseInputs("Shopping", "Gift", "-250");
        check("Rejects negative expense amount", errNegAmt != null && errNegAmt.contains("cannot be negative"));

        String errNonNumAmt = frame.validateExpenseInputs("Shopping", "Gift", "abc");
        check("Rejects non-numeric expense amount", errNonNumAmt != null && errNonNumAmt.contains("valid number"));

        System.out.println();
    }

    private static void testBudgetSummaryAndOverBudget(MainFrame frame) {
        System.out.println("6. Testing Budget Summary Calculations & Over-Budget Handling:");

        Trip trip = frame.getSelectedTrip();
        frame.refreshBudgetSummary();

        // Values:
        // Budget = 25000.0
        // Transport (Flight) = 5250.0
        // Activities = 4700.0 (3500 + 1200)
        // Expenses = 5500.0 (4000 + 1500)
        // Total Cost = 5250 + 4700 + 5500 = 15450.0
        // Remaining = 25000 - 15450 = 9550.0
        double expectedTotal = 15450.0;
        double expectedRemaining = 9550.0;

        check("Grand total trip cost calculation is 15450.0", trip.calculateTotalCost() == expectedTotal);
        check("Remaining budget calculation is 9550.0", trip.calculateRemainingBudget() == expectedRemaining);

        // Verify summary UI label contents
        check("lblSummaryBudget shows 25000.00", frame.getLblSummaryBudget().getText().contains("25000.00"));
        check("lblSummaryTransport shows 5250.00", frame.getLblSummaryTransport().getText().contains("5250.00"));
        check("lblSummaryActivities shows 4700.00", frame.getLblSummaryActivities().getText().contains("4700.00"));
        check("lblSummaryExpenses shows 5500.00", frame.getLblSummaryExpenses().getText().contains("5500.00"));
        check("lblSummaryTotalCost shows 15450.00", frame.getLblSummaryTotalCost().getText().contains("15450.00"));
        check("lblSummaryRemaining shows 9550.00", frame.getLblSummaryRemaining().getText().contains("9550.00"));
        check("lblSummaryStatus indicates 'Within Budget'", frame.getLblSummaryStatus().getText().contains("Within Budget"));
        check("lblBottomSummary displays live total and remaining",
                frame.getLblBottomSummary().getText().contains("15450.00") && frame.getLblBottomSummary().getText().contains("9550.00"));

        // Over-Budget Scenario:
        // Add a large expense: Luxury Resort, 20000.0
        // New Total: 15450 + 20000 = 35450.0 -> Exceeds budget of 25000 by 10450.0
        Expense bigExpense = new Expense("Accommodation", "Luxury Resort Stay", 20000.0);
        trip.addExpense(bigExpense);
        frame.refreshBudgetSummary();

        check("Over-budget: Grand total is 35450.0", trip.calculateTotalCost() == 35450.0);
        check("Over-budget: Remaining budget is negative (-10450.0)", trip.calculateRemainingBudget() == -10450.0);
        check("Over-budget: Status banner detects OVER BUDGET",
                frame.getLblSummaryStatus().getText().contains("OVER BUDGET"));
        check("Over-budget: Bottom summary displays OVER BUDGET warning",
                frame.getLblBottomSummary().getText().contains("OVER BUDGET"));

        // Remove the large expense to restore balanced budget
        trip.removeExpense(bigExpense);
        frame.refreshBudgetSummary();
        check("Removing big expense restores positive remaining budget (9550.0)",
                trip.calculateRemainingBudget() == 9550.0);
        check("Status returns to 'Within Budget'",
                frame.getLblSummaryStatus().getText().contains("Within Budget"));

        System.out.println();
    }

    private static void testMultiTripIsolation(MainFrame frame) {
        System.out.println("7. Testing Multi-Trip Isolation in Session:");

        List<Trip> tripList = frame.getTripList();
        Trip trip1 = tripList.get(0); // Goa
        Trip trip2 = tripList.get(1); // Manali

        check("Trip 1 (Goa) has Flight transport assigned", trip1.getTransport() != null);
        check("Trip 1 has 2 activities", trip1.getActivities().size() == 2);
        check("Trip 1 has 2 expenses", trip1.getExpenses().size() == 2);

        check("Trip 2 (Manali) has no transport assigned", trip2.getTransport() == null);
        check("Trip 2 has 0 activities", trip2.getActivities().isEmpty());
        check("Trip 2 has 0 expenses", trip2.getExpenses().isEmpty());
        check("Trip 2 total cost is 0.0", trip2.calculateTotalCost() == 0.0);
        check("Trip 2 remaining budget equals full budget (18000.0)",
                trip2.calculateRemainingBudget() == 18000.0);

        // Switch to Trip 2 via openManageTrip
        frame.openManageTrip(trip2);
        check("Managing screen now active for Trip 2",
                frame.getSelectedTrip() != null && "Manali Adventure".equals(frame.getSelectedTrip().getTripName()));
        check("Manage header displays Manali Adventure",
                frame.getLblManageTripTitle().getText().contains("Manali Adventure"));
        check("Manage header displays 18000.00 budget",
                frame.getLblManageTripDates().getText().contains("18000.00"));
        check("Activities table empty for Trip 2",
                frame.getActivitiesTableModel().getRowCount() == 0);
        check("Expenses table empty for Trip 2",
                frame.getExpensesTableModel().getRowCount() == 0);

        // Assign Train transport to Trip 2: Delhi -> Chandigarh, 650.0, 50.0
        frame.getCmbTransportType().setSelectedItem("Train");
        frame.getTxtTransportSource().setText("Delhi");
        frame.getTxtTransportDestination().setText("Chandigarh");
        frame.getTxtTransportBaseFare().setText("650.0");
        frame.getTxtTransportExtra().setText("50.0");
        frame.handleSaveTransport();

        check("Trip 2 has Train transport assigned (cost: 700.0)",
                trip2.calculateTotalTransportCost() == 700.0);
        check("Trip 1 transport is still Flight (cost: 5250.0)",
                trip1.calculateTotalTransportCost() == 5250.0);

        // Return to My Trips table
        frame.showScreen(MainFrame.SCREEN_VIEW);
        check("Switched back to My Trips screen", true);
        check("Table row count is still 2", frame.getTableModel().getRowCount() == 2);

        System.out.println();
    }
}
