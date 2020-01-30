package dps.FTinvoker.function;

import java.sql.Timestamp;

/**
 * Class for saving ConstraintSettings for a Function.
 */
public class ConstraintSettings {

	private Timestamp latestStartingTime;
	private Timestamp latestFinishingTime;
	private int maxRunningTime; // in milliseconds, setting this to 0 means no maxRunningTime

	public ConstraintSettings(Timestamp latestStartingTime, Timestamp latestFinishingTime, int maxRunningTime) {
		super();
		this.latestStartingTime = latestStartingTime;
		this.latestFinishingTime = latestFinishingTime;
		this.maxRunningTime = maxRunningTime;
	}

	public Timestamp getLatestStartingTime() {
		return latestStartingTime;
	}

	public void setLatestStartingTime(Timestamp earliestStartingTime) {
		this.latestStartingTime = earliestStartingTime;
	}

	public Timestamp getLatestFinishingTime() {
		return latestFinishingTime;
	}

	public void setLatestFinishingTime(Timestamp latestFinishingTime) {
		this.latestFinishingTime = latestFinishingTime;
	}

	public int getMaxRunningTime() {
		return maxRunningTime;
	}

	public void setMaxRunningTime(int maxRunningTime) {
		this.maxRunningTime = maxRunningTime;
	}

	public boolean hasLatestStartingTime() {
		if (this.latestStartingTime != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasLatestFinishingTime() {
		if (this.latestFinishingTime != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasMaxRunningTime() {
		if (this.maxRunningTime != 0) {
			return true;
		} else {
			return false;
		}
	}
}
