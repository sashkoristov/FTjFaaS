package at.uibk.dps;

import at.uibk.dps.databases.MongoDBAccess;
import at.uibk.dps.exception.CancelInvokeException;
import at.uibk.dps.exception.InvalidResourceException;
import at.uibk.dps.function.Function;
import at.uibk.dps.util.Event;
import at.uibk.dps.util.Type;
import com.amazonaws.regions.Regions;
import jFaaS.invokers.*;
import jFaaS.utils.PairResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will invoke a single FaaS function on the correct provider Can be canceled using by calling the stop() function Will
 * store Exception in "exception" if invocation fails If successful it will store result in "result"
 */
public class InvokationThread implements Runnable {
	final static Logger logger = LoggerFactory.getLogger(InvokationThread.class);
	private Exception exception;
	volatile private Thread thread;
	private AWSAccount awsAccount = null;
	private GoogleFunctionAccount googleFunctionAccount = null;
	private AzureAccount azureAccount = null;
	private IBMAccount ibmAccount = null;
	private OpenWhiskInvoker ibmInvoker = null; // needed to cancel invocation
	private Function function;
	volatile private boolean cancel = false;
	volatile private boolean finished = false;
	volatile private String result = null;
	volatile private Long RTT = null; // round trip time

	InvokationThread(AWSAccount awsAccount, IBMAccount ibmAccount, Function function) {
		this.awsAccount = awsAccount;
		this.ibmAccount = ibmAccount;
		this.function = function;
	}

	public InvokationThread(GoogleFunctionAccount googleFunctionAccount, AzureAccount azureAccount, Function function) {
		this.googleFunctionAccount = googleFunctionAccount;
		this.azureAccount = azureAccount;
		this.function = function;
	}

	public InvokationThread(GoogleFunctionAccount googleFunctionAccount, AzureAccount azureAccount, AWSAccount awsAccount,  Function function){
		this.googleFunctionAccount = googleFunctionAccount;
		this.azureAccount = azureAccount;
		this.awsAccount = awsAccount;
		this.function = function;
	}

	public InvokationThread(GoogleFunctionAccount googleFunctionAccount, AzureAccount azureAccount, IBMAccount ibmAccount,  Function function){
		this.googleFunctionAccount = googleFunctionAccount;
		this.azureAccount = azureAccount;
		this.ibmAccount = ibmAccount;
		this.function = function;
	}


	public InvokationThread(GoogleFunctionAccount googleFunctionAccount, AzureAccount azureAccount, AWSAccount awsAccount, IBMAccount ibmAccount, Function function) {
		this.googleFunctionAccount = googleFunctionAccount;
		this.azureAccount = azureAccount;
		this.awsAccount = awsAccount;
		this.ibmAccount = ibmAccount;
		this.function = function;
	}

	/**
	 * Detects the FaaS provider
	 */
	private static String detectProvider(String functionURL) {
		if (functionURL.contains(".functions.cloud.ibm.com/") || functionURL.contains(".functions.appdomain.cloud/")) {
			return "ibm";
		} else if (functionURL.contains("arn:aws:lambda:")) {
			return "aws";
		} else if (functionURL.contains("fc.aliyuncs.com")) {
			return "alibaba";
		} else if (functionURL.contains("cloudfunctions.net")) {
			return "google";
		} else if (functionURL.contains("azurewebsites.net")) {
			return "azure";
		}

		// Inform Scheduler Provider Detection Failed
		return "fail";
	}

	/**
	 * detects the region of a Function on AWS throws InvalidResourceException if region can not be detected
	 */
	private Regions detectAWSRegion(String functionURL) throws InvalidResourceException {
		String regionName;
		int searchIndex = functionURL.indexOf("lambda:");
		if (searchIndex != -1) {
			regionName = functionURL.substring(searchIndex + "lambda:".length());
			regionName = regionName.split(":")[0];
			try {
				Regions tmp = Regions.fromName(regionName);
				function.setRegion(regionName);
				return tmp;
			} catch (Exception e) {
				throw new InvalidResourceException("Region detection failed");
			}
		} else {
			// Error Parsing arn
			throw new InvalidResourceException("Region detection failed");
		}
	}

