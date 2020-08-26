package at.uibk.dps.database;

import java.util.Comparator;

import at.uibk.dps.function.Function;



public class FunctionSuccessRateComperator implements Comparator<Function>{
	// Needed so we can sort alternative Functions by SuccessRate

	@Override
	public int compare(Function func1, Function func2) {
		return Double.compare(func1.getSuccessRate(), func2.getSuccessRate());
	}	
}
