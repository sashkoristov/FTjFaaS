package at.uibk.dps;
import java.util.ArrayList;
import java.util.List;

import at.uibk.dps.exception.CancelInvokeException;
import at.uibk.dps.exception.InvokationFailureException;
import at.uibk.dps.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MasterThread that will execute FaultTolerance
 * will retry and execute AlternativeStrategy
 * Constraints will be checked by the FaultToleranceEngine
 */
public class MasterThread implements Runnable {
	final Logger logger = LoggerFactory.getLogger(MasterThread.class);
	volatile private Thread thread;
	private AWSAccount awsAccount = null;
	private IBMAccount ibmAccount = null;
	private GoogleFunctionAccount googleFunctionAccount = null;
	private AzureAccount azureAccount = null;
	private Function function;
	volatile private boolean cancel = false;
	volatile private boolean finished = false;
	volatile private String result = null;
	volatile private InvokationThread invokThread = null;

	MasterThread(AWSAccount awsAccount, IBMAccount ibmAccount, Function function) {
		this.awsAccount = awsAccount;
		this.ibmAccount = ibmAccount;
		this.googleFunctionAccount = null;
		this.azureAccount = null;
		this.function = function;
	}

	MasterThread(GoogleFunctionAccount googleFunctionAccount, AzureAccount azureAccount, Function function){
		this.googleFunctionAccount = googleFunctionAccount;
		this.azureAccount = azureAccount;
		this.function = function;
		this.awsAccount = null;
		this.ibmAccount = null;

	}

	MasterThread(GoogleFunctionAccount googleFunctionAccount, AzureAccount azureAccount, AWSAccount awsAccount, Function function){
		this.googleFunctionAccount = googleFunctionAccount;
		this.azureAccount  = azureAccount;
		this.awsAccount = awsAccount;
		this.function = function;

	}

	MasterThread(GoogleFunctionAccount googleFunctionAccount, AzureAccount azureAccount, IBMAccount ibmAccount, Function function){
		this.googleFunctionAccount = googleFunctionAccount;
		this.azureAccount  = azureAccount;
		this.ibmAccount = ibmAccount;
		this.function = function;

	}

	MasterThread(GoogleFunctionAccount googleFunctionAccount, AzureAccount azureAccount, AWSAccount awsAccount, IBMAccount ibmAccount, Function function){
		this.googleFunctionAccount = googleFunctionAccount;
		this.azureAccount  = azureAccount;
		this.awsAccount = awsAccount;
		this.ibmAccount = ibmAccount;
		this.function = function;

	}

	public synchronized void stop() {
		if (this.invokThread != null) {
			this.invokThread.stop();
		}
		this.cancel = true;
	}

	public synchronized Thread getThread() {
		return thread;
	}

	public synchronized void setThread(Thread thread) {
		this.thread = thread;
	}

	public synchronized String getResult() {
		return result;
	}

	public synchronized void setResult(String result) {
		this.result = result;
	}

