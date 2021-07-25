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
package indi.atlantis.framework.tridenter.utils;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

/**
 * 
 * Contact
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@JsonInclude(value = Include.NON_NULL)
@Data
@ConfigurationProperties("spring.application.cluster.contact")
public class Contact implements Serializable {

	private static final long serialVersionUID = 4110243793757357219L;
	private String name = "Fred Feng";
	private String campany;
	private String department;
	private String position;
	private String homePage = "https://github.com/paganini2008";
	private String email = "paganini.fy@gmail.com";
	private String phone;
	private String description;

}
