package dps.invoker;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import requests.HTTPRequestFactory;
import requests.IResultConverter;
import requests.ResponseConverter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OpenWhiskInvoker implements FaaSInvoker {

    private String key;

    public OpenWhiskInvoker(String key){
        this.key = key;
    }

    public String invokeFunction(String function, Map<String, Object> functionInputs) throws Exception {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("Authorization",
                "Basic " + key);
        header.put("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        header.put("Accept-Language", "en-At");

        IResultConverter<?> resultHandler = ResponseConverter.getResponseConverter("String");
        String functionParameters = "?blocking=true&result=true";
        Gson g = new Gson();
        String body = g.toJson(functionInputs);
        HttpUriRequest request = HTTPRequestFactory.getPostRequest(function + functionParameters, header, body);
        HttpClient httpClient = getHttpCLientForSSL();

        HttpResponse response = null;
        response = httpClient.execute(request);

        return (String) resultHandler.convertResult(response);
    }

    private HttpClient getHttpCLientForSSL() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

        } };

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            SSLConnectionSocketFactory sslfactory = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);

            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslfactory)
                    .setConnectionTimeToLive(10, TimeUnit.SECONDS).build();

            return httpClient;

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        return HttpClients.createDefault();
    }
}
