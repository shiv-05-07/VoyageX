package interfaces;

/**
 * Billable defines a contract for any domain entity in VoyageX that incurs a financial cost.
 * 
 * Demonstrates:
 * - User-defined interfaces (Syllabus Unit 2).
 * - Polymorphism via interface reference (Syllabus Unit 1 & 2).
 * - Java 8 default methods in interfaces (Syllabus Unit 7).
 */
public interface Billable {

    /**
     * Calculates and returns the financial cost or amount associated with this item.
     * 
     * @return non-negative cost in INR.
     */
    double calculateCost();

    /**
     * Java 8 default method providing a standardized formatted currency string.
     * Demonstrates default methods in interfaces without forcing subclasses to override.
     * 
     * @return formatted currency string (e.g. "INR 2500.00").
     */
    default String getFormattedCost() {
        return String.format("INR %.2f", calculateCost());
    }
}
