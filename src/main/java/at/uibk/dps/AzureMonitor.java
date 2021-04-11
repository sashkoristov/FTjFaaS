package at.uibk.dps;

import at.uibk.dps.exception.AuthenticationFailedException;
import at.uibk.dps.exception.MemoryExceededException;
import at.uibk.dps.exception.TimeLimitException;
import com.google.api.client.http.HttpResponseException;
import at.uibk.dps.function.Function;
import jFaaS.invokers.FaaSInvoker;




/**
 * Method to invoke Azure Functions with Monitoring (using jFaas.jar)
 * Since the AzureFunction uses HTTPTriggers, different HTTPResponseException status codes are interpreted based on observations
 * More specific Exceptions from exception package are thrown to be handled down the line
 */
public class AzureMonitor implements InvokeMonitor{


    @Override
    public String monitoredInvoke(FaaSInvoker invoker, Function function) throws Exception {

        String returnValue;

        try{
            returnValue= invoker.invokeFunction(function.getUrl(), function.getFunctionInputs()).toString();
            assert returnValue!= null;

        } catch(HttpResponseException e){
            int statusCode = e.getStatusCode();

            if(statusCode == 401){
                throw new AuthenticationFailedException(e.getMessage());
            }
            else if(statusCode== 502){
                throw new TimeLimitException(e.getMessage());

            }
            else if(statusCode==500){
                throw new MemoryExceededException(e.getMessage());

            } else{
                throw e;

            }

        }catch(Exception e){
            throw e;
        }

        return returnValue;

    }



}
