package exception;

/**
 * InvalidTripDataException is a custom checked exception representing validation
 * errors for trips, activities, expenses, or dates in VoyageX.
 * 
 * Demonstrates:
 * - Creating custom exception subclasses (Syllabus Unit 3).
 * - Checked exception hierarchy (extends java.lang.Exception).
 * - Constructor overloading and exception cause chaining (super(message, cause)).
 */
public class InvalidTripDataException extends Exception {

    public InvalidTripDataException(String message) {
        super(message);
    }

    public InvalidTripDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
