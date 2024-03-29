package at.uibk.dps;

import at.uibk.dps.database.SQLLiteDatabase;
import at.uibk.dps.exception.AuthenticationFailedException;
import at.uibk.dps.exception.InvalidResourceException;
import at.uibk.dps.exception.SyntaxErrorException;
import at.uibk.dps.exception.TimeLimitException;
import at.uibk.dps.function.Function;
import jFaaS.invokers.FaaSInvoker;
import jFaaS.invokers.OpenWhiskInvoker;
import jFaaS.utils.PairResult;

import java.sql.Timestamp;

public class OpenWhiskMonitor implements InvokeMonitor{

	/**
	 * Method to invoke OpenWhisk functions with monitoring (using dpsinvoker.jar) Saves monitoring data to database
	 * Returns value or throws exceptions if invokation resulted in error
	 *
	 * @return
	 */
	@Override
	public PairResult<String, Long> monitoredInvoke(FaaSInvoker invoker, Function function) throws Exception {
		OpenWhiskInvoker whiskInvoker = (OpenWhiskInvoker) invoker;

		PairResult<String, Long> returnValue;
		Timestamp returnTime = null, invokeTime = null;
		SQLLiteDatabase DB = null;
		if (Configuration.enableDatabase) {
			DB = new SQLLiteDatabase("jdbc:sqlite:Database/FTDatabase.db");
		}
		try {
			// save timestamp and invoke
			invokeTime = new Timestamp(System.currentTimeMillis());
			returnValue = whiskInvoker.invokeFunction(function.getUrl(), function.getFunctionInputs());
			assert returnValue != null;
		} catch (Exception e) { // could be canceled invokation
			returnTime = new Timestamp(System.currentTimeMillis());
//			if(Configuration.enableDatabase){
//				DB.addInvocation(function.getUrl(),function.getType(),"IBM",function.getRegion(), invokeTime, returnTime,e.getClass().getName(), e.getMessage());
//			}
			throw e;
		}// catch all Exceptions and pass them up the chain

		// Was invoked without throwing an Exception or returning null
		returnTime = new Timestamp(System.currentTimeMillis());
		if (returnValue.getResult().contains("\"error\"")) {
			try {
				parseAndThrowException(returnValue.getResult());
			} catch (SyntaxErrorException | InvalidResourceException | AuthenticationFailedException
					| TimeLimitException e) {
//				if(Configuration.enableDatabase){
//					DB.addInvocation(function.getUrl(),function.getType(),"IBM",function.getRegion(), invokeTime, returnTime,e.getClass().getName(), e.getMessage());
//				}
				throw e;
			} catch (Exception e) {
//				if(Configuration.enableDatabase){
//					DB.addInvocation(function.getUrl(),function.getType(),"IBM",function.getRegion(), invokeTime, returnTime,e.getClass().getName(), e.getMessage());
//				}
				throw e;
			}
		}

		// Correct Return value without Errors
//		if(Configuration.enableDatabase){
//			DB.addInvocation(function.getUrl(),function.getType(),"IBM",function.getRegion(), invokeTime, returnTime,"OK", null);
//		}
		return returnValue;
	}

	/**
	 * Method parses error and tries to identify known Exceptions to throw
	 * Returns
	 * 
	 * @throws SyntaxErrorException
	 * @throws InvalidResourceException
	 * @throws AuthenticationFailedException
	 * @throws TimeLimitException
	 * @throws Exception
	 */
	private void parseAndThrowException(String returnValue) throws SyntaxErrorException,
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
