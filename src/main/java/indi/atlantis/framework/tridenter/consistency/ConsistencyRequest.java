/**
* Copyright 2018-2021 Fred Feng (paganini.fy@gmail.com)

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
package indi.atlantis.framework.tridenter.consistency;

import java.io.Serializable;
import java.util.UUID;

import com.github.paganini2008.devtools.date.DateUtils;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import lombok.Getter;

/**
 * 
 * ConsistencyRequest
 *
 * @author Fred Feng
 * @since 1.0
 */
@Getter
public class ConsistencyRequest implements Serializable {

	private static final long serialVersionUID = 7128587826224341606L;
	
	public static final String PREPARATION_OPERATION_REQUEST = "<Perparation Operation Request>";
	public static final String PREPARATION_OPERATION_RESPONSE = "<Perparation Operation Response>";
	public static final String COMMITMENT_OPERATION_REQUEST = "<Commitment Operation Request>";
	public static final String COMMITMENT_OPERATION_RESPONSE = "<Commitment Operation Response>";
	public static final String LEARNING_OPERATION_REQUEST = "<Learning Operation Request>";
	public static final String LEARNING_OPERATION_RESPONSE = "<Learning Operation Response>";
	public static final String TIMEOUT_OPERATION_REQUEST = "<Timeout Operation Request>";
	public static final String TIMEOUT_OPERATION_RESPONSE = "<Timeout Operation Response>";

	private ApplicationInfo applicationInfo;
	private String id;
	private String name;
	private Object value;
	private long serial;
	private long round;
	private long timestamp;
	private int timeout;

	public ConsistencyRequest() {
	}

	ConsistencyRequest(ApplicationInfo applicationInfo) {
		this.id = UUID.randomUUID().toString();
		this.applicationInfo = applicationInfo;
		this.timestamp = System.currentTimeMillis();
	}

	public static ConsistencyRequest of(ApplicationInfo applicationInfo) {
		return new ConsistencyRequest(applicationInfo);
	}

	public ConsistencyRequest setName(String name) {
		this.name = name;
		return this;
	}

	public ConsistencyRequest setValue(Object value) {
		this.value = value;
		return this;
	}

	public ConsistencyRequest setRound(long round) {
		this.round = round;
		return this;
	}

	public ConsistencyRequest setSerial(long serial) {
		this.serial = serial;
		return this;
	}

	public ConsistencyRequest setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public boolean hasExpired() {
		return timeout > 0 && System.currentTimeMillis() - timestamp > timeout * 1000;
	}

	public ConsistencyResponse ack(ApplicationInfo applicationInfo, boolean acceptable) {
		return new ConsistencyResponse(this, applicationInfo, acceptable);
	}

	public ConsistencyRequest copy() {
		return ConsistencyRequest.of(applicationInfo).setName(name).setRound(round).setValue(value).setSerial(serial);
	}

	public String toString() {
		return "[" + round + "/" + serial + "] name: " + name + ", value: " + (value != null ? value.toString() : "NULL") + ", date: "
				+ DateUtils.format(timestamp);
	}

}
