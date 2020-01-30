package dps.FTinvoker.exception;

/**
 * Custom exception which is thrown when Function Time Limit is reached
 */
public class TimeLimitException extends Exception {
	private static final long serialVersionUID = 1L;

	public TimeLimitException(String message) {
		super(message);
	}
}
