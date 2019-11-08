package requests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Response converter to convert integer and string objects from JSON objects.
 * 
 * @author markusmoosbrugger, jakobnoeckl
 *
 */
public class ResponseConverter {

	public static IResultConverter<?> getResponseConverter(String ouputType) {
		IResultConverter<?> resultHandler = (response) -> {
			try {
				InputStream is = response.getEntity().getContent();
				String stringResponse = IOUtils.toString(is, StandardCharsets.UTF_8.name());
				is.close();
				return stringResponse;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "Error while parsing response.";
		};

		switch (ouputType) {
		case "integer":
			resultHandler = (response) -> {
				try {
					InputStream is = response.getEntity().getContent();
					String stringResponse = IOUtils.toString(is, StandardCharsets.UTF_8.name());
					is.close();
					JsonObject jobj = new Gson().fromJson(stringResponse, JsonObject.class);
					return jobj.get("payload").getAsInt();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return "Error while parsing response.";

			};
			break;
		case "String":
			resultHandler = (response) -> {
				try {
					String stringResponse = null;
					InputStream is = response.getEntity().getContent();
					stringResponse = IOUtils.toString(is, StandardCharsets.UTF_8.name());
					is.close();
					JsonObject jobj = new Gson().fromJson(stringResponse, JsonObject.class);
					String rs = jobj.toString();

					return rs;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return "Error while parsing response.";

			};
			break;
		default:
			break;
		}

		return resultHandler;
	}
}
