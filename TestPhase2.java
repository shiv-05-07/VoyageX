import model.Trip;
import ui.DatePickerDialog;
import ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * TestPhase2 tests all Phase 2 Swing GUI requirements:
 * 1. MainFrame initialization and window title
 * 2. CardLayout screen navigation (HOME, CREATE, VIEW)
 * 3. Input validation logic:
 *    - Empty fields check
 *    - Non-numeric budget check
 *    - Zero/negative budget check
 *    - Invalid date format check
 *    - End date before start date check
 *    - Valid input acceptance
 * 4. Model integration and in-memory session list (ArrayList<Trip>)
 * 5. JTable data population and table model synchronization
 */
public class TestPhase2 {
    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("   VoyageX Phase 2: Swing GUI Test Suite         ");
        System.out.println("==================================================\n");

        // Run GUI tests on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame();

            testWindowProperties(frame);
            testScreenNavigation(frame);
            testTodayButtonBehavior(frame);
            testInputValidation(frame);
            testTripCreationAndTableUpdates(frame);

            frame.dispose(); // Clean up native window resources
        });

        System.out.println("\n--------------------------------------------------");
        System.out.println("Test Summary: " + testsPassed + " / " + testsTotal + " checks passed.");
        if (testsPassed == testsTotal) {
            System.out.println(">>> ALL PHASE 2 CHECKS PASSED SUCCESSFULLY! <<<");
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

    private static void testWindowProperties(MainFrame frame) {
        System.out.println("1. Testing MainFrame Window Properties & DatePicker Components:");
        check("Window title is 'VoyageX — Travel Planner'", "VoyageX — Travel Planner".equals(frame.getTitle()));
        check("Trip list is initially empty", frame.getTripList().isEmpty());
        check("Table model has 5 columns", frame.getTableModel().getColumnCount() == 5);
        check("Table column 0 is 'Trip Name'", "Trip Name".equals(frame.getTableModel().getColumnName(0)));
        check("Table column 4 is 'Budget (INR)'", "Budget (INR)".equals(frame.getTableModel().getColumnName(4)));
        check("Start Date Pick button exists", frame.getBtnPickStartDate() != null);
        check("End Date Pick button exists", frame.getBtnPickEndDate() != null);
        System.out.println();
    }

    private static void testScreenNavigation(MainFrame frame) {
        System.out.println("2. Testing Screen Navigation via CardLayout:");
        // Test switching to CREATE screen
        frame.showScreen(MainFrame.SCREEN_CREATE);
        check("Switched to CREATE screen without error", true);

        // Test switching to VIEW screen
        frame.showScreen(MainFrame.SCREEN_VIEW);
        check("Switched to VIEW screen and refreshed table without error", true);

        // Test switching back to HOME screen
        frame.showScreen(MainFrame.SCREEN_HOME);
        check("Switched back to HOME screen without error", true);
        System.out.println();
    }

    private static void testTodayButtonBehavior(MainFrame frame) {
        System.out.println("3. Testing Today Button in DatePickerDialog & Field Integration:");
        String expectedToday = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

        // Test Today button on Start Date picker
        DatePickerDialog startDialog = new DatePickerDialog(frame, "");
        check("Start Date DatePickerDialog Today button exists", startDialog.getBtnToday() != null);
        startDialog.selectToday();
        check("Start Date DatePickerDialog returns today's date (" + expectedToday + ")", 
                expectedToday.equals(startDialog.getSelectedDate()));
        frame.getTxtStartDate().setText(startDialog.getSelectedDate());
        check("Start Date text field populated with today's date", 
                expectedToday.equals(frame.getTxtStartDate().getText()));

        // Test Today button on End Date picker
        DatePickerDialog endDialog = new DatePickerDialog(frame, "");
        check("End Date DatePickerDialog Today button exists", endDialog.getBtnToday() != null);
        endDialog.selectToday();
        check("End Date DatePickerDialog returns today's date (" + expectedToday + ")", 
                expectedToday.equals(endDialog.getSelectedDate()));
        frame.getTxtEndDate().setText(endDialog.getSelectedDate());
        check("End Date text field populated with today's date", 
                expectedToday.equals(frame.getTxtEndDate().getText()));

        // When both start and end date are today (valid single-day trip)
        String sameDayValidation = frame.validateTripInputs("Day Trip", "Nearby", 
                frame.getTxtStartDate().getText(), frame.getTxtEndDate().getText(), "5000");
        check("Same-day trip using Today button passes date validation", sameDayValidation == null);

        System.out.println();
    }

    private static void testInputValidation(MainFrame frame) {
        System.out.println("4. Testing Input Validation Rules (dd-MM-yyyy Format):");

        // Case 1: Empty fields
        String emptyErr = frame.validateTripInputs("", "Goa", "01-11-2026", "07-11-2026", "20000");
        check("Catches empty trip name", emptyErr != null && emptyErr.contains("All fields are required"));

        String emptyDestErr = frame.validateTripInputs("Goa Tour", "", "01-11-2026", "07-11-2026", "20000");
        check("Catches empty destination", emptyDestErr != null && emptyDestErr.contains("All fields are required"));

        String emptyDateErr = frame.validateTripInputs("Goa Tour", "Goa", "", "07-11-2026", "20000");
        check("Catches empty start date", emptyDateErr != null && emptyDateErr.contains("All fields are required"));

        // Case 2: Invalid budget (non-numeric, zero, negative)
        String nonNumBudgetErr = frame.validateTripInputs("Goa Tour", "Goa", "01-11-2026", "07-11-2026", "abc");
        check("Catches non-numeric budget", nonNumBudgetErr != null && nonNumBudgetErr.contains("valid number"));

        String zeroBudgetErr = frame.validateTripInputs("Goa Tour", "Goa", "01-11-2026", "07-11-2026", "0");
        check("Catches zero budget", zeroBudgetErr != null && zeroBudgetErr.contains("greater than zero"));

        String negBudgetErr = frame.validateTripInputs("Goa Tour", "Goa", "01-11-2026", "07-11-2026", "-5000");
        check("Catches negative budget", negBudgetErr != null && negBudgetErr.contains("greater than zero"));

        // Case 3: Invalid date format (testing rejection of old yyyy-MM-dd and invalid calendar dates)
        String oldFormatErr = frame.validateTripInputs("Goa Tour", "Goa", "2026-11-01", "2026-11-07", "20000");
        check("Catches old YYYY-MM-DD format as invalid", oldFormatErr != null && oldFormatErr.contains("DD-MM-YYYY"));

        String slashFormatErr = frame.validateTripInputs("Goa Tour", "Goa", "01/11/2026", "07/11/2026", "20000");
        check("Catches slash format (01/11/2026) as invalid", slashFormatErr != null && slashFormatErr.contains("DD-MM-YYYY"));

        String invalidDateValueErr = frame.validateTripInputs("Goa Tour", "Goa", "30-02-2026", "05-03-2026", "20000");
        check("Catches invalid calendar date (Feb 30)", invalidDateValueErr != null && invalidDateValueErr.contains("DD-MM-YYYY"));

        // Case 4: Chronological date order (end before start)
        String dateOrderErr = frame.validateTripInputs("Goa Tour", "Goa", "10-11-2026", "05-11-2026", "20000");
        check("Catches end date before start date", dateOrderErr != null && dateOrderErr.contains("earlier than start date"));

        // Case 5: Valid inputs in DD-MM-YYYY format
        String validRes = frame.validateTripInputs("Goa Tour", "Goa", "01-11-2026", "07-11-2026", "25000.0");
        check("Valid DD-MM-YYYY inputs return null (no error)", validRes == null);

        System.out.println();
    }

    private static void testTripCreationAndTableUpdates(MainFrame frame) {
        System.out.println("5. Testing Trip Creation & Session Table Synchronization:");

        // Add first trip
        Trip trip1 = new Trip("Goa Beach Trip", "Goa", "01-11-2026", "07-11-2026", 25000.0);
        frame.addTrip(trip1);

        List<Trip> trips = frame.getTripList();
        DefaultTableModel model = frame.getTableModel();

        check("Trip list size is 1", trips.size() == 1);
        check("Table row count is 1", model.getRowCount() == 1);
        check("Table row 0 Trip Name matches", "Goa Beach Trip".equals(model.getValueAt(0, 0)));
        check("Table row 0 Destination matches", "Goa".equals(model.getValueAt(0, 1)));
        check("Table row 0 Start Date matches (01-11-2026)", "01-11-2026".equals(model.getValueAt(0, 2)));
        check("Table row 0 End Date matches (07-11-2026)", "07-11-2026".equals(model.getValueAt(0, 3)));
        check("Table row 0 Budget formatted", "25000.00".equals(model.getValueAt(0, 4)));

        // Add second trip
        Trip trip2 = new Trip("Manali Mountain Trek", "Manali", "15-12-2026", "22-12-2026", 18500.0);
        frame.addTrip(trip2);

        check("Trip list size is 2", trips.size() == 2);
        check("Table row count is 2", model.getRowCount() == 2);
        check("Table row 1 Trip Name matches", "Manali Mountain Trek".equals(model.getValueAt(1, 0)));
        check("Table row 1 Destination matches", "Manali".equals(model.getValueAt(1, 1)));
        check("Table row 1 Budget formatted", "18500.00".equals(model.getValueAt(1, 4)));

        // Verify form clearing method
        frame.getTxtTripName().setText("Temporary");
        frame.clearCreateTripForm();
        check("clearCreateTripForm() resets fields", frame.getTxtTripName().getText().isEmpty());
    }
}
