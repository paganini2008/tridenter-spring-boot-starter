package org.springtribe.framework.cluster.pool;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * MethodSignature
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@JsonInclude(value = Include.NON_NULL)
@Getter
@Setter
public class MethodSignature implements Serializable, Signature {

	private static final long serialVersionUID = 3733605394122091343L;
	private String beanName;
	private String beanClassName;
	private String methodName;
	private @Nullable String successMethodName;
	private @Nullable String failureMethodName;

	public MethodSignature() {
	}

	MethodSignature(String beanName, String beanClassName, String methodName) {
		this.beanName = beanName;
		this.beanClassName = beanClassName;
		this.methodName = methodName;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static Signature of(Method method) {
		String beanClassName = method.getDeclaringClass().getName();
		String methodName = method.getName();
		return new MethodSignature(null, beanClassName, methodName);
	}

}
