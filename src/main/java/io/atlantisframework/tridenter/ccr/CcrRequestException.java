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

/**
 * 
 * CcrRequestException
 *
 * @author Fred Feng
 *
 * @since 2.0.1
 */
public class CcrRequestException extends IllegalStateException {

	private static final long serialVersionUID = -1376569536093800120L;

	public CcrRequestException(String name, long serialNo, long round) {
		super(repr(name, serialNo, round));
		this.name = name;
		this.serialNo = serialNo;
		this.round = round;
	}

	public CcrRequestException(String name, long serialNo, long round, Throwable e) {
		super(repr(name, serialNo, round), e);
		this.name = name;
		this.serialNo = serialNo;
		this.round = round;
	}

	private final String name;
	private final long serialNo;
	private final long round;

	public String getName() {
		return name;
	}

	public long getSerialNo() {
		return serialNo;
	}

	public long getRound() {
		return round;
	}

	private static String repr(String name, long serial, long round) {
		return name + "::" + serial + "::" + round;
	}

}
