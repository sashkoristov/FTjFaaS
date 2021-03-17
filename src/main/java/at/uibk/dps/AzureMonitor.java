package at.uibk.dps;

import at.uibk.dps.exception.AuthenticationFailedException;
import at.uibk.dps.exception.InvalidResourceException;
import at.uibk.dps.exception.MemoryExceededException;
import at.uibk.dps.exception.TimeLimitException;
import com.google.api.client.http.HttpResponseException;
import at.uibk.dps.function.Function;
import jFaaS.invokers.AzureInvoker;
import jFaaS.invokers.FaaSInvoker;

import java.util.HashMap;
import java.util.Map;


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
                System.out.println("AuthenticationFailedException");
                throw new AuthenticationFailedException(e.getMessage());
            }
            else if(statusCode== 502){
                System.out.println("TimeLimitException");
                throw new TimeLimitException(e.getMessage());

            }
            else if(statusCode==500){
                System.out.println("MemoryExceededException");
                throw new MemoryExceededException(e.getMessage());

            } else{
                System.out.println("Other HTTPException");
                throw e;

            }

        }catch(Exception e){
            System.out.println("Some other Exception occured");
            throw e;
        }

        return returnValue;

    }

    /*public static void main(String[] args) throws Exception {



        Map<String, Object> inputs = new HashMap<>();
        inputs.put("name", "Elsa");

        String azureKey = "3W8/qsen1wJDwYHBxfxyFpFoIixMOy2ZGf2ITTb1CHhJj3ece00QEA==";

        AzureAccount acc = new AzureAccount(azureKey);
        GoogleFunctionAccount accgoogle = new GoogleFunctionAccount();


        AzureInvoker invoker = new AzureInvoker(azureKey);
        Function function2 = new Function("https://helloworld111.azurewebsites.net/api/deniedFunction", inputs);
        Function function = new Function("https://helloworld111.azurewebsites.net/api/timeoutFunction", inputs);
        Function function1 = new Function("https://helloworld111.azurewebsites.net/api/memoryFunction", inputs);


        AzureMonitor monitor = new AzureMonitor();
        //String result = monitor.monitoredInvoke(invoker, function1);
        //System.out.println(result);



        try{InvokationThread invok = new InvokationThread(accgoogle, acc, function);

        invok.run();}catch(Exception e){e.getMessage();}


    }*/


}
