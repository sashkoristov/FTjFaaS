package dps.FTinvoker;
import java.sql.Timestamp;
import com.amazonaws.AbortedException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.model.AWSLambdaException;
import com.amazonaws.services.lambda.model.ResourceNotFoundException;

import dps.FTinvoker.database.SQLLiteDatabase;
import dps.FTinvoker.exception.AuthenticationFailedException;
import dps.FTinvoker.exception.CancelInvokeException;
import dps.FTinvoker.exception.InvalidResourceException;
import dps.FTinvoker.exception.SyntaxErrorException;
import dps.FTinvoker.exception.TimeLimitException;
import dps.FTinvoker.exception.TimedOutException;
import dps.FTinvoker.function.Function;
import dps.invoker.FaaSInvoker;

public class LambdaMonitor implements InvokeMonitor{

	/**
	 * Method to invoke Lambda Functions with Monitoring (using dpsinvoker.jar)
	 * Parses errors and tries to identify known Exceptions to throw Returns
	 * correct value or throws Exception
	 */
	@Override
	public String monitoredInvoke(FaaSInvoker invoker, Function function)
			throws Exception {
		String returnValue;
		Timestamp returnTime = null, invokeTime = null;
		SQLLiteDatabase DB = new SQLLiteDatabase("jdbc:sqlite:Database/FTDatabase.db");
		try {
			// save timestamp and invoke
			invokeTime = new Timestamp(System.currentTimeMillis());
			returnValue = invoker.invokeFunction(function.getUrl(), function.getFunctionInputs());
			assert returnValue != null;

		} catch (AbortedException e) { //has been canceled
			returnTime = new Timestamp(System.currentTimeMillis());
			DB.add(function.getUrl(),function.getType(),"AWS",function.getRegion(), invokeTime, returnTime, "Canceled", null);
			throw new CancelInvokeException();
		}

		catch (ResourceNotFoundException e) { // InvalidResourceException
			returnTime = new Timestamp(System.currentTimeMillis());
			DB.add(function.getUrl(),function.getType(),"AWS",function.getRegion(), invokeTime, returnTime, "InvalidResourceException", e.getMessage());
			throw new InvalidResourceException(e.getErrorMessage());

		} catch (AWSLambdaException e) { // check if auth Exception or other
			returnTime = new Timestamp(System.currentTimeMillis());
			if (e.getErrorMessage().contains("security token included in the request is invalid")) {
				AuthenticationFailedException newException = new AuthenticationFailedException(e.getErrorMessage());
				DB.add(function.getUrl(),function.getType(),"AWS",function.getRegion(), invokeTime, returnTime, newException.getClass().getName(), e.getMessage());
				throw newException;
			} else {
				DB.add(function.getUrl(),function.getType(),"AWS",function.getRegion(), invokeTime, returnTime, e.getClass().getName(), e.getMessage());
				throw e;
			}

		} catch (SdkClientException e) { // check if timeout or other Exception
			returnTime = new Timestamp(System.currentTimeMillis());
			if (e.getMessage().contains("Unable to execute HTTP request: Read timed out")) {
				TimeLimitException newException = new TimeLimitException(e.getMessage());
				DB.add(function.getUrl(),function.getType(),"AWS",function.getRegion(), invokeTime, returnTime, newException.getClass().getName(), e.getMessage());
				throw newException;
			} else {
				DB.add(function.getUrl(),function.getType(),"AWS",function.getRegion(), invokeTime, returnTime, e.getClass().getName(),e.getMessage());
				throw e;
			}

		} catch (Exception e) { // catch all Exceptions and pass them up the
								// chain
			returnTime = new Timestamp(System.currentTimeMillis());
			DB.add(function.getUrl(),function.getType(),"AWS",function.getRegion(), invokeTime, returnTime, e.getClass().getName(), e.getMessage());
			throw e;
		}

		// Was invoked without throwing an Exception or returning null
		returnTime = new Timestamp(System.currentTimeMillis());

		// check if any errors in returnValue
		int searchIndex = returnValue.indexOf("\"errorType\"");
		if (searchIndex != -1) {
			if (returnValue.contains("Syntax error") || returnValue.contains("SyntaxError")) {// Syntax errors
				SyntaxErrorException newException = new SyntaxErrorException(returnValue);
				DB.add(function.getUrl(),function.getType(),"AWS",function.getRegion(), invokeTime, returnTime, newException.getClass().getName(), returnValue);
				throw newException;
			} else {
				// save to DB
				DB.add(function.getUrl(),function.getType(),"AWS",function.getRegion(), invokeTime, returnTime,parseError(returnValue, searchIndex), returnValue);
				throw new Exception(returnValue);
			}
		}
		
		int searchIndex2 = returnValue.indexOf("\"errorMessage\"");
		if (searchIndex2 != -1) {
			if (returnValue.contains("Task timed out")) {// Timed out
				TimedOutException exception = new TimedOutException(returnValue);
				DB.add(function.getUrl(),function.getType(),"AWS",function.getRegion(), invokeTime, returnTime,exception.getClass().getName(), returnValue);
				throw exception;
			} else {
				// save to DB
				DB.add(function.getUrl(),function.getType(),"AWS",function.getRegion(), invokeTime, returnTime,parseError(returnValue, searchIndex), returnValue);
				throw new Exception(returnValue);
			}
		}

		// Correct Return value without Errors
		DB.add(function.getUrl(),function.getType(),"AWS",function.getRegion(), invokeTime, returnTime,"OK", null);
		return returnValue;
	};

	/**
	 * Method Parses errorType from returnValue containing an Error Returns
	 * String of errorType
	 */
	private String parseError(String returnValue, int searchIndex) {
		String tmp;
		tmp = returnValue.substring(searchIndex + 12);
		tmp = tmp.substring(tmp.indexOf("\"") + 1);
		tmp = tmp.split("\"")[0];
		return tmp;
	}


}
