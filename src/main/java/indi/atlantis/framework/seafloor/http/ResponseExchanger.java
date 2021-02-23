package indi.atlantis.framework.seafloor.http;

import org.springframework.http.client.ClientHttpResponse;

/**
 * 
 * ResponseExchanger
 * 
 * @author Jimmy Hoff
 *
 * @version 1.0
 */
public interface ResponseExchanger<T> {

	T exchange(ClientHttpResponse response);

}