	/**
	 * detects the region of a Function on IBM and sets Region
	 */
	private void detectAndSetIBMRegion(String functionURL){
		//https://eu-gb.functions.cloud.ibm.com
		String regionName;
		int searchIndex = functionURL.indexOf("://");
		if (searchIndex != -1) {
			regionName = functionURL.substring(searchIndex + "://".length());
			regionName = regionName.split(".functions")[0];
			function.setRegion(regionName);
		} else {
			function.setRegion(null);
		}
	}

	public synchronized void reset() {
		exception = null;
		ibmInvoker = null;
		cancel = false;
		finished = false;
		result = null;
		RTT = null;
	}

	/**
	 * stops this thread
	 */
	public synchronized void stop() {
		logger.info("Stopping");
		// Stop invocation and terminate thread
		cancel = true;
		/*if (this.ibmInvoker != null) {
			ibmInvoker.cancelInvoke(); // to stop IBM Invoke
		}*/
		thread.interrupt(); // to stop AWS Invoke
	}

	public synchronized Thread getThread() {
		return thread;
	}

	public synchronized String getResult() {
		return result;
	}

	public synchronized void setResult(String result) {
		this.result = result;
	}

	public Long getRTT() { return RTT; }

	public void setRTT(Long RTT) { this.RTT = RTT; }

	@Override
	public void run() {
		thread = Thread.currentThread();
		long end, start = System.currentTimeMillis();
		try {
			// Try to invoke function
			PairResult<String, Long> pairResult = invokeFunctionOnCorrectProvider(function);
			result = pairResult.getResult();
			RTT = pairResult.getRTT();
		} catch (CancelInvokeException e) {
			end = System.currentTimeMillis();
			result = null;
			logger.info("Invocation in " + thread.toString() + "has been canceled.");
			MongoDBAccess.saveLog(Event.FUNCTION_CANCELED, function.getUrl(), function.getDeployment(), function.getName(), function.getType(), result, end - start, false, function.getLoopCounter(), function.getMaxLoopCounter(), start, Type.EXEC);
			exception = e;
			finished = true;
			return;
		} catch (Exception e) {
			end = System.currentTimeMillis();
			result = null;
			logger.error("Invocation in " + thread.toString() + "failed! - Error:" + e.getMessage());
			MongoDBAccess.saveLog(Event.FUNCTION_FAILED, function.getUrl(), function.getDeployment(), function.getName(), function.getType(), result, end - start, false, function.getLoopCounter(), function.getMaxLoopCounter(), start, Type.EXEC);
			exception = e;
			finished = true;
			return;
		}
		finished = true;
		exception = null;
		logger.info("Invocation in " + thread.toString() + " OK - " + "RESULT: " + result);
		MongoDBAccess.saveLog(Event.FUNCTION_END, function.getUrl(), function.getDeployment(), function.getName(), function.getType(), result, RTT, true, function.getLoopCounter(), function.getMaxLoopCounter(), start, Type.EXEC);
	}

