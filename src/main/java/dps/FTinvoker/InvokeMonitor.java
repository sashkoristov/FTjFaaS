package dps.FTinvoker;

import java.util.Map;

import dps.FTinvoker.function.Function;
import dps.invoker.FaaSInvoker;

/**
 * InvokeMonitor interface
 */
public interface InvokeMonitor {

	String monitoredInvoke(FaaSInvoker invoker, Function function) throws Exception;
}
