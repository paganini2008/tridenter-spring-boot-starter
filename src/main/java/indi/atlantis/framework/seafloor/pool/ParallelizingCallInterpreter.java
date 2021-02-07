package indi.atlantis.framework.seafloor.pool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.reflection.MethodUtils;

import indi.atlantis.framework.seafloor.utils.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ParallelizingCallInterpreter
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
@Aspect
public class ParallelizingCallInterpreter {

	@Autowired
	private MultiProcessingMethodDetector methodDetector;

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(parallelizing)")
	public Object arround(ProceedingJoinPoint pjp, ParallelizingCall parallelizing) throws Throwable {
		if (((org.aspectj.lang.reflect.MethodSignature) pjp.getSignature()).getMethod().isAnnotationPresent(MultiProcessing.class)) {
			throw new UnsupportedOperationException(
					"Either annotation 'ParallelizingCall' or 'MultiProcessing' is to decorate on target method.");
		}
		Object[] args = pjp.getArgs();
		if (ArrayUtils.isEmpty(args)) {
			throw new IllegalArgumentException("No arguments");
		}

		Signature signature = methodDetector.getSignature(parallelizing.value());
		Object bean = ApplicationContextUtils.getBean(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()));
		if (bean == null) {
			throw new NoSuchBeanDefinitionException(signature.getBeanName());
		}
		try {
			List<Object> results = new ArrayList<Object>();
			Parallelization parallelization = BeanUtils.instantiate(parallelizing.usingParallelization());
			parallelization = ApplicationContextUtils.autowireBean(parallelization);
			Object[] slices = parallelization.slice(args[0]);
			List<Object> methodArgs = new ArrayList<Object>();
			for (Object slice : slices) {
				methodArgs.add(slice);
				if (args.length > 1) {
					methodArgs.addAll(Arrays.asList(ArrayUtils.copy(args, 1)));
				}
				Object result = MethodUtils.invokeMethod(bean, signature.getMethodName(), methodArgs.toArray());
				results.add(result);
				methodArgs.clear();
			}
			return parallelization.merge(results.toArray());
		} catch (Throwable e) {
			if (ExceptionUtils.ignoreException(e, parallelizing.ignoreFor())) {
				log.error(e.getMessage(), e);
				return pjp.proceed();
			}
			throw e;
		}

	}

}
