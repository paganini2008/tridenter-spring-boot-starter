package org.springtribe.framework.cluster.pool;

import java.io.Serializable;

import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.io.IOUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * ThrowableProxy
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Getter
@Setter
public class ThrowableProxy implements Serializable {

	private static final long serialVersionUID = 9079267952341059883L;
	private String msg;
	private String className;
	private String[] stackTrace;

	public ThrowableProxy() {
	}

	public ThrowableProxy(String msg, Throwable e) {
		this.msg = msg;
		this.className = e.getClass().getName();
		this.stackTrace = ExceptionUtils.toArray(e);
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Caused by: ");
		for (String trace : stackTrace) {
			str.append(trace).append(IOUtils.NEWLINE);
		}
		return str.toString();
	}

}
