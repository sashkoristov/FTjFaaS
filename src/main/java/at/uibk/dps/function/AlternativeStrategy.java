package at.uibk.dps.function;

import java.util.Iterator;
import java.util.List;

/**
 * AlternativeStrategy that will be executed if the first x retries have failed
 * List of Function Lists. Each Function List represents one possible
 * alternative (that will reach required availability) by invoking multiple
 * functions parallelly Each Function List will be tried once.
 */

public class AlternativeStrategy implements Iterable<List<Function>> {
	private List<List<Function>> alternativeList;

	public AlternativeStrategy(List<List<Function>> alternativeList) {
		this.alternativeList = alternativeList;
	}

	public List<List<Function>> getAlternativeList() {
		return alternativeList;
	}

	public void setAlternativeList(List<List<Function>> alternativeList) {
		this.alternativeList = alternativeList;
	}

	@Override
	public Iterator<List<Function>> iterator() {
		// TODO Auto-generated method stub
		return alternativeList.iterator();
	}
}
