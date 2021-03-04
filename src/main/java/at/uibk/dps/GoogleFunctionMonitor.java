package at.uibk.dps;

import com.google.api.client.http.HttpResponseException;
import at.uibk.dps.function.Function;
import jFaaS.invokers.FaaSInvoker;

public class GoogleFunctionMonitor implements InvokeMonitor{


    @Override
    public String monitoredInvoke(FaaSInvoker invoker, Function function) throws Exception {

        String returnValue = new String();


        try{
            returnValue= invoker.invokeFunction(function.getUrl(), function.getFunctionInputs()).toString();
            assert returnValue!= null;

        } catch(HttpResponseException e){
            int statusCode = e.getStatusCode();

            if(statusCode == 403){
                System.out.println("AuthenticationFailedException");
            }
            else if(statusCode== 503){
                System.out.println("MemoryExceededException");

            }
            else if(statusCode==408){
                System.out.println("TimeLimitException");

            } else{
                System.out.println("Other HTTPException");

            }

        }catch(Exception e){
            System.out.println("Some other Exception");
        }





        return returnValue;

    }


}
