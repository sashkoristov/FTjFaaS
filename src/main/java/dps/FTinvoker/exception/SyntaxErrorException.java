package dps.FTinvoker.exception;

/**
 * -Custom exception that is thrown if Syntax Errors where detected
 */
public class SyntaxErrorException extends Exception {
	private static final long serialVersionUID = 1L;

	public SyntaxErrorException(String message) {
		super(message);
	}
}
