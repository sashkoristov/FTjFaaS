package dps.FTinvoker.exception;

/**
 * Custom exception which is thrown when Authentication failed
 */
public class AuthenticationFailedException extends Exception {
	private static final long serialVersionUID = 1L;

	public AuthenticationFailedException(String message) {
		super(message);
	}
}
