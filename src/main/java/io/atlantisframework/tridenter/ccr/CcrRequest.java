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
package io.atlantisframework.tridenter.ccr;

import java.io.Serializable;
import java.util.UUID;

import com.github.paganini2008.devtools.beans.ToStringBuilder;

import io.atlantisframework.tridenter.ApplicationInfo;
import lombok.Getter;

/**
 * 
 * CcrRequest
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Getter
public class CcrRequest implements Serializable, Cloneable {

	private static final long serialVersionUID = 7128587826224341606L;

	public static final String PREPARATION_REQUEST = "<Perparation Request>";
	public static final String PREPARATION_RESPONSE = "<Perparation Response>";
	public static final String COMMITMENT_REQUEST = "<Commitment Request>";
	public static final String COMMITMENT_RESPONSE = "<Commitment Response>";
	public static final String LEARNING_REQUEST = "<Learning Request>";
	public static final String LEARNING_RESPONSE = "<Learning Response>";
	public static final String TIMEOUT_REQUEST = "<Request Timeout>";
	public static final String TIMEOUT_RESPONSE = "<Response Timeout>";

	private ApplicationInfo applicationInfo;
	private String id;
	private String name;
	private Object value;
	private long serialNo;
	private long batchNo;
	private long timestamp;
	private long timeout;

	public CcrRequest() {
	}

	CcrRequest(ApplicationInfo applicationInfo) {
		this.id = UUID.randomUUID().toString();
		this.applicationInfo = applicationInfo;
		this.timestamp = System.currentTimeMillis();
	}

	public CcrRequest setName(String name) {
		this.name = name;
		return this;
	}

	public CcrRequest setValue(Object value) {
		this.value = value;
		return this;
	}

	public CcrRequest setBatchNo(long batchNo) {
		this.batchNo = batchNo;
		return this;
	}

	public CcrRequest setSerialNo(long serialNo) {
		this.serialNo = serialNo;
		return this;
	}

	public CcrRequest setTimeout(long timeout) {
		this.timeout = timeout;
		return this;
	}

	public boolean hasExpired(long ms) {
		return (timeout > 0) && (ms - timestamp > timeout);
	}

	public CcrResponse ack(ApplicationInfo applicationInfo, boolean acceptable) {
		return new CcrResponse(this, applicationInfo, acceptable);
	}

	@Override
	public CcrRequest clone() {
		try {
			return (CcrRequest) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, new String[] { "applicationInfo" });
	}

	public static CcrRequest of(ApplicationInfo applicationInfo) {
		return new CcrRequest(applicationInfo);
	}

}
