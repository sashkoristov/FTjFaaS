package at.uibk.dps;

import at.uibk.dps.exception.InvokationFailureException;
import at.uibk.dps.exception.LatestFinishingTimeException;
import at.uibk.dps.exception.LatestStartingTimeException;
import at.uibk.dps.exception.MaxRunningTimeException;
import at.uibk.dps.function.Function;

import java.sql.Timestamp;

/**
 * Class to invoke Functions with fault-tolerance and/or constraints
 */

public class FaultToleranceEngine {
	private AWSAccount awsAccount;
	private IBMAccount ibmAccount;
	private GoogleFunctionAccount googleFunctionAccount;
	private AzureAccount azureAccount;


	public FaultToleranceEngine (AWSAccount awsAccount, IBMAccount ibmAccount) {
		this.awsAccount = awsAccount;
		this.ibmAccount = ibmAccount;
		this.googleFunctionAccount = null;
		this.azureAccount = null;
	}

	public FaultToleranceEngine(GoogleFunctionAccount googleFunctionAccount, AzureAccount azureAccount) {

		this.googleFunctionAccount = googleFunctionAccount;
		this.azureAccount = azureAccount;
		this.ibmAccount = null;
		this.awsAccount = null;
	}
	public FaultToleranceEngine(GoogleFunctionAccount googleFunctionAccount, AzureAccount azureAccount, AWSAccount awsAccount, IBMAccount ibmAccount){
		this.googleFunctionAccount = googleFunctionAccount;
		this.azureAccount = azureAccount;
		this.awsAccount = awsAccount;
		this.ibmAccount = ibmAccount;
	}
	public FaultToleranceEngine(GoogleFunctionAccount googleFunctionAccount, AzureAccount azureAccount, AWSAccount awsAccount){
		this.googleFunctionAccount = googleFunctionAccount;
		this.azureAccount = azureAccount;
		this.awsAccount = awsAccount;
	}

	public FaultToleranceEngine(GoogleFunctionAccount googleFunctionAccount, AzureAccount azureAccount, IBMAccount ibmAccount){
		this.googleFunctionAccount = googleFunctionAccount;
		this.azureAccount = azureAccount;
		this.ibmAccount = ibmAccount;
	}






