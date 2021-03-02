package at.uibk.dps;

import at.uibk.dps.function.Function;
import jFaaS.invokers.FaaSInvoker;

public class GoogleFunctionMonitor implements InvokeMonitor{


    @Override
    public String monitoredInvoke(FaaSInvoker invoker, Function function) throws Exception {
        return null;
    }


}
