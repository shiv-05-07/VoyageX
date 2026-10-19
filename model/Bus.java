package model;

/**
 * Bus represents road/bus travel in the VoyageX planner.
 * 
 * Demonstrates:
 * - Inheritance: Extends Transport superclass.
 * - 'super' keyword: Passes bus type and route details to Transport constructor.
 * - Method Overriding & Polymorphism: Implements calculateCost() with bus-specific logic (conditional AC surcharge).
 */
public class Bus extends Transport {
    private boolean isAc;
    private double acSurcharge;

    // Default constructor
    public Bus() {
        super("Bus", "", "", 0.0);
        this.isAc = false;
        this.acSurcharge = 0.0;
    }

    // Parameterized constructor using super() to invoke parent Transport constructor
    public Bus(String source, String destination, double baseFare, boolean isAc, double acSurcharge) {
        super("Bus", source, destination, baseFare);
        this.isAc = isAc;
        setAcSurcharge(acSurcharge);
    }

    // Overridden method providing bus-specific cost calculation
    // Applies acSurcharge only if the bus has AC selected
    @Override
    public double calculateCost() {
        if (isAc) {
            return getBaseFare() + acSurcharge;
        }
        return getBaseFare();
    }

    // Getters and Setters
    public boolean isAc() {
        return isAc;
    }

    public void setAc(boolean isAc) {
        this.isAc = isAc;
    }

    public double getAcSurcharge() {
        return acSurcharge;
    }

    public void setAcSurcharge(double acSurcharge) {
        if (acSurcharge < 0) {
            this.acSurcharge = 0.0;
        } else {
            this.acSurcharge = acSurcharge;
        }
    }

    @Override
    public String toString() {
        String acStatus = isAc ? "AC (+" + acSurcharge + ")" : "Non-AC";
        return "Bus [" + getSource() + " -> " + getDestination() 
                + ", Base Fare: " + getBaseFare() 
                + ", Type: " + acStatus 
                + ", Total Cost: " + calculateCost() + "]";
    }
}
