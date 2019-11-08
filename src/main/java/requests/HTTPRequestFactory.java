package requests;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * HTTP request factory for creating POST requests.
 * 
 * @author markusmoosbrugger, jakobnoeckl
 *
 */
public class HTTPRequestFactory {

	public static HttpUriRequest getPostRequest(String url, Map<String, String> headers, String body) {
		HttpPost post = new HttpPost(url);

		StringEntity entity = null;
		try {
			entity = new StringEntity(body);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		post.setEntity(entity);

		headers.forEach((k, v) -> post.addHeader(k, v));

		return post;
	}

}
