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

/**
 * 
 * ConsistencyRequestException
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsistencyRequestException extends IllegalStateException {

	private static final long serialVersionUID = -1376569536093800120L;

	public ConsistencyRequestException(String name, long serial, long round) {
		super(repr(name, serial, round));
		this.name = name;
		this.serial = serial;
		this.round = round;
	}

	public ConsistencyRequestException(String name, long serial, long round, Throwable e) {
		super(repr(name, serial, round), e);
		this.name = name;
		this.serial = serial;
		this.round = round;
	}

	private final String name;
	private final long serial;
	private final long round;

	public String getName() {
		return name;
	}

	public long getSerial() {
		return serial;
	}

	public long getRound() {
		return round;
	}

	private static String repr(String name, long serial, long round) {
		return name + "::" + serial + "::" + round;
	}

}
