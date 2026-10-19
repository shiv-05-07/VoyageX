package model;

/**
 * Flight represents air travel in the VoyageX planner.
 * 
 * Demonstrates:
 * - Inheritance: Extends Transport superclass.
 * - 'super' keyword: Calls the superclass constructor to initialize inherited fields.
 * - Method Overriding & Polymorphism: Implements calculateCost() with flight-specific logic (base fare + baggage fee).
 */
public class Flight extends Transport {
    private double baggageFee;

    // Default constructor
    public Flight() {
        super("Flight", "", "", 0.0);
        this.baggageFee = 0.0;
    }

    // Parameterized constructor using super() to invoke parent Transport constructor
    public Flight(String source, String destination, double baseFare, double baggageFee) {
        super("Flight", source, destination, baseFare);
        setBaggageFee(baggageFee);
    }

    // Overridden method providing flight-specific cost calculation
    @Override
    public double calculateCost() {
        return getBaseFare() + baggageFee;
    }

    // Getters and Setters
    public double getBaggageFee() {
        return baggageFee;
    }

    public void setBaggageFee(double baggageFee) {
        if (baggageFee < 0) {
            this.baggageFee = 0.0;
        } else {
            this.baggageFee = baggageFee;
        }
    }

    @Override
    public String toString() {
        return "Flight [" + getSource() + " -> " + getDestination() 
                + ", Base Fare: " + getBaseFare() 
                + ", Baggage Fee: " + baggageFee 
                + ", Total Cost: " + calculateCost() + "]";
    }
}
