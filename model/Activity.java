package model;

import interfaces.Billable;

/**
 * Activity represents a planned event or tour in a trip (e.g., Museum Visit, Scuba Diving).
 * 
 * Demonstrates:
 * - Encapsulation: Private instance variables with public accessors and mutators.
 * - Constructor Overloading: Multiple constructors for different initialization needs.
 * - 'this' keyword: Differentiates instance variables from parameter names and supports constructor chaining.
 * - Interface Implementation: Implements Billable (Unit 2).
 */
public class Activity implements Billable {
    private int id;
    private String name;
    private String date;
    private double cost;

    // Default constructor
    public Activity() {
        this(0, "", "", 0.0);
    }

    // Overloaded constructor for new activities before database assignment
    public Activity(String name, String date, double cost) {
        this(0, name, date, cost);
    }

    // Overloaded constructor including database ID
    public Activity(int id, String name, String date, double cost) {
        this.id = id;
        this.name = name;
        this.date = date;
        setCost(cost);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        if (cost < 0) {
            this.cost = 0.0;
        } else {
            this.cost = cost;
        }
    }

    @Override
    public double calculateCost() {
        return getCost();
    }

    @Override
    public String toString() {
        return "Activity: " + name + " (Date: " + date + ") - Cost: " + cost;
    }
}
