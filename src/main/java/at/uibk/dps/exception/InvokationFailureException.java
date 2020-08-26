package at.uibk.dps.exception;

/**
 * -Custom exception which is thrown when Function invokation failed after all
 * retries
 */
public class InvokationFailureException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvokationFailureException(String message) {
		super(message);
	}
}
