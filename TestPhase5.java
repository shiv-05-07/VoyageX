import model.Activity;
import model.Bus;
import model.Expense;
import model.Flight;
import model.Train;
import model.Trip;
import service.ExportService;
import ui.MainFrame;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * TestPhase5 tests all Phase 5 requirements:
 * 1. UI Integration:
 *    - "Export Itinerary" button present in Complete Itinerary tab
 *    - Button styling, text, and tooltip configured
 * 2. Filename Sanitization & Extension Handling:
 *    - Sanitizes illegal characters (\ / : * ? " < > |)
 *    - Collapses multiple whitespace to underscores
 *    - Trims leading/trailing dots/underscores
 *    - Default fallback to "itinerary" for empty/null names
 *    - Automatic '.txt' extension appending when omitted
 *    - Preserves existing '.txt' or '.TXT' extension without duplication
 * 3. File Handling & Output Content Verification:
 *    - Exports complete itinerary to plain-text file on disk using Core Java I/O
 *    - Verifies file exists, is non-empty, and readable
 *    - Verifies file content matches trip.generateItinerarySummary() byte-for-byte
 *    - Verifies presence of trip metadata, transport, activities, expenses, and budget status
 * 4. Dynamic Live Updates in Export:
 *    - Modifying transport, adding activities, and removing expenses immediately reflects in exported file
 * 5. Edge Cases & Error Handling:
 *    - Export with null trip or null file handled safely without crashing
 *    - Graceful non-blocking operation in headless test mode
 */
public class TestPhase5 {
    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("   VoyageX Phase 5: Export Itinerary to Text File ");
        System.out.println("==================================================\n");

        // 1. Pure service-level tests
        testFilenameSanitization();
        testExtensionEnforcement();
        testExportServiceFileWriting();

        // 2. UI integration and end-to-end export tests
        SwingUtilities.invokeAndWait(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setShowDialogs(false); // Disable blocking modal dialogs during automated tests

                Trip trip = new Trip("Rajasthan Heritage Tour", "Jaipur", "05-01-2027", "12-01-2027", 35000.0);
                frame.addTrip(trip);

                testUiExportButtonIntegration(frame, trip);
                testUiExportWorkflowAndLiveSync(frame, trip);
                testExportEdgeCases(frame);

                frame.dispose(); // Release Swing window resources
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("\n--------------------------------------------------");
        System.out.println("Test Summary: " + testsPassed + " / " + testsTotal + " checks passed.");
        if (testsPassed == testsTotal) {
            System.out.println(">>> ALL PHASE 5 CHECKS PASSED SUCCESSFULLY! <<<");
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

    private static void testFilenameSanitization() {
        System.out.println("1. Testing Filename Sanitization (ExportService.sanitizeFilename):");

        // Normal trip name with spaces
        String s1 = ExportService.sanitizeFilename("Goa Beach Holiday");
        check("Replaces spaces with underscores ('Goa_Beach_Holiday')", "Goa_Beach_Holiday".equals(s1));

        // Illegal filesystem characters (\ / : * ? " < > |)
        String s2 = ExportService.sanitizeFilename("Trip: Paris / Rome * 2026? <Summer> | \"Best\"");
        check("Strips illegal characters: \\ / : * ? < > | \"",
                "Trip_Paris_Rome_2026_Summer_Best".equals(s2));

        // Multiple spaces collapsed
        String s3 = ExportService.sanitizeFilename("Kashmir    Valley   Tour");
        check("Collapses multiple spaces into single underscore", "Kashmir_Valley_Tour".equals(s3));

        // Leading and trailing dots or underscores
        String s4 = ExportService.sanitizeFilename("...Manali_Trip...");
        check("Trims leading/trailing dots and underscores", "Manali_Trip".equals(s4));

        // Null and blank strings fallback to 'itinerary'
        check("Null trip name returns 'itinerary'", "itinerary".equals(ExportService.sanitizeFilename(null)));
        check("Empty trip name returns 'itinerary'", "itinerary".equals(ExportService.sanitizeFilename("")));
        check("Whitespace trip name returns 'itinerary'", "itinerary".equals(ExportService.sanitizeFilename("   ")));
        check("Only illegal characters returns 'itinerary'", "itinerary".equals(ExportService.sanitizeFilename("???:::***")));

        System.out.println();
    }

    private static void testExtensionEnforcement() {
        System.out.println("2. Testing Extension Enforcement (ExportService.ensureTxtExtension):");

        // File without extension
        File f1 = ExportService.ensureTxtExtension(new File("my_itinerary"));
        check("Appends .txt to filename without extension", f1.getName().equals("my_itinerary.txt"));

        // File with existing lowercase .txt
        File f2 = ExportService.ensureTxtExtension(new File("my_itinerary.txt"));
        check("Preserves existing .txt extension without duplicating", f2.getName().equals("my_itinerary.txt"));

        // File with uppercase .TXT
        File f3 = ExportService.ensureTxtExtension(new File("my_itinerary.TXT"));
        check("Preserves case-insensitive .TXT extension", f3.getName().equalsIgnoreCase("my_itinerary.txt"));

        // Nested directory path
        File f4 = ExportService.ensureTxtExtension(new File("exports/summer_trip"));
        check("Preserves directory structure while adding .txt",
                f4.getName().equals("summer_trip.txt") && f4.getPath().contains("exports"));

        // Null file safe handling
        check("Null file returns null", ExportService.ensureTxtExtension(null) == null);

        System.out.println();
    }

    private static void testExportServiceFileWriting() throws IOException {
        System.out.println("3. Testing File Writing & Content Integrity (ExportService.exportItinerary):");

        Trip trip = new Trip("Himalayan Trek", "Manali", "15-05-2027", "22-05-2027", 25000.0);
        trip.setTransport(new Train("Delhi", "Chandigarh", 650.0, 120.0));
        trip.addActivity(new Activity("River Rafting", "17-05-2027", 1800.0));
        trip.addActivity(new Activity("Campfire & Music", "19-05-2027", 800.0));
        trip.addExpense(new Expense("Food", "Local Himachali Meals", 2500.0));

        // Create temporary test file
        File tempFile = File.createTempFile("voyagex_test_export_", ".txt");
        tempFile.deleteOnExit();

        ExportService.exportItinerary(trip, tempFile);

        check("Exported file exists on disk", tempFile.exists());
        check("Exported file is not empty", tempFile.length() > 0);

        // Read contents from disk
        StringBuilder readContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                readContent.append(line).append("\n");
            }
        }

        String fileText = readContent.toString();
        String expectedText = trip.generateItinerarySummary();

        // Standardize line endings for assertion comparison across operating systems
        String normFileText = fileText.replace("\r\n", "\n").trim();
        String normExpectedText = expectedText.replace("\r\n", "\n").trim();

        check("Exported file content matches generateItinerarySummary()",
                normFileText.equals(normExpectedText));
        check("File contains VOYAGEX TRIP ITINERARY banner", fileText.contains("VOYAGEX TRIP ITINERARY"));
        check("File contains trip name 'Himalayan Trek'", fileText.contains("Himalayan Trek"));
        check("File contains destination 'Manali'", fileText.contains("Manali"));
        check("File contains Train transport details", fileText.contains("Train") && fileText.contains("Chandigarh"));
        check("File contains River Rafting activity", fileText.contains("River Rafting"));
        check("File contains Local Himachali Meals expense", fileText.contains("Local Himachali Meals"));
        check("File contains Allocated Budget (25000.00)", fileText.contains("25000.00"));
        check("File contains WITHIN BUDGET status", fileText.contains("WITHIN BUDGET"));

        // Clean up
        tempFile.delete();
        System.out.println();
    }

    private static void testUiExportButtonIntegration(MainFrame frame, Trip trip) {
        System.out.println("4. Testing UI Export Button in Complete Itinerary Tab:");

        frame.openManageTrip(trip);
        frame.getManageTabbedPane().setSelectedIndex(4); // Switch to Complete Itinerary

        check("Export Itinerary button is initialized", frame.getBtnExportItinerary() != null);
        check("Export Itinerary button text is 'Export Itinerary'",
                "Export Itinerary".equals(frame.getBtnExportItinerary().getText()));
        check("Export button tooltip is configured",
                frame.getBtnExportItinerary().getToolTipText() != null
                        && frame.getBtnExportItinerary().getToolTipText().contains(".txt"));
        check("Export button is visible and enabled",
                frame.getBtnExportItinerary().isVisible() && frame.getBtnExportItinerary().isEnabled());

        System.out.println();
    }

    private static void testUiExportWorkflowAndLiveSync(MainFrame frame, Trip trip) throws IOException {
        System.out.println("5. Testing UI Export Workflow & Live Synchronization:");

        frame.openManageTrip(trip);

        // 1. Initial configuration
        trip.setTransport(new Flight("Mumbai", "Jaipur", 4200.0, 750.0));
        trip.getActivities().clear();
        trip.addActivity(new Activity("Amber Fort Tour", "06-01-2027", 1200.0));
        trip.getExpenses().clear();
        trip.addExpense(new Expense("Shopping", "Handicrafts & Textiles", 3500.0));
        frame.refreshItinerary();

        File exportFile1 = File.createTempFile("ui_export_test_1_", ".txt");
        exportFile1.deleteOnExit();

        boolean export1Success = frame.exportItineraryToFile(exportFile1);
        check("exportItineraryToFile executes successfully", export1Success);
        check("Exported file 1 exists", exportFile1.exists());
        check("Exported file 1 contains Flight transport", readFileAsString(exportFile1).contains("Flight"));
        check("Exported file 1 contains Amber Fort Tour", readFileAsString(exportFile1).contains("Amber Fort Tour"));

        // 2. Dynamically modify trip components: switch to AC Bus, add activity, add expense
        trip.setTransport(new Bus("Delhi", "Jaipur", 800.0, true, 200.0));
        trip.addActivity(new Activity("Hawa Mahal Photography", "07-01-2027", 500.0));
        trip.addExpense(new Expense("Food", "Chokhi Dhani Dinner", 1800.0));
        frame.refreshItinerary();

        File exportFile2 = File.createTempFile("ui_export_test_2_", ".txt");
        exportFile2.deleteOnExit();

        boolean export2Success = frame.exportItineraryToFile(exportFile2);
        check("exportItineraryToFile executes successfully after dynamic updates", export2Success);

        String content2 = readFileAsString(exportFile2);
        check("Updated export reflects AC Bus transport", content2.contains("AC Bus"));
        check("Updated export contains new activity 'Hawa Mahal Photography'", content2.contains("Hawa Mahal Photography"));
        check("Updated export contains 2 scheduled activities", content2.contains("2. SCHEDULED ACTIVITIES (2)"));
        check("Updated export contains Chokhi Dhani Dinner expense", content2.contains("Chokhi Dhani Dinner"));
        check("Updated export contains 2 incidental expenses", content2.contains("3. INCIDENTAL EXPENSES (2)"));

        // Verify grand total calculation in export:
        // Transport: 800 + 200 = 1000.0; Activities: 1200 + 500 = 1700.0; Expenses: 3500 + 1800 = 5300.0
        // Total = 1000 + 1700 + 5300 = 8000.0; Remaining = 35000 - 8000 = 27000.0
        check("Updated export reflects grand total cost 8000.00", content2.contains("8000.00"));
        check("Updated export reflects remaining budget 27000.00", content2.contains("27000.00"));

        // Clean up
        exportFile1.delete();
        exportFile2.delete();
        System.out.println();
    }

    private static void testExportEdgeCases(MainFrame frame) {
        System.out.println("6. Testing Export Edge Cases & Defensive Error Handling:");

        // Case 1: Null trip handling in ExportService
        boolean caughtNullTrip = false;
        try {
            ExportService.exportItinerary(null, new File("dummy.txt"));
        } catch (IllegalArgumentException e) {
            caughtNullTrip = true;
        } catch (Exception e) {
            caughtNullTrip = false;
        }
        check("ExportService rejects null trip with IllegalArgumentException", caughtNullTrip);

        // Case 2: Null target file handling in ExportService
        boolean caughtNullFile = false;
        try {
            Trip dummy = new Trip("Dummy", "City", "01-01-2027", "02-01-2027", 5000.0);
            ExportService.exportItinerary(dummy, null);
        } catch (IllegalArgumentException e) {
            caughtNullFile = true;
        } catch (Exception e) {
            caughtNullFile = false;
        }
        check("ExportService rejects null target file with IllegalArgumentException", caughtNullFile);

        // Case 3: MainFrame export with no trip selected
        frame.setSelectedTrip(null);
        boolean noTripResult = frame.exportItineraryToFile(new File("test.txt"));
        check("exportItineraryToFile returns false when no trip is selected", !noTripResult);

        // Case 4: MainFrame export with null file
        Trip validTrip = new Trip("Valid", "City", "01-01-2027", "02-01-2027", 5000.0);
        frame.setSelectedTrip(validTrip);
        boolean nullFileResult = frame.exportItineraryToFile(null);
        check("exportItineraryToFile returns false when target file is null", !nullFileResult);

        System.out.println();
    }

    private static String readFileAsString(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
