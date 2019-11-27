package dps.FTinvoker;

import java.sql.Timestamp;
import java.util.Map;

import dps.FTinvoker.exception.AuthenticationFailedException;
import dps.FTinvoker.exception.ResourceNotFoundException;
import dps.FTinvoker.exception.SyntaxErrorException;
import dps.FTinvoker.exception.TimeLimitException;
import dps.invoker.OpenWhiskInvoker;

public class OpenWhiskFT {

	/**
	 * Method to invoke OpenWhisk functions with monitoring (using
	 * dpsinvoker.jar) Saves monitoring data to database Returns value or throws
	 * exceptions if invokation resulted in error
	 */
	public static String monitoredInvoke(OpenWhiskInvoker whiskInvoker, String function,
			Map<String, Object> functionInputs) throws Exception {
		SQLLiteDatabase DB = new SQLLiteDatabase("jdbc:sqlite:Database/FTDatabase.db");
		String returnValue;
		Timestamp returnTime = null, invokeTime = null;
		try {
			invokeTime = new Timestamp(System.currentTimeMillis());
			returnValue = whiskInvoker.invokeFunction(function, functionInputs);
			assert returnValue != null;
		} catch (Exception e) {
			returnTime = new Timestamp(System.currentTimeMillis());
			DB.add(function, invokeTime, returnTime, e.getClass().getName() + ": " + e.getMessage());
			throw e;
		}
		returnTime = new Timestamp(System.currentTimeMillis());
		if (returnValue.contains("\"error\"")) {
			try {
				parseAndThrowException(returnValue);
			} catch (SyntaxErrorException | ResourceNotFoundException | AuthenticationFailedException
					| TimeLimitException e) {
				DB.add(function, invokeTime, returnTime, e.getClass().getName());
				throw e;
			} catch (Exception e) {
				DB.add(function, invokeTime, returnTime, e.getClass().getName() + ": " + e.getMessage());
				throw e;
			}
		}
		DB.add(function, invokeTime, returnTime, "Success");
		return returnValue;
	};

	/**
	 * Method parses error and tries to identify known Exceptions to throw
	 * Returns
	 * 
	 * @throws SyntaxErrorException
	 * @throws ResourceNotFoundException
	 * @throws AuthenticationFailedException
	 * @throws TimeLimitException
	 * @throws UnknownException
	 * @throws Exception
	 */
	private static void parseAndThrowException(String returnValue) throws SyntaxErrorException,
			ResourceNotFoundException, AuthenticationFailedException, TimeLimitException, Exception {
		if (returnValue.contains("SyntaxError")) {
			throw new SyntaxErrorException(returnValue);
		} else if (returnValue.contains("requested resource does not exist")) {
			throw new ResourceNotFoundException(returnValue);
		} else if (returnValue.contains("supplied authentication is invalid")
				|| returnValue.contains("resource requires authentication")
				|| returnValue.contains("supplied authentication is not authorized")) {
			throw new AuthenticationFailedException(returnValue);
		} else if (returnValue.contains("exceeded its time limits")) {
			throw new TimeLimitException(returnValue);
		}
		// if none detected throw basic Exception
		throw new Exception(returnValue);
	}

}
