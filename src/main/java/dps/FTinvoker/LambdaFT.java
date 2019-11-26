package dps.FTinvoker;

import java.sql.Timestamp;
import java.util.Map;
import dps.invoker.LambdaInvoker;

public class LambdaFT {

	/**
	 * Method to invoke Lambda Functions with Monitoring (using dpsinvoker.jar)
	 * Parses Errors and Saves Monitoring Data to Database Passes Exceptions
	 * thrown when invoking further up the chain Returns correct value or throws
	 * Exception
	 */
	public static String monitoredInvoke(LambdaInvoker awsInvoker, String function, Map<String, Object> functionInputs)
			throws Exception {
		SQLLiteDatabase DB = new SQLLiteDatabase("jdbc:sqlite:Database/FTDatabase.db");
		String returnValue;
		Timestamp returnTime = null, invokeTime = null;
		try {
			invokeTime = new Timestamp(System.currentTimeMillis());
			returnValue = awsInvoker.invokeFunction(function, functionInputs);
			assert returnValue != null;
		} catch (Exception e) {
			returnTime = new Timestamp(System.currentTimeMillis());
			// save to DB
			DB.add(function, invokeTime, returnTime, e.getClass().getName());
			throw e;
		}
		returnTime = new Timestamp(System.currentTimeMillis());
		int searchIndex = returnValue.indexOf("\"errorType\"");
		if (searchIndex != -1) {
			// save to DB
			DB.add(function, invokeTime, returnTime, parseError(returnValue, searchIndex));
			throw new Exception(returnValue);
		}
		// save to DB
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
