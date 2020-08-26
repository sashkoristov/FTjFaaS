package at.uibk.dps.exception;

/**
 * Custom exception which is thrown when latestStartingTime constraint is missed
 */
public class MaxRunningTimeException extends Exception {
	private static final long serialVersionUID = 1L;

	public MaxRunningTimeException(String message) {
		super(message);
	}
}
