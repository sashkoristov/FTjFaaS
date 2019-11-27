package dps.FTinvoker.exception;

/**
 * -Custom exception which is thrown when Function is not found on FaaS provider
 * 
 * @author Matteo Battaglin
 *
 */
public class InvalidResourceException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidResourceException(String message) {
		super(message);
	}
}
