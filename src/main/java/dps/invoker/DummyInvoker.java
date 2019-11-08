package dps.invoker;

import java.util.Map;

/**
 * Dummy Invoker which can be used to return the needed string. Only used for
 * test purposes.
 */
public class DummyInvoker implements FaaSInvoker {

	@Override
	public String invokeFunction(String function, Map<String, Object> parameters) throws Exception {
		return "0";
	}
}
