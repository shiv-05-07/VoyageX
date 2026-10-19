package model;

import interfaces.Billable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trip is the central model class in VoyageX, representing a user's planned itinerary.
 * 
 * Demonstrates:
 * - Encapsulation: All trip data is kept private and manipulated through getters/setters.
 * - Constructor Overloading & Chaining: Uses this(...) to reuse constructor logic.
 * - Aggregation / Composition: A Trip has a Transport object and lists of Activity and Expense objects.
 * - Polymorphism: The transport field is of type Transport, allowing any subclass (Flight, Train, Bus)
 *   to be attached and its specific calculateCost() method to be called dynamically.
 * - Collections Framework: Uses ArrayList to maintain dynamic lists of activities and expenses.
 * - Interfaces: Implements Billable (Unit 2).
 * - Java 8 Stream API: Stream filtering, mapping, and reduction for costs and categories (Unit 7).
 */
public class Trip implements Billable {
    private int id;
    private String tripName;
    private String destination;
    private String startDate;
    private String endDate;
    private double budget;
    private Transport transport;
    private List<Activity> activities;
    private List<Expense> expenses;

    // Default constructor initializing collection lists
    public Trip() {
        this.id = 0;
        this.tripName = "";
        this.destination = "";
        this.startDate = "";
        this.endDate = "";
        this.budget = 0.0;
        this.transport = null;
        this.activities = new ArrayList<Activity>();
        this.expenses = new ArrayList<Expense>();
    }

    // Overloaded constructor for creating a trip before database persistence
    public Trip(String tripName, String destination, String startDate, String endDate, double budget) {
        this();
        this.tripName = tripName;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        setBudget(budget);
    }

    // Overloaded constructor including database ID
    public Trip(int id, String tripName, String destination, String startDate, String endDate, double budget) {
        this(tripName, destination, startDate, endDate, budget);
        this.id = id;
    }

    // Activity collection management methods
    public void addActivity(Activity activity) {
        if (activity != null) {
            this.activities.add(activity);
        }
    }

    public boolean removeActivity(Activity activity) {
        return this.activities.remove(activity);
    }

    // Expense collection management methods
    public void addExpense(Expense expense) {
        if (expense != null) {
            this.expenses.add(expense);
        }
    }

    public boolean removeExpense(Expense expense) {
        return this.expenses.remove(expense);
    }

    // Transport cost calculation demonstrating polymorphism
    public double calculateTotalTransportCost() {
        if (transport != null) {
            return transport.calculateCost(); // Dynamic method dispatch at runtime
        }
        return 0.0;
    }

    // Total activity cost calculation using a standard loop
    public double calculateTotalActivityCost() {
        double total = 0.0;
        for (Activity activity : activities) {
            if (activity != null) {
                total += activity.getCost();
            }
        }
        return total;
    }

    // Total expense cost calculation using a standard loop
    public double calculateTotalExpenseCost() {
        double total = 0.0;
        for (Expense expense : expenses) {
            if (expense != null) {
                total += expense.getAmount();
            }
        }
        return total;
    }

    // Grand total trip cost: Transport + Activities + Expenses
    public double calculateTotalCost() {
        return calculateTotalTransportCost() + calculateTotalActivityCost() + calculateTotalExpenseCost();
    }

    // Remaining budget: Budget - Total Cost
    public double calculateRemainingBudget() {
        return budget - calculateTotalCost();
    }

    /**
     * Implementation of Billable interface.
     * Demonstrates polymorphic cost calculation across all domain items (Unit 2).
     */
    @Override
    public double calculateCost() {
        return calculateTotalCost();
    }

    /**
     * Aggregates all billable items (Transport, Activities, Expenses) into a polymorphic list.
     * Demonstrates interface collections (Unit 2).
     */
    public List<Billable> getAllBillableItems() {
        List<Billable> items = new ArrayList<>();
        if (transport != null) {
            items.add(transport);
        }
        if (activities != null) {
            items.addAll(activities);
        }
        if (expenses != null) {
            items.addAll(expenses);
        }
        return items;
    }

    /**
     * Calculates total trip cost using the Java 8 Stream API and method references.
     * Demonstrates Stream API mapToDouble() and sum() (Unit 7).
     */
    public double calculateTotalCostViaStream() {
        return getAllBillableItems().stream()
                .mapToDouble(Billable::calculateCost)
                .sum();
    }

    /**
     * Filters activities occurring on a specific date using Java 8 Stream API.
     * Demonstrates stream filter() with lambda predicate and Collectors.toList() (Unit 7).
     * 
     * @param date date string in dd-MM-yyyy format
     * @return list of matching activities
     */
    public List<Activity> getActivitiesByDate(String date) {
        if (date == null || activities == null) {
            return Collections.emptyList();
        }
        return activities.stream()
                .filter(a -> a != null && date.equals(a.getDate()))
                .collect(Collectors.toList());
    }

    /**
     * Filters expenses belonging to a specific category using Java 8 Stream API.
     * Demonstrates stream filter() with case-insensitive category matching (Unit 7).
     * 
     * @param category category name (e.g. "Food", "Lodging")
     * @return list of matching expenses
     */
    public List<Expense> getExpensesByCategory(String category) {
        if (category == null || expenses == null) {
            return Collections.emptyList();
        }
        return expenses.stream()
                .filter(e -> e != null && category.equalsIgnoreCase(e.getCategory()))
                .collect(Collectors.toList());
    }

