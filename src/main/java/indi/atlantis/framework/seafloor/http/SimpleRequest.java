package indi.atlantis.framework.seafloor.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.github.paganini2008.devtools.collection.LruMap;
import com.github.paganini2008.devtools.collection.MapUtils;

/**
 * 
 * SimpleRequest
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class SimpleRequest implements Request {

	private static final LruMap<String, SimpleRequest> cache = new LruMap<String, SimpleRequest>();

	private final String path;

	SimpleRequest(String path) {
		this.path = path;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public HttpMethod getMethod() {
		throw new UnsupportedOperationException("getMethod");
	}

	@Override
	public HttpHeaders getHeaders() {
		throw new UnsupportedOperationException("getHeaders");
	}

	@Override
	public HttpEntity<Object> getBody() {
		throw new UnsupportedOperationException("getBody");
	}

	@Override
	public long getTimestamp() {
		throw new UnsupportedOperationException("getTimestamp");
	}
	

	public static Request of(final String path) {
		return MapUtils.get(cache, path, () -> {
			return new SimpleRequest(path);
		});
	}

}
