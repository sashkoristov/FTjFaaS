package at.uibk.dps.exception;

/**
 * Custom exception which is thrown when Function is not found on FaaS provider or if provider cannot be detected
 */
public class InvalidResourceException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidResourceException(String message) {
		super(message);
	}
}
