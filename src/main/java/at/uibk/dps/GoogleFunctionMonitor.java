package at.uibk.dps;

import at.uibk.dps.exception.AuthenticationFailedException;
import at.uibk.dps.exception.MemoryExceededException;
import at.uibk.dps.exception.TimeLimitException;
import at.uibk.dps.function.Function;
import com.google.api.client.http.HttpResponseException;
import jFaaS.invokers.FaaSInvoker;
import jFaaS.utils.PairResult;


/**
 * Method to invoke Google Functions with Monitoring (using jFaas.jar)
 * Since the GoogleInvoker uses HTTPTriggers, different HTTPResponseException status codes are interpreted
 * More specific Exceptions from exception package are thrown to be handled down the line
 */
public class GoogleFunctionMonitor implements InvokeMonitor{


    @Override
    public PairResult<String, Long> monitoredInvoke(FaaSInvoker invoker, Function function) throws Exception {

        PairResult<String, Long> returnValue = null;


        try {
            returnValue = invoker.invokeFunction(function.getUrl(), function.getFunctionInputs());
            assert returnValue != null;

        } catch (HttpResponseException e) {
            int statusCode = e.getStatusCode();

            if (statusCode == 403) {
                throw new AuthenticationFailedException(e.getMessage());
            } else if (statusCode == 503) {
                throw new MemoryExceededException(e.getMessage());

            } else if (statusCode == 408) {
                throw new TimeLimitException(e.getMessage());

            } else {
                throw e;

            }

        }catch(Exception e){
            throw e;
        }

        return returnValue;

    }


}
