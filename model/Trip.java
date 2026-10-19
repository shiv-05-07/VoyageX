package model;

import java.util.ArrayList;
import java.util.List;

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
 */
public class Trip {
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

    @Override
    public String toString() {
        return "Trip: " + tripName + " to " + destination 
                + " [" + startDate + " to " + endDate + "], Budget: " + budget 
                + ", Total Cost: " + calculateTotalCost() 
                + ", Remaining: " + calculateRemainingBudget();
    }
}
