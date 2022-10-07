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
package io.atlantisframework.tridenter;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.beans.ToStringBuilder;

import io.atlantisframework.tridenter.utils.Contact;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * ApplicationInfo
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
@JsonInclude(value = Include.NON_NULL)
@Getter
@Setter
public class ApplicationInfo implements Serializable, Comparable<ApplicationInfo> {

	private static final long serialVersionUID = 2499029995227541654L;

	private String id;
	private String clusterName;
	private String applicationName;
	private int weight;
	private long startTime;
	@JsonProperty("leader")
	private boolean isLeader;
	private ApplicationInfo leaderInfo;
	private String applicationContextPath;
	private Contact contact;

	public ApplicationInfo() {
	}

	ApplicationInfo(String id, String clusterName, String applicationName, ApplicationInfo leaderInfo) {
		Assert.hasNoText(id);
		Assert.hasNoText(clusterName);
		Assert.hasNoText(applicationName);

		this.id = id;
		this.clusterName = clusterName;
		this.applicationName = applicationName;
		this.leaderInfo = leaderInfo;
		this.isLeader = leaderInfo != null && id.equals(leaderInfo.getId());
	}

	@JsonIgnore
	public boolean isLeader() {
		return isLeader;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (clusterName != null ? 0 : clusterName.hashCode());
		result = prime * result + (id != null ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ApplicationInfo) {
			ApplicationInfo other = (ApplicationInfo) obj;
			return getClusterName().equals(other.getClusterName()) && getId().equals(other.getId());
		}
		return false;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new String[] { "contact", "leaderInfo" });
	}

	@Override
	public int compareTo(ApplicationInfo otherInfo) {
		String left = String.format("%s:%d:%s", clusterName, startTime, id);
		String right = String.format("%s:%d:%s", otherInfo.getClusterName(), otherInfo.getStartTime(), otherInfo.getId());
		return left.compareTo(right);
	}

}
