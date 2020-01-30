package dps.FTinvoker.exception;

/**
 * Custom exception which is thrown when latestStartingTime constraint is missed
 */
public class LatestFinishingTimeException extends Exception {
	private static final long serialVersionUID = 1L;

	public LatestFinishingTimeException(String message) {
		super(message);
	}
}