	/**
	 * invokes function on correct provider throws Exception if invocation failed
	 */
	private PairResult<String, Long> invokeFunctionOnCorrectProvider(Function function) throws Exception {
		switch (detectProvider(function.getUrl())) {
			case "ibm":
				detectAndSetIBMRegion(function.getUrl());
				OpenWhiskInvoker OWinvoker = new OpenWhiskInvoker(ibmAccount.getIBMKey());
				ibmInvoker = OWinvoker; // Set so invocation can be canceled;
				if (!cancel) {
					OpenWhiskMonitor owMonitor = new OpenWhiskMonitor();
					return owMonitor.monitoredInvoke(OWinvoker, function);
				} else {
					throw new CancelInvokeException();
				}
			case "aws":
				try {
					Regions detectedRegion = detectAWSRegion(function.getUrl());
					function.setRegion(detectedRegion.getName());
					LambdaInvoker lambdaInvoker = new LambdaInvoker(awsAccount.getAwsAccessKey(),
							awsAccount.getAwsSecretKey(), awsAccount.getAwsSecctionToken(), detectedRegion);
//					memorySize = lambdaInvoker.getAssignedMemory(function.getUrl());
					if (!cancel) {
						LambdaMonitor lambdaMonitor = new LambdaMonitor();
						return lambdaMonitor.monitoredInvoke(lambdaInvoker, function);
					} else {
						throw new CancelInvokeException();
					}
				} catch (InvalidResourceException e) {
					// Add to DB (Normally after invokation - but will never be reached so we do it here)
//				if(Configuration.enableDatabase){
//					SQLLiteDatabase DB = new SQLLiteDatabase("jdbc:sqlite:Database/FTDatabase.db");
//					DB.addInvocation(function.getUrl(), function.getType(), "AWS",null, null, null, e.getClass().getName(), "Region detection Failed");
//				}
					throw e;
				}
			case "alibaba":
				// TODO do real monitoring
				HTTPGETInvoker httpgetInvoker = new HTTPGETInvoker();
				if (!cancel) {
					return httpgetInvoker.invokeFunction(function.getUrl(), function.getFunctionInputs());
				} else {
					throw new CancelInvokeException();
				}

			case "google":
				GoogleFunctionInvoker googleFunctionInvoker = null;
				if (googleFunctionAccount.getServiceAccountKey() != null) {
					googleFunctionInvoker = new GoogleFunctionInvoker(googleFunctionAccount.getServiceAccountKey(), "serviceAccount");
				} else {
					googleFunctionInvoker = new GoogleFunctionInvoker();
				}
				if (!cancel) {
					GoogleFunctionMonitor googleFunctionMonitor = new GoogleFunctionMonitor();
					return googleFunctionMonitor.monitoredInvoke(googleFunctionInvoker, function);
				} else {
					throw new CancelInvokeException();
				}
			case "azure":
				AzureInvoker azureInvoker;
				if (azureAccount.getAzureKey() != null) {
					azureInvoker = new AzureInvoker(azureAccount.getAzureKey());
				} else {
					azureInvoker = new AzureInvoker();
				}
				if (!cancel) {
					AzureMonitor azureMonitor = new AzureMonitor();
					return azureMonitor.monitoredInvoke(azureInvoker, function);
				} else {
					throw new CancelInvokeException();
				}


			default:
				// Tell Scheduler we cannot deal with this request;

				InvalidResourceException exception = new InvalidResourceException("Detection of provider failed");
//			if(Configuration.enableDatabase) {
//				SQLLiteDatabase DB = new SQLLiteDatabase("jdbc:sqlite:Database/FTDatabase.db");
//				DB.addInvocation(function.getUrl(), function.getType(), null,null, null, null,exception.getClass().getName(), "Provider detection Failed");
//			}
				throw exception;
		}
	}

	public AWSAccount getAwsAccount() {
		return awsAccount;
	}

	public void setAwsAccount(AWSAccount awsAccount) {
		this.awsAccount = awsAccount;
	}

	public IBMAccount getIbmAccount() {
		return ibmAccount;
	}

	public void setIbmAccount(IBMAccount ibmAccount) {
		this.ibmAccount = ibmAccount;
	}

	public GoogleFunctionAccount getGoogleFunctionAccount(){ return googleFunctionAccount;}

	public void setGoogleFunctionAccount(GoogleFunctionAccount googleFunctionAccount){this.googleFunctionAccount = googleFunctionAccount;}

	public AzureAccount getAzureAccount(){return azureAccount;}

	public void setAzureAccount(AzureAccount azureAccount){ this.azureAccount = azureAccount;}

	public synchronized boolean isFinished() {
		return finished;
	}

	public synchronized void setFinished(boolean finished) {
		this.finished = finished;
	}

	public synchronized Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

}