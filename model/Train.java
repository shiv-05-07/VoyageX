package model;

/**
 * Train represents railway travel in the VoyageX planner.
 * 
 * Demonstrates:
 * - Inheritance: Extends Transport superclass.
 * - 'super' keyword: Passes train type and route details to Transport constructor.
 * - Method Overriding & Polymorphism: Implements calculateCost() with train-specific logic (base fare + reservation fee).
 */
public class Train extends Transport {
    private double reservationFee;

    // Default constructor
    public Train() {
        super("Train", "", "", 0.0);
        this.reservationFee = 0.0;
    }

    // Parameterized constructor using super() to invoke parent Transport constructor
    public Train(String source, String destination, double baseFare, double reservationFee) {
        super("Train", source, destination, baseFare);
        setReservationFee(reservationFee);
    }

    // Overridden method providing train-specific cost calculation
    @Override
    public double calculateCost() {
        return getBaseFare() + reservationFee;
    }

    // Getters and Setters
    public double getReservationFee() {
        return reservationFee;
    }

    public void setReservationFee(double reservationFee) {
        if (reservationFee < 0) {
            this.reservationFee = 0.0;
        } else {
            this.reservationFee = reservationFee;
        }
    }

    @Override
    public String toString() {
        return "Train [" + getSource() + " -> " + getDestination() 
                + ", Base Fare: " + getBaseFare() 
                + ", Reservation Fee: " + reservationFee 
                + ", Total Cost: " + calculateCost() + "]";
    }
}
