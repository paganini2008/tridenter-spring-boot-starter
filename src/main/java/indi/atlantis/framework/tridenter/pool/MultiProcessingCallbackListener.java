package indi.atlantis.framework.tridenter.pool;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.reflection.MethodUtils;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.multicast.ApplicationMessageListener;
import indi.atlantis.framework.tridenter.utils.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * MultiProcessingCallbackListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class MultiProcessingCallbackListener implements ApplicationMessageListener {

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		final Callback callback = (Callback) message;
		final Signature signature = callback.getInvocation().getSignature();

		final Object bean = ApplicationContextUtils.getBean(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()));
		if (bean != null) {
			try {
				MethodUtils.invokeMethod(bean, callback.getMethodName(), callback.getArguments());
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			}
		} else {
			log.warn("No bean registered in spring context to call the method of signature: " + signature);
		}
	}

	@Override
	public String getTopic() {
		return MultiProcessingCallbackListener.class.getName();
	}

}
