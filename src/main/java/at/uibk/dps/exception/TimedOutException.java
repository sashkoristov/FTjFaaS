package at.uibk.dps.exception;


/**
 * Custom exception which is thrown when Function times out
 */
public class TimedOutException extends Exception {
	private static final long serialVersionUID = 1L;

	public TimedOutException(String message) {
		super(message);
	}
}
