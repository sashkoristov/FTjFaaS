package dps.FTinvoker.exception;

/**
 * Custom exception which is thrown when latestStartingTime constraint is missed
 */
public class LatestStartingTimeException extends Exception {
	private static final long serialVersionUID = 1L;

	public LatestStartingTimeException(String message) {
		super(message);
	}
}
