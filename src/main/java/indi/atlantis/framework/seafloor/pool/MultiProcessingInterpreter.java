package indi.atlantis.framework.seafloor.pool;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.StringUtils;

import indi.atlantis.framework.seafloor.multicast.ApplicationMulticastGroup;
import indi.atlantis.framework.seafloor.utils.BeanExpressionUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * MultiProcessingInterpreter
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
@Aspect
public class MultiProcessingInterpreter {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private ProcessPool processPool;

	@Autowired
	private MultiProcessingMethodDetector methodDetector;

	@Autowired
	private InvocationBarrier invocationBarrier;

	@Autowired
	private ApplicationMulticastGroup multicastGroup;

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(multiProcessing)")
	public Object arround(ProceedingJoinPoint pjp, MultiProcessing multiProcessing) throws Throwable {
		final Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		if (invocationBarrier.isCompleted()) {
			try {
				return pjp.proceed();
			} catch (Throwable e) {
				if (ExceptionUtils.ignoreException(e, multiProcessing.ignoredFor())) {
					Signature signature = methodDetector.getSignature(multiProcessing.value());
					if (StringUtils.isNotBlank(signature.getFailureMethodName())) {
						multicastGroup.unicast(applicationName, MultiProcessingCallbackListener.class.getName(),
								new FailureCallback(new MethodInvocation(signature, pjp.getArgs()), e));
					}
					if (StringUtils.isNotBlank(multiProcessing.defaultValue())) {
						return BeanExpressionUtils.resolveExpression(multiProcessing.defaultValue(), method.getReturnType());
					}
				}
				throw e;
			}
		} else {
			Signature signature = methodDetector.getSignature(multiProcessing.value());
			MethodInvocation invocation = new MethodInvocation(signature, pjp.getArgs());
			try {
				if (multiProcessing.async()) {
					processPool.execute(invocation);
					return ClassUtils.getNullableValue(method.getReturnType());
				} else {
					TaskPromise promise = processPool.submit(invocation);
					Supplier<Object> defaultValue = StringUtils.isNotBlank(multiProcessing.defaultValue()) ? () -> {
						return BeanExpressionUtils.resolveExpression(multiProcessing.defaultValue(), method.getReturnType());
					} : null;
					return multiProcessing.timeout() > 0 ? promise.get(multiProcessing.timeout(), TimeUnit.MILLISECONDS, defaultValue)
							: promise.get(defaultValue);
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				throw e;
			}
		}
	}

}
