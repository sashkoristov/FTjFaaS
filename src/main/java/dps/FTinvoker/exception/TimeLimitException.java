package dps.FTinvoker.exception;

/**
 * Custom exception which is thrown when Function Invokation times out
 * 
 * @author Matteo Battaglin
 *
 */
public class TimeLimitException extends Exception {
	private static final long serialVersionUID = 1L;

	public TimeLimitException(String message) {
		super(message);
	}
}
