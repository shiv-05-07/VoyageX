import model.Activity;
import model.Bus;
import model.Expense;
import model.Flight;
import model.Train;
import model.Transport;
import model.Trip;

/**
 * TestPhase1 verifies the core OOP models for VoyageX:
 * 1. Object creation and Encapsulation
 * 2. Inheritance and super constructor chaining
 * 3. Polymorphism and Dynamic Method Dispatch via Transport hierarchy
 * 4. Distinct cost calculations for Flight, Train, and Bus
 * 5. Trip composition, collections (ArrayList), total cost, and budget calculations
 */
public class TestPhase1 {
    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   VoyageX Phase 1: Core OOP Models Test Suite   ");
        System.out.println("==================================================\n");

        testEncapsulationAndBasicModels();
        testInheritanceAndTransportCosts();
        testPolymorphismDynamicDispatch();
        testTripIntegrationAndBudget();

        System.out.println("\n--------------------------------------------------");
        System.out.println("Test Summary: " + testsPassed + " / " + testsTotal + " checks passed.");
        if (testsPassed == testsTotal) {
            System.out.println(">>> ALL PHASE 1 CHECKS PASSED SUCCESSFULLY! <<<");
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

    private static void testEncapsulationAndBasicModels() {
        System.out.println("1. Testing Encapsulation, Getters, and Setters:");

        // Test Activity
        Activity act = new Activity("Scuba Diving", "2026-11-02", 3500.0);
        check("Activity name getter", "Scuba Diving".equals(act.getName()));
        check("Activity date getter", "2026-11-02".equals(act.getDate()));
        check("Activity cost getter", act.getCost() == 3500.0);

        act.setCost(-500.0); // Encapsulation validation rule (negative values reset to 0)
        check("Activity negative cost validation", act.getCost() == 0.0);
        act.setCost(3500.0);

        // Test Expense
        Expense exp = new Expense("Lodging", "Beach Resort", 8000.0);
        check("Expense category getter", "Lodging".equals(exp.getCategory()));
        check("Expense description getter", "Beach Resort".equals(exp.getDescription()));
        check("Expense amount getter", exp.getAmount() == 8000.0);

        exp.setAmount(-200.0); // Encapsulation validation rule
        check("Expense negative amount validation", exp.getAmount() == 0.0);
        exp.setAmount(8000.0);

        System.out.println();
    }

    private static void testInheritanceAndTransportCosts() {
        System.out.println("2. Testing Inheritance & calculateCost() Implementations:");

        // Flight: baseFare + baggageFee
        Flight flight = new Flight("Mumbai", "Goa", 4500.0, 750.0);
        check("Flight inherits source from Transport", "Mumbai".equals(flight.getSource()));
        check("Flight inherits destination from Transport", "Goa".equals(flight.getDestination()));
        check("Flight inherits base fare", flight.getBaseFare() == 4500.0);
        check("Flight baggage fee getter", flight.getBaggageFee() == 750.0);
        check("Flight calculateCost() [4500 + 750 = 5250]", flight.calculateCost() == 5250.0);

        // Train: baseFare + reservationFee
        Train train = new Train("Mumbai", "Goa", 1200.0, 150.0);
        check("Train inherits route via super", "Goa".equals(train.getDestination()));
        check("Train reservation fee getter", train.getReservationFee() == 150.0);
        check("Train calculateCost() [1200 + 150 = 1350]", train.calculateCost() == 1350.0);

        // Bus: baseFare + acSurcharge (if AC)
        Bus acBus = new Bus("Mumbai", "Goa", 900.0, true, 300.0);
        check("AC Bus isAc check", acBus.isAc());
        check("AC Bus calculateCost() [900 + 300 = 1200]", acBus.calculateCost() == 1200.0);

        Bus nonAcBus = new Bus("Mumbai", "Goa", 900.0, false, 300.0);
        check("Non-AC Bus isAc check", !nonAcBus.isAc());
        check("Non-AC Bus calculateCost() [900 only]", nonAcBus.calculateCost() == 900.0);

        System.out.println();
    }

    private static void testPolymorphismDynamicDispatch() {
        System.out.println("3. Testing Polymorphism & Dynamic Method Dispatch:");

        // Transport array holding different concrete subclasses
        Transport[] transports = new Transport[] {
            new Flight("Delhi", "Jaipur", 3000.0, 500.0), // 3500.0
            new Train("Delhi", "Jaipur", 600.0, 80.0),     // 680.0
            new Bus("Delhi", "Jaipur", 400.0, true, 120.0) // 520.0
        };

        double[] expectedCosts = {3500.0, 680.0, 520.0};

        for (int i = 0; i < transports.length; i++) {
            Transport t = transports[i];
            // Even though the reference type is Transport, Java dynamically invokes
            // the overridden calculateCost() in Flight, Train, or Bus at runtime.
            double cost = t.calculateCost();
            System.out.println("  " + t.getTransportType() + " cost: " + cost);
            check("Polymorphic cost for " + t.getTransportType(), cost == expectedCosts[i]);
        }

        System.out.println();
    }

    private static void testTripIntegrationAndBudget() {
        System.out.println("4. Testing Trip Composition, Collections, and Budget Calculations:");

        Trip trip = new Trip("Goa Vacation", "Goa", "2026-11-01", "2026-11-07", 25000.0);

        // Assign Flight as transport (Polymorphism)
        Flight flight = new Flight("Mumbai", "Goa", 4500.0, 750.0); // Cost = 5250.0
        trip.setTransport(flight);

        // Add Activities
        trip.addActivity(new Activity("Scuba Diving", "2026-11-02", 3500.0));
        trip.addActivity(new Activity("Dolphin Cruise", "2026-11-03", 1200.0));

        // Add Expenses
        trip.addExpense(new Expense("Lodging", "Beach Resort", 8000.0));
        trip.addExpense(new Expense("Food", "Seafood Dinner", 1500.0));

        check("Trip transport cost is 5250.0", trip.calculateTotalTransportCost() == 5250.0);
        check("Trip activity cost is 4700.0 (3500 + 1200)", trip.calculateTotalActivityCost() == 4700.0);
        check("Trip expense cost is 9500.0 (8000 + 1500)", trip.calculateTotalExpenseCost() == 9500.0);

        // Grand total: 5250 + 4700 + 9500 = 19450.0
        check("Trip total cost is 19450.0", trip.calculateTotalCost() == 19450.0);

        // Remaining: 25000.0 - 19450.0 = 5550.0
        check("Trip remaining budget is 5550.0", trip.calculateRemainingBudget() == 5550.0);

        // Switch transport polymorphically to Train and verify dynamic recalculation
        Train train = new Train("Mumbai", "Goa", 1200.0, 150.0); // Cost = 1350.0
        trip.setTransport(train);
        // New total: 1350 + 4700 + 9500 = 15550.0
        // New remaining: 25000.0 - 15550.0 = 9450.0
        check("Switched transport cost is 1350.0", trip.calculateTotalTransportCost() == 1350.0);
        check("Updated trip total cost is 15550.0", trip.calculateTotalCost() == 15550.0);
        check("Updated remaining budget is 9450.0", trip.calculateRemainingBudget() == 9450.0);

        // Verify toString output is non-empty
        check("Trip toString is descriptive", trip.toString().contains("Goa Vacation"));
    }
}
