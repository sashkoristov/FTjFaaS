package requests;

import org.apache.http.HttpResponse;

public interface IResultConverter<T> {
	T convertResult(HttpResponse response);
}
