package model;

/**
 * Expense represents an incidental cost incurred during a trip (e.g., Food, Hotel, Souvenirs).
 * 
 * Demonstrates:
 * - Encapsulation: Private data members protected behind getters and setters.
 * - Constructor Overloading: Multiple constructors providing flexible instantiation.
 * - 'this' keyword: Constructor chaining and self-reference.
 */
public class Expense {
    private int id;
    private String category;
    private String description;
    private double amount;

    // Default constructor
    public Expense() {
        this(0, "General", "", 0.0);
    }

    // Overloaded constructor for new expenses before database persistence
    public Expense(String category, String description, double amount) {
        this(0, category, description, amount);
    }

    // Overloaded constructor including database ID
    public Expense(int id, String category, String description, double amount) {
        this.id = id;
        this.category = category;
        this.description = description;
        setAmount(amount);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            this.amount = 0.0;
        } else {
            this.amount = amount;
        }
    }

    @Override
    public String toString() {
        return "Expense [" + category + "]: " + description + " - Amount: " + amount;
    }
}
