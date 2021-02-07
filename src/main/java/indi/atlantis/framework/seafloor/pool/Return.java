package indi.atlantis.framework.seafloor.pool;

import java.io.Serializable;

import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Return
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Getter
@Setter
public class Return implements Serializable {

	private static final long serialVersionUID = 5736144131241770067L;
	private Invocation invocation;
	private Object returnValue;

	public Return() {
	}

	Return(Invocation invocation, Object returnValue) {
		this.invocation = invocation;
		this.returnValue = returnValue;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
