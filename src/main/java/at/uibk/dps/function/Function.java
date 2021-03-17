package at.uibk.dps.function;
import java.util.Map;


/**
 * Class that represents a FaaS-Function (and can optionally contain FaultToleranceSettings and ConstraintSettings) 
 * "url" = function url
 * "type" = function type (alternatives must all have the same type)
 * "region" = is the region in which the function is hosted. (this variable is used only by the Database)
 * "FaultToleranceSettings" contain FaultToleranceSettings
 * "ConstraintSettings" contain ConstraintSettings
 * "successRate" is only used by the Scheduler to propose alternativeStrategies
 * 
 */
public class Function {
	private String url;
	private String type;
	private String region; //only used to save to DB
	private Map<String, Object> functionInputs;
	private FaultToleranceSettings FTSettings;
	private ConstraintSettings constraints;
	private double successRate = 0; // Only used for the Scheduler!


	/*public Function(String url,Map<String, Object> functionInputs){
		super();
		this.url = url;
		this.type = null;
		this.functionInputs = functionInputs;
		this.constraints = null;
		this.FTSettings = null;
	}*/

	public Function(String url,String type,Map<String, Object> functionInputs){
		super();
		this.url = url;
		this.type = type;
		this.functionInputs = functionInputs;
		this.constraints = null;
		this.FTSettings = null;
	}
	
	public Function(String url,String type,Map<String, Object> functionInputs,double successRate){
		super();
		this.url = url;
		this.type = type;
		this.functionInputs = functionInputs;
		this.constraints = null;
		this.FTSettings = null;
		this.successRate = successRate;
	}
	
	
	public Function(String url,String type, Map<String, Object> functionInputs , FaultToleranceSettings FTSettings){
		super();
		this.url = url;
		this.type = type;
		this.functionInputs = functionInputs;
		this.FTSettings = FTSettings;
		this.constraints = null;
	}
	
	public Function(String url,String type, Map<String, Object> functionInputs ,ConstraintSettings constraints){
		super();
		this.url = url;
		this.type = type;
		this.functionInputs = functionInputs;
		this.constraints = constraints;
		this.FTSettings = null;
	}
	
	public Function(String url,String type, Map<String, Object> functionInputs , FaultToleranceSettings FTSettings,ConstraintSettings constraints){
		super();
		this.url = url;
		this.type = type;
		this.functionInputs = functionInputs;
		this.FTSettings = FTSettings;
		this.constraints = constraints;
	}

	/*public Function(String url, Map<String, Object> functionInputs , FaultToleranceSettings FTSettings){
		super();
		this.url = url;
		this.functionInputs = functionInputs;
		this.FTSettings = FTSettings;
	}*/
	
	
	 public String toString(){ 
		  return "URL:"+this.url+", Type: "+this.getType();  
		 }

	public Map<String, Object> getFunctionInputs() {
		return functionInputs;
	}

	public void setFunctionInputs(Map<String, Object> functionInputs) {
		this.functionInputs = functionInputs;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public FaultToleranceSettings getFTSettings() {
		return FTSettings;
	}

	public void setFTSettings(FaultToleranceSettings fTSettings) {
		FTSettings = fTSettings;
	}

	public ConstraintSettings getConstraints() {
		return constraints;
	}

	public void setConstraints(ConstraintSettings constraints) {
		this.constraints = constraints;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean hasConstraintSet(){
		if(this.constraints != null){
			return true;
		}else{
			return false;
		}
	}
	
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public boolean hasFTSet(){
		if(this.FTSettings != null){
			return true;
		}else{
			return false;
		}
	}

	public double getSuccessRate() {
		return successRate;
	}

	public void setSuccessRate(double successRate) {
		this.successRate = successRate;
	}
}
