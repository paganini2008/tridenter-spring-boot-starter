/**
* Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)

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
package io.atlantisframework.tridenter.utils;

/**
 * 
 * SimpleBeanInfo
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
public class SimpleBeanInfo {

	private String beanName;
	private String beanClassName;
	
	public SimpleBeanInfo() {
	}

	public SimpleBeanInfo(String beanName, String beanClassName) {
		this.beanName = beanName;
		this.beanClassName = beanClassName;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getBeanClassName() {
		return beanClassName;
	}

	public void setBeanClassName(String beanClassName) {
		this.beanClassName = beanClassName;
	}

}
