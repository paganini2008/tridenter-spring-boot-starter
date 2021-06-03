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
