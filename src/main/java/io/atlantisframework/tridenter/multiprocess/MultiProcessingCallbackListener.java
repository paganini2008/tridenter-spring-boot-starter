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
package io.atlantisframework.tridenter.multiprocess;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.reflection.MethodUtils;

import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.multicast.ApplicationMessageListener;
import io.atlantisframework.tridenter.utils.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * MultiProcessingCallbackListener
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
@Slf4j
public class MultiProcessingCallbackListener implements ApplicationMessageListener {

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		final Callback callback = (Callback) message;
		final Signature signature = callback.getInvocation().getSignature();
		Object bean = ApplicationContextUtils.getBean(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()));
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
