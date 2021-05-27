package indi.atlantis.framework.tridenter.http;

import java.util.Map;

/**
 * 
 * ParameterizedRequest
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface ParameterizedRequest extends Request {

	Map<String, Object> getRequestParameters();

	Map<String, Object> getPathVariables();
}
