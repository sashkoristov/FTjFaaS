package dps.FTinvoker;
import java.net.SocketException;
import java.sql.Timestamp;

import dps.FTinvoker.database.SQLLiteDatabase;
import dps.FTinvoker.exception.AuthenticationFailedException;
import dps.FTinvoker.exception.CancelInvokeException;
import dps.FTinvoker.exception.InvalidResourceException;
import dps.FTinvoker.exception.SyntaxErrorException;
import dps.FTinvoker.exception.TimeLimitException;
import dps.FTinvoker.function.Function;
import dps.invoker.OpenWhiskInvoker;

public class OpenWhiskFT {

	/**
	 * Method to invoke OpenWhisk functions with monitoring (using
	 * dpsinvoker.jar) Saves monitoring data to database Returns value or throws
	 * exceptions if invokation resulted in error
	 */
	public static String monitoredInvoke(OpenWhiskInvoker whiskInvoker, Function function) throws Exception {
		String returnValue;
		Timestamp returnTime = null, invokeTime = null;
		SQLLiteDatabase DB = new SQLLiteDatabase("jdbc:sqlite:Database/FTDatabase.db");
		try {
			// save timestamp and invoke
			invokeTime = new Timestamp(System.currentTimeMillis());
			returnValue = whiskInvoker.invokeFunction(function.getUrl(), function.getFunctionInputs());
			assert returnValue != null;
		} catch (CancelInvokeException e) {
			returnTime = new Timestamp(System.currentTimeMillis());
			DB.add(function.getUrl(),function.getType(),"IBM",function.getRegion(), invokeTime, returnTime,"Canceled", null);
			throw e;
		} catch (SocketException e) { // could be canceled invokation
			returnTime = new Timestamp(System.currentTimeMillis());
			if (whiskInvoker.cancel) {
				DB.add(function.getUrl(),function.getType(),"IBM",function.getRegion(), invokeTime, returnTime,"Canceled", null);
				throw new CancelInvokeException();
			} else {
				DB.add(function.getUrl(),function.getType(),"IBM",function.getRegion(), invokeTime, returnTime,e.getClass().getName(), e.getMessage());
				throw e;
			}
		}
		catch (Exception e) { // catch all Exceptions and pass them up the chain
			returnTime = new Timestamp(System.currentTimeMillis());
			DB.add(function.getUrl(),function.getType(),"IBM",function.getRegion(), invokeTime, returnTime,e.getClass().getName(), e.getMessage());
			throw e;
		}
		// Was invoked without throwing an Exception or returning null
		returnTime = new Timestamp(System.currentTimeMillis());
		if (returnValue.contains("\"error\"")) {
			try {
				parseAndThrowException(returnValue);
			} catch (SyntaxErrorException | InvalidResourceException | AuthenticationFailedException
					| TimeLimitException e) {
				DB.add(function.getUrl(),function.getType(),"IBM",function.getRegion(), invokeTime, returnTime,e.getClass().getName(), e.getMessage());
				throw e;
			} catch (Exception e) {
				DB.add(function.getUrl(),function.getType(),"IBM",function.getRegion(), invokeTime, returnTime,e.getClass().getName(), e.getMessage());
				throw e;
			}
		}

		// Correct Return value without Errors
		DB.add(function.getUrl(),function.getType(),"IBM",function.getRegion(), invokeTime, returnTime,"OK", null);
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
			InvalidResourceException, AuthenticationFailedException, TimeLimitException, Exception {
		if (returnValue.contains("SyntaxError")) {
			throw new SyntaxErrorException(returnValue);
		} else if (returnValue.contains("requested resource does not exist")) {
			throw new InvalidResourceException(returnValue);
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
