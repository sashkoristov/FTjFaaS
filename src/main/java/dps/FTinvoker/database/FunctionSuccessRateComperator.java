package dps.FTinvoker.database;

import java.util.Comparator;
import dps.FTinvoker.function.Function;



public class FunctionSuccessRateComperator implements Comparator<Function>{
	// Needed so we can sort alternative Functions by SuccessRate

	@Override
	public int compare(Function func1, Function func2) {
		return Double.compare(func1.getSuccessRate(), func2.getSuccessRate());
	}	
}