	/**
	 * Function to invoke a Function with fault-tolerance and/or constraints
	 * Will spawn multiple worker threads if necessary
	 * Will check constraints and cancel invocations if needed
	 * Throws Exceptions if it has failed to execute the function after trying everything
	 */
	public String InvokeFunctionFT(Function function) throws InvokationFailureException, LatestStartingTimeException,
			LatestFinishingTimeException, MaxRunningTimeException {
		if (function != null) {

			if (function.hasConstraintSet()) {
				// constraints set
				Timestamp timeAtStart = new Timestamp(System.currentTimeMillis());
				if (function.getConstraints().hasLatestStartingTime()) {
					if (timeAtStart.after(function.getConstraints().getLatestStartingTime())) {
						throw new LatestStartingTimeException("latestStartingTime constraint missed!");
					}
					if (!function.getConstraints().hasLatestFinishingTime()
							&& !function.getConstraints().hasMaxRunningTime()) {
						// neither LFT nor MRT set so we will not have to cancel
						// Invocation (we do not need extra thread)
						MasterThread master = null;

						if(this.googleFunctionAccount != null && this.azureAccount != null && this.awsAccount != null && this.ibmAccount != null){
							master = new MasterThread(this.getGoogleFunctionAccount(), this.getAzureAccount(), this.getAwsAccount(), this.getIbmAccount(), function);
						} else if(this.googleFunctionAccount != null && this.azureAccount!=null && this.ibmAccount != null){
							master = new MasterThread(this.getGoogleFunctionAccount(), this.getAzureAccount(), this.getIbmAccount(), function);
						} else if(this.googleFunctionAccount != null && this.azureAccount != null && this.awsAccount != null ){
							master = new MasterThread(this.getGoogleFunctionAccount(), this.getAzureAccount(), this.getAwsAccount(), function);
						} else if(this.azureAccount != null && this.googleFunctionAccount != null) {
							 master = new MasterThread(this.getGoogleFunctionAccount(), this.getAzureAccount(), function);
						}else{
							master= new MasterThread(this.getAwsAccount(), this.getIbmAccount(), function);
						}
						master.run();
						// Current Thread will block until it has finished running
						if (master.getResult() == null) {
							throw new InvokationFailureException("Invokation has failed");
						} else {
							return master.getResult();
						}
					}
				}
				MasterThread master = null;
				if(this.googleFunctionAccount != null && this.azureAccount != null && this.awsAccount != null && this.ibmAccount != null){
					master = new MasterThread(this.getGoogleFunctionAccount(), this.getAzureAccount(), this.getAwsAccount(), this.getIbmAccount(), function);
				} else if(this.googleFunctionAccount != null && this.azureAccount!=null && this.ibmAccount != null){
					master = new MasterThread(this.getGoogleFunctionAccount(), this.getAzureAccount(), this.getIbmAccount(), function);
				} else if(this.googleFunctionAccount != null && this.azureAccount != null && this.awsAccount != null ){
					master = new MasterThread(this.getGoogleFunctionAccount(), this.getAzureAccount(), this.getAwsAccount(), function);
				} else if(this.azureAccount != null && this.googleFunctionAccount != null) {
					master = new MasterThread(this.getGoogleFunctionAccount(), this.getAzureAccount(), function);
				}else{
					master= new MasterThread(this.getAwsAccount(), this.getIbmAccount(), function);
				}
				Thread thread = new Thread(master);
				thread.start(); // start new thread to run because we have to be
								// able to cancel
				while (true) {
					// check maxRunningTime
					Timestamp newTime = new Timestamp(System.currentTimeMillis());
					if (function.getConstraints().hasLatestFinishingTime()) {
						if (newTime.after(function.getConstraints().getLatestFinishingTime())) {
							// missed LatestFinishingTime deadline
							master.stop();
							throw new LatestFinishingTimeException("Missed LatestFinishingTime deadline");
						}
					}
					if (function.getConstraints().hasMaxRunningTime()) {
						if (newTime.getTime() - timeAtStart.getTime() > function.getConstraints().getMaxRunningTime()) {
							// Missed MaxRunningTime deadline
							master.stop();
							throw new MaxRunningTimeException("MaxRunningTime has passed");
						}
					}
					if (master.isFinished()) {
						if (master.getResult() == null) {
							throw new InvokationFailureException("Invokation has failed alter entire alternative stategy");
						} else {
							return master.getResult();
						}
					}
					try {
						Thread.sleep(50); // to save CPU cycles
					} catch (InterruptedException e) {
						// ignore
					}
				}
			} else {
				// no constraints. Just invoke in current thread. (we do not
				// need to cancel)
				MasterThread master = null;

				if(this.googleFunctionAccount != null && this.azureAccount != null && this.awsAccount != null && this.ibmAccount != null){
					master = new MasterThread(this.getGoogleFunctionAccount(), this.getAzureAccount(), this.getAwsAccount(), this.getIbmAccount(), function);
				} else if(this.googleFunctionAccount != null && this.azureAccount!=null && this.ibmAccount != null){
					master = new MasterThread(this.getGoogleFunctionAccount(), this.getAzureAccount(), this.getIbmAccount(), function);
				} else if(this.googleFunctionAccount != null && this.azureAccount != null && this.awsAccount != null ){
					master = new MasterThread(this.getGoogleFunctionAccount(), this.getAzureAccount(), this.getAwsAccount(), function);
				} else if(this.azureAccount != null && this.googleFunctionAccount != null) {
					master = new MasterThread(this.getGoogleFunctionAccount(), this.getAzureAccount(), function);
				}else{
					master= new MasterThread(this.getAwsAccount(), this.getIbmAccount(), function);
				}

				master.run(); // will block until it returns
				if (master.getResult() == null) {
					throw new InvokationFailureException("Invokation has failed");
				} else {
					return master.getResult();
				}
			}
		} else {
			throw new InvokationFailureException("Function is null");
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


	public GoogleFunctionAccount getGoogleFunctionAccount() { return this.googleFunctionAccount; }

	public void setGoogleFunctionAccount(GoogleFunctionAccount googleFunctionAccount) { this.googleFunctionAccount = googleFunctionAccount; }

	public AzureAccount getAzureAccount() { return this.azureAccount; }

	public void setAzureAccount(AzureAccount azureAccount) { this.azureAccount = azureAccount; }

}
