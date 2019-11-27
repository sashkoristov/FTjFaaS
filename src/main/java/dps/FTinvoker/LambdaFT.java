package dps.FTinvoker;

import java.sql.Timestamp;
import java.util.Map;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.model.AWSLambdaException;
import com.amazonaws.services.lambda.model.ResourceNotFoundException;

import dps.FTinvoker.exception.AuthenticationFailedException;
import dps.FTinvoker.exception.InvalidResourceException;
import dps.FTinvoker.exception.SyntaxErrorException;
import dps.FTinvoker.exception.TimeLimitException;
import dps.invoker.LambdaInvoker;

public class LambdaFT {

	/**
	 * Method to invoke Lambda Functions with Monitoring (using dpsinvoker.jar)
	 * Parses errors and tries to identify known Exceptions to throw Returns
	 * correct value or throws Exception
	 */
	public static String monitoredInvoke(LambdaInvoker awsInvoker, String function, Map<String, Object> functionInputs)
			throws Exception {
		String returnValue;
		Timestamp returnTime = null, invokeTime = null;
		SQLLiteDatabase DB = new SQLLiteDatabase("jdbc:sqlite:Database/FTDatabase.db");

		try {
			// save timestamp and invoke
			invokeTime = new Timestamp(System.currentTimeMillis());
			returnValue = awsInvoker.invokeFunction(function, functionInputs);
			assert returnValue != null;

		} catch (ResourceNotFoundException e) { //InvalidResourceException
			returnTime = new Timestamp(System.currentTimeMillis());
			DB.add(function, invokeTime, returnTime, "InvalidResourceException");
			throw new InvalidResourceException(e.getErrorMessage());

		} catch (AWSLambdaException e) { //check if auth Exception or other
			returnTime = new Timestamp(System.currentTimeMillis());
			if (e.getErrorMessage().contains("security token included in the request is invalid")) {
				AuthenticationFailedException newException = new AuthenticationFailedException(e.getErrorMessage());
				DB.add(function, invokeTime, returnTime, newException.getClass().getName());
				throw newException;
			} else {
				DB.add(function, invokeTime, returnTime, e.getClass().getName());
				throw e;
			}

		} catch (SdkClientException e) { //check if timeout or other Exception
			returnTime = new Timestamp(System.currentTimeMillis());
			if (e.getMessage().contains("Unable to execute HTTP request: Read timed out")) {
				TimeLimitException newException = new TimeLimitException(e.getMessage());
				DB.add(function, invokeTime, returnTime, newException.getClass().getName());
				throw newException;
			} else {
				DB.add(function, invokeTime, returnTime, e.getClass().getName());
				throw e;
			}

		} catch (Exception e) { // catch all Exceptions and pass them up the chain
			returnTime = new Timestamp(System.currentTimeMillis());
			DB.add(function, invokeTime, returnTime, e.getClass().getName());
			throw e;
		}
		
		//Was invoked without throwing an Exception or returning null
		returnTime = new Timestamp(System.currentTimeMillis());
		
		//check if any errors in returnValue
		int searchIndex = returnValue.indexOf("\"errorType\"");
		if (searchIndex != -1) {
			if (returnValue.contains("Syntax error") || returnValue.contains("SyntaxError")) { // try to identify Syntax Errors
				SyntaxErrorException newException = new SyntaxErrorException(returnValue);
				DB.add(function, invokeTime, returnTime, newException.getClass().getName());
				throw newException;
			} else {
				// save to DB
				DB.add(function, invokeTime, returnTime, parseError(returnValue, searchIndex));
				throw new Exception(returnValue);
			}
		}
		
		// Correct Return value without Errors
		DB.add(function, invokeTime, returnTime, "OK");
		return returnValue;
	};

	/**
	 * Method Parses errorType from returnValue containing an Error Returns
	 * String of errorType
	 */
	private static String parseError(String returnValue, int searchIndex) {
		String tmp;
		tmp = returnValue.substring(searchIndex + 12);
		tmp = tmp.substring(tmp.indexOf("\"") + 1);
		tmp = tmp.split("\"")[0];
		return tmp;
	}

}
