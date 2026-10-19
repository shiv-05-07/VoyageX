package exception;

/**
 * InvalidBudgetException is a specific custom exception subclass representing
 * illegal, negative, or non-numeric budget and cost amounts in VoyageX.
 * 
 * Demonstrates:
 * - Multi-level custom exception inheritance (InvalidBudgetException extends InvalidTripDataException).
 * - Exception hierarchy specialization (Syllabus Unit 3).
 */
public class InvalidBudgetException extends InvalidTripDataException {

    public InvalidBudgetException(String message) {
        super(message);
    }

    public InvalidBudgetException(String message, Throwable cause) {
        super(message, cause);
    }
}
