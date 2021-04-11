
package at.uibk.dps.exception;

/**
 * Custom exception which is thrown when latestStartingTime constraint is missed
 */
public class MemoryExceededException extends Exception {
    private static final long serialVersionUID = 1L;

    public MemoryExceededException(String message) {
        super(message);
    }
}

