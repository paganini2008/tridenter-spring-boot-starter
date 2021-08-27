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
package indi.atlantis.framework.tridenter.ccr;

import java.io.Serializable;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * CcResponse
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@ToString
@Setter
@Getter
public class CcrResponse implements Serializable, Comparable<CcrResponse> {
	
	private static final long serialVersionUID = 704634098311834803L;

	public CcrResponse() {
	}

	public CcrResponse(CcrRequest request, ApplicationInfo applicationInfo, boolean acceptable) {
		this.request = request;
		this.applicationInfo = applicationInfo;
		this.acceptable = acceptable;
	}

	private CcrRequest request;
	private ApplicationInfo applicationInfo;
	private boolean acceptable;

	@Override
	public int compareTo(CcrResponse other) {
		long otherSerialNo = other.getRequest().getSerialNo();
		if (otherSerialNo == request.getSerialNo()) {
			return other.getApplicationInfo().getId().compareTo(applicationInfo.getId());
		}
		return (int) (otherSerialNo - request.getSerialNo());
	}

}
