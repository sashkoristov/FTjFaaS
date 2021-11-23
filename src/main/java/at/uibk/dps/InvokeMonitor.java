package at.uibk.dps;

import at.uibk.dps.function.Function;
import jFaaS.invokers.FaaSInvoker;
import jFaaS.utils.PairResult;

/**
 * InvokeMonitor interface
 */
public interface InvokeMonitor {

	PairResult<String, Long> monitoredInvoke(FaaSInvoker invoker, Function function) throws Exception;
}
