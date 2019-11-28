package dps.invoker;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.Gson;

import java.util.Map;

/**
 * AWS Lambda invoker using AWS SDK
 */
public class LambdaInvoker implements FaaSInvoker {

    private String awsAccessKey;
    private String awsSecretKey;

    public LambdaInvoker(String awsAccessKey, String awsSecretKey) {
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
    }

    /**
     * Invokes the lambda function
     *
     * @param function       function name or ARN
     * @param functionInputs inputs of the function to invoke
     * @return json result in string format
     */
    public String invokeFunction(String function, Map<String, Object> functionInputs) throws Exception {
        String payload = new Gson().toJson(functionInputs);
        InvokeRequest invokeRequest = new InvokeRequest().withFunctionName(function)
                .withInvocationType(InvocationType.RequestResponse).withPayload(payload);

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

        // TODO check: better add this in constructor? Are default values set?
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSocketTimeout(900 * 1000);

        AWSLambda awsLambda = AWSLambdaClientBuilder.standard().withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withClientConfiguration(clientConfiguration)
                .build();

        InvokeResult invokeResult = null;

        invokeResult = awsLambda.invoke(invokeRequest);

        assert invokeResult != null;
        return new String(invokeResult.getPayload().array());
    }
}