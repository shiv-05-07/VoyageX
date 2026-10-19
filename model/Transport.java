package model;

/**
 * Transport represents an abstract travel mode in the VoyageX planner.
 * 
 * Demonstrates:
 * - Abstraction: Cannot be instantiated directly; defines common template for subclasses.
 * - Encapsulation: Private fields with public getters and setters.
 * - Abstract Method: calculateCost() must be implemented by each concrete transport subclass.
 */
public abstract class Transport {
    private String transportType;
    private String source;
    private String destination;
    private double baseFare;

    // Default constructor
    public Transport() {
        this("General Transport", "", "", 0.0);
    }

    // Parameterized constructor demonstrating the 'this' keyword
    public Transport(String transportType, String source, String destination, double baseFare) {
        this.transportType = transportType;
        this.source = source;
        this.destination = destination;
        setBaseFare(baseFare);
    }

    // Abstract method: Every transport mode must calculate its own total cost
    public abstract double calculateCost();

    // Getters and Setters (Encapsulation)
    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(double baseFare) {
        if (baseFare < 0) {
            this.baseFare = 0.0;
        } else {
            this.baseFare = baseFare;
        }
    }

    @Override
    public String toString() {
        return transportType + " [" + source + " -> " + destination + ", Base Fare: " + baseFare + "]";
    }
}