    /**
     * Calculates total expenditure for a specific category using Java 8 Stream API.
     * Demonstrates stream filter(), mapToDouble(), and sum() (Unit 7).
     * 
     * @param category category name to sum
     * @return sum of expenses in the category
     */
    public double calculateExpenseCostByCategory(String category) {
        if (category == null || expenses == null) {
            return 0.0;
        }
        return expenses.stream()
                .filter(e -> e != null && category.equalsIgnoreCase(e.getCategory()))
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        if (budget < 0) {
            this.budget = 0.0;
        } else {
            this.budget = budget;
        }
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        if (activities != null) {
            this.activities = activities;
        } else {
            this.activities = new ArrayList<Activity>();
        }
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        if (expenses != null) {
            this.expenses = expenses;
        } else {
            this.expenses = new ArrayList<Expense>();
        }
    }

    /**
     * Generates a complete, structured plain-text itinerary for the trip.
     * Demonstrates string building and data aggregation across model components.
     */
    public String generateItinerarySummary() {
        StringBuilder sb = new StringBuilder();
        String line = "======================================================================\n";
        String divider = "----------------------------------------------------------------------\n";

        sb.append(line);
        sb.append("                       VOYAGEX TRIP ITINERARY                         \n");
        sb.append(line);
        sb.append(String.format("Trip Name    : %s\n", tripName.isEmpty() ? "Untitled Trip" : tripName));
        sb.append(String.format("Destination  : %s\n", destination.isEmpty() ? "Unspecified" : destination));
        sb.append(String.format("Travel Dates : %s to %s\n", startDate, endDate));
        sb.append(String.format("Budget       : INR %.2f\n", budget));
        sb.append(divider);

        // 1. Transportation Section
        sb.append("1. TRANSPORTATION\n");
        if (transport != null) {
            sb.append(String.format("   Mode       : %s\n", transport.getTransportType()));
            sb.append(String.format("   Route      : %s -> %s\n", transport.getSource(), transport.getDestination()));
            sb.append(String.format("   Base Fare  : INR %.2f\n", transport.getBaseFare()));
            if (transport instanceof Flight) {
                sb.append(String.format("   Baggage Fee: INR %.2f\n", ((Flight) transport).getBaggageFee()));
            } else if (transport instanceof Train) {
                sb.append(String.format("   Reservation: INR %.2f\n", ((Train) transport).getReservationFee()));
            } else if (transport instanceof Bus) {
                Bus bus = (Bus) transport;
                sb.append(String.format("   Type       : %s\n", bus.isAc() ? "AC Bus" : "Non-AC Bus"));
                if (bus.isAc()) {
                    sb.append(String.format("   AC Surchg  : INR %.2f\n", bus.getAcSurcharge()));
                }
            }
            sb.append(String.format("   Total Cost : INR %.2f\n", transport.calculateCost()));
        } else {
            sb.append("   Status     : No transportation arranged yet.\n");
            sb.append("   Total Cost : INR 0.00\n");
        }
        sb.append(divider);

        // 2. Scheduled Activities Section
        sb.append(String.format("2. SCHEDULED ACTIVITIES (%d)\n", activities.size()));
        if (activities.isEmpty()) {
            sb.append("   No scheduled activities added yet.\n");
        } else {
            for (int i = 0; i < activities.size(); i++) {
                Activity act = activities.get(i);
                sb.append(String.format("   [%d] %s (%s) - INR %.2f\n", (i + 1), act.getName(), act.getDate(), act.getCost()));
            }
        }
        sb.append(String.format("   Total Activities Cost : INR %.2f\n", calculateTotalActivityCost()));
        sb.append(divider);

        // 3. Incidental Expenses Section
        sb.append(String.format("3. INCIDENTAL EXPENSES (%d)\n", expenses.size()));
        if (expenses.isEmpty()) {
            sb.append("   No incidental expenses recorded yet.\n");
        } else {
            for (int i = 0; i < expenses.size(); i++) {
                Expense exp = expenses.get(i);
                sb.append(String.format("   [%d] [%s] %s - INR %.2f\n", (i + 1), exp.getCategory(), exp.getDescription(), exp.getAmount()));
            }
        }
        sb.append(String.format("   Total Other Expenses  : INR %.2f\n", calculateTotalExpenseCost()));
        sb.append(divider);

        // 4. Financial Summary
        double totalCost = calculateTotalCost();
        double remaining = calculateRemainingBudget();
        sb.append("4. FINANCIAL & BUDGET SUMMARY\n");
        sb.append(String.format("   Allocated Budget  : INR %.2f\n", budget));
        sb.append(String.format("   Transport Cost    : INR %.2f\n", calculateTotalTransportCost()));
        sb.append(String.format("   Activities Cost   : INR %.2f\n", calculateTotalActivityCost()));
        sb.append(String.format("   Incidental Cost   : INR %.2f\n", calculateTotalExpenseCost()));
        sb.append(String.format("   Grand Total Cost  : INR %.2f\n", totalCost));
        sb.append(String.format("   Remaining Budget  : INR %.2f\n", remaining));
        if (remaining >= 0) {
            sb.append(String.format("   Budget Status     : WITHIN BUDGET (INR %.2f available)\n", remaining));
        } else {
            sb.append(String.format("   Budget Status     : OVER BUDGET by INR %.2f!\n", Math.abs(remaining)));
        }
        sb.append(line);

        return sb.toString();
    }

    @Override
    public String toString() {
        return "Trip: " + tripName + " to " + destination 
                + " [" + startDate + " to " + endDate + "], Budget: " + budget 
                + ", Total Cost: " + calculateTotalCost() 
                + ", Remaining: " + calculateRemainingBudget();
    }
}
