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

	public FaultToleranceEngine(AWSAccount awsAccount, IBMAccount ibmAccount) {
		
		this.awsAccount = awsAccount;
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
						MasterThread master = new MasterThread(getAwsAccount(), getIbmAccount(), function);
						master.run();
						// Current Thread will block until it has finished running
						if (master.getResult() == null) {
							throw new InvokationFailureException("Invokation has failed");
						} else {
							return master.getResult();
						}
					}
				}
				MasterThread master = new MasterThread(getAwsAccount(), getIbmAccount(), function);
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
				MasterThread master = new MasterThread(getAwsAccount(), getIbmAccount(), function);
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

}
