package indi.atlantis.framework.tridenter.consistency;

import org.springframework.context.ApplicationListener;

/**
 * 
 * ConsistentStateEventListener
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class ConsistentStateEventListener implements ApplicationListener<ConsistencyRequestConfirmationEvent> {

	@Override
	public void onApplicationEvent(ConsistencyRequestConfirmationEvent event) {

	}

}
