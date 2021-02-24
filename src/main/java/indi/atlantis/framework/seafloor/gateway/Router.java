package indi.atlantis.framework.seafloor.gateway;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.github.paganini2008.devtools.CharsetUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.io.PathUtils;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * Router
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Accessors(fluent = true)
@Data
public final class Router implements Comparable<Router> {

	public static final Router MISMATCHED = new Router("");

	private final String prefix;
	private final int prefixEndPosition;
	private String provider = "";
	private int retries;
	private int timeout = Integer.MAX_VALUE;
	private int allowedPermits = Integer.MAX_VALUE;
	private boolean cached;
	private boolean forward;
	private boolean stream;
	private Charset charset = CharsetUtils.UTF_8;
	private Class<?> fallback;
	private final MultiValueMap<String, String> defaultHeaders = new LinkedMultiValueMap<String, String>();
	private final List<String> ignoredHeaders = new ArrayList<String>();

	Router(String prefix) {
		if (prefix.endsWith("/")) {
			throw new IllegalArgumentException("Router's prefix must not end with '/'");
		}
		this.prefix = prefix;
		this.prefixEndPosition = PathUtils.indexOfLastSeparator(prefix);
	}

	public Router ignoredHeaders(String[] headerNames) {
		this.ignoredHeaders.addAll(Arrays.asList(headerNames));
		return this;
	}

	public Router defaultHeaders(String[] nameValues) {
		Map<String, String> headerMap = MapUtils.toMap(nameValues);
		for (Map.Entry<String, String> entry : headerMap.entrySet()) {
			this.defaultHeaders.add(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@Override
	public int compareTo(Router other) {
		return other.prefixEndPosition() - prefixEndPosition();
	}

	public String trimPath(String path) {
		return prefixEndPosition >= 0 ? path.substring(prefixEndPosition) : path;
	}

}