	/**
	 * returns index of first successful Thread in workerList
	 */
	private int successfullThread(List<InvokationThread> workerList) {
		int numThreads = workerList.size();
		for (int index = 0; index < numThreads; index++) {
			if (workerList.get(index).isFinished() == true && workerList.get(index).getResult() != null) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * stops all threads in workerList
	 */
	private void terminateAll(List<InvokationThread> workerList) {
		for (InvokationThread thread : workerList) {
			thread.stop();
		}
	}

	/**
	 * Checks if all threads have terminated
	 */
	private boolean allThreadsDone(List<InvokationThread> workerList) {
		int numThreads = workerList.size();
		for (int index = 0; index < numThreads; index++) {
			if (workerList.get(index).isFinished() == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * spawns multiple InvokationThreads to invoke multiple functions paralelly
	 */
	private String parallelInvoke(List<Function> functionList)
			throws CancelInvokeException, InvokationFailureException {
		boolean running = true;
		List<InvokationThread> workerList = new ArrayList<InvokationThread>(functionList.size());
		if (functionList != null && functionList.size() > 0) {
			for (Function functionToBeInvoked : functionList) {
				InvokationThread invocationThread = null;

				if(this.awsAccount != null && this.ibmAccount!= null && this.googleFunctionAccount != null && this.azureAccount != null){
					invocationThread = new InvokationThread(googleFunctionAccount, azureAccount, awsAccount, ibmAccount, functionToBeInvoked);
				}
				else if(this.awsAccount != null && this.googleFunctionAccount != null && this.azureAccount != null){
					invocationThread = new InvokationThread(googleFunctionAccount, azureAccount, awsAccount, functionToBeInvoked);
				}
				else if(this.azureAccount != null && this.googleFunctionAccount != null && this.ibmAccount != null){
					invocationThread = new InvokationThread(googleFunctionAccount, azureAccount, ibmAccount, functionToBeInvoked);
				}
				else if(this.azureAccount != null && this.googleFunctionAccount != null) {
					 invocationThread = new InvokationThread(googleFunctionAccount, azureAccount, functionToBeInvoked);
				}
				else {
					 invocationThread = new InvokationThread(awsAccount, ibmAccount, functionToBeInvoked);
				}
				Thread thread = new Thread(invocationThread);
				thread.start();
				workerList.add(invocationThread);
			}
			// All invocation Threads have been started
			while (running) {
				int indexOfSucessfull = successfullThread(workerList);
				if (indexOfSucessfull != -1) {
					String correctResult = workerList.get(indexOfSucessfull).getResult();
					terminateAll(workerList);
					return correctResult;
				}
				if (allThreadsDone(workerList) == true && successfullThread(workerList) == -1) {
					running = false;
					throw new InvokationFailureException("All Threads Failed");
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// Ignore
				}
				if (this.cancel) {
					terminateAll(workerList);
					System.out.flush();
					throw new CancelInvokeException();
				}
			}
			throw new InvokationFailureException("Failed");
		} else {
			throw new InvokationFailureException("FunctionList is empty or null!");
		}
	}

	/**
	 * Invokes the alternativeStrategy of a given Function
	 */
	private String invokeAlternativeStategy(Function function) throws Exception {
		// throws exception if it has tried entire alternativeStrategy without
		// success
		if (function.getFTSettings().getAltStrategy() != null) {
			int i = 0;
			for (List<Function> alternativePlan : function.getFTSettings().getAltStrategy()) {
				logger.info("##############  Trying Alternative Plan "+i +"  ##############");
				i++;
				try {
					String result = parallelInvoke(alternativePlan);
					return result;
				} catch (CancelInvokeException e) {
					//
					throw e;
				} catch (Exception e) {
					// ignore Exceptions here because they have been logged to
					// DB
					// continue to next alternativePlan
				}
			}
			throw new Exception("Failed after entire Alternative Strategy");
		}
		throw new Exception("No alternative Strategy defined");
	}


	@Override
	public void run() {
		InvokationThread invokThread = null;

		if(this.azureAccount != null && this.googleFunctionAccount != null   && this.awsAccount != null && this.ibmAccount != null) {
			 invokThread = new InvokationThread(this.googleFunctionAccount, this.azureAccount, this.awsAccount, this.ibmAccount, function);
		} else if(this.awsAccount != null && this.googleFunctionAccount != null && this.azureAccount != null) {
			invokThread = new InvokationThread(this.googleFunctionAccount, this.azureAccount, this.awsAccount, function);
		}else if(this.azureAccount != null && this.googleFunctionAccount != null && this.ibmAccount != null){
			invokThread = new InvokationThread(this.googleFunctionAccount, this.azureAccount, this.ibmAccount, function);
		}else if(this.azureAccount != null && this.googleFunctionAccount != null) {
			invokThread = new InvokationThread(this.googleFunctionAccount, this.azureAccount, function);
		} else{
			invokThread = new InvokationThread(this.awsAccount, this.ibmAccount, function);
		}
		this.invokThread = invokThread; // so we can stop invocation
		this.invokThread.run(); // Try to invoke the Function in this Thread;
								// Will return if canceled , finished or failed

		if (invokThread.getException() == null) {
			// set correct result and terminate thread
			this.result = this.invokThread.getResult();
			this.finished = true;
			return;
		} else { // check FT Settings because first invocation has failed.
			if (this.function.hasFTSet()) {
				int i = 0;
				logger.info("##############  First invokation has failed retrying "+function.getFTSettings().getRetries()+ " times.  ##############");
				while (i < function.getFTSettings().getRetries()) {
					this.invokThread.reset();
					this.invokThread.run();
					if (invokThread.getException() == null) {
						// set correct result and terminate thread
						this.result = this.invokThread.getResult();
						this.finished = true;
						return;
					}
					i = i + 1;
				}
				// Failed after all retries. Check for alternative Strategy
				if (this.function.getFTSettings().hasAlternativeStartegy()) {
					try {
						String result = invokeAlternativeStategy(this.function);
						// AlternativeStrategy has correct Result
						this.result = result;
						this.finished = true;
						return;
					}catch(CancelInvokeException e){
						this.cancel = true;
						this.finished = true;
						this.result = null;
						return;
					} 
					catch (Exception e) {
						this.cancel = false;
						this.finished = true;
						this.result = null;
						return;
					}
				} else {
					// no alternativeStrat set so failure
					this.cancel = false;
					this.finished = true;
					this.result = null;
					return;
				}
			} else {
				this.cancel = false;
				this.finished = true;
				this.result = null;
				return;
			}

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

	public synchronized boolean isFinished() {
		return finished;
	}

	public synchronized void setFinished(boolean finished) {
		this.finished = finished;
	}

	public GoogleFunctionAccount getGoogleFunctionAccount() { return googleFunctionAccount; }

	public void setGoogleFunctionAccount(GoogleFunctionAccount googleFunctionAccount) { this.googleFunctionAccount = googleFunctionAccount; }

	public AzureAccount getAzureAccount() { return azureAccount; }

	public void setAzureAccount(AzureAccount azureAccount) { this.azureAccount = azureAccount; }
}