package dps.FTinvoker.function;


/**
 * Class for saving FaultToleranceSettings of a  Function 
 * "retries" is the number of retries before we will start invoking AlternativeStrategy
 * AlternativeStrategy is a List of Function Lists (Multiple functions to invoke paralelly to reach wanted availability)
 */
public class FaultToleranceSettings {

	private int retries;
	private AlternativeStrategy altStrategy;

	public FaultToleranceSettings(int retries) {
		super();
		this.retries = retries;
		this.altStrategy = null;
	}

	public FaultToleranceSettings(int retries, AlternativeStrategy altStrategy) {
		super();
		this.retries = retries;
		this.altStrategy = altStrategy;
	}

	public int getRetries() {
		return this.retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public AlternativeStrategy getAltStrategy() {
		return altStrategy;
	}

	public void setAltStrategy(AlternativeStrategy altStrategy) {
		this.altStrategy = altStrategy;
	}

	public boolean hasAlternativeStartegy() {
		if (this.altStrategy != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isEmpty(){
		if (this.getRetries() == 0 && this.hasAlternativeStartegy() == false){
			return true;
		}else{
			return false;
		}
	}
}
