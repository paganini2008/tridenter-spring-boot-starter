package indi.atlantis.framework.seafloor.gateway;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * CachedResponseEntity
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Getter
@Setter
public class CachedResponseEntity {

	private Object body;
	private HttpHeaders headers;
	private HttpStatus status;

	CachedResponseEntity(Object body, HttpHeaders headers, HttpStatus status) {
		this.body = body;
		this.headers = headers;
		this.status = status;
	}

}
