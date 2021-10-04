/**
* Copyright 2017-2021 Fred Feng (paganini.fy@gmail.com)

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package indi.atlantis.framework.tridenter.xa;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;

import com.github.paganini2008.springdessert.reditools.common.RedisAtomicLongSequence;

/**
 * 
 * XaInterpreter
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
@Aspect
public class XaInterpreter {

	@Qualifier("xaSerialGen")
	@Autowired
	private RedisAtomicLongSequence xaSerialGen;

	@Autowired
	private XaResourceManager xaResourceManager;

	@Qualifier("xaTaskExecutor")
	@Autowired
	private AsyncTaskExecutor taskExecutor;

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(xa)")
	public Object arround(ProceedingJoinPoint pjp, XA xa) throws Throwable {
		final boolean starter = !XaId.has();
		final String xaId = XaId.get();
		MethodInfo methodInfo = MethodInfo.extractFrom(pjp);
		long serial = xaSerialGen.incrementAndGet();
		long timeout = xa.timeout();
		xaResourceManager.beforeInvoking(xaId, serial, methodInfo, timeout);
		boolean success = true;
		try {
			if (starter) {
				Future<Object> future = taskExecutor.submit(() -> invokeMethod(pjp));
				if (timeout > 0) {
					return future.get(timeout, TimeUnit.MILLISECONDS);
				}
				return future.get();
			} else {
				return invokeMethod(pjp);
			}
		} catch (Throwable e) {
			success = false;
			throw e;
		} finally {
			xaResourceManager.afterInvoking(xaId, serial, methodInfo, timeout, success);
			if (starter) {
				XaId.reset();
			}
		}
	}

	private Object invokeMethod(ProceedingJoinPoint pjp) throws Exception {
		try {
			return pjp.proceed();
		} catch (Throwable e) {
			if (e instanceof Exception) {
				throw (Exception) e;
			}
			throw new Error(e);
		}
	}

}
