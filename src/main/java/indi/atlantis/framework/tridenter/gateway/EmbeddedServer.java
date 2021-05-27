package indi.atlantis.framework.tridenter.gateway;

/**
 * 
 * EmbeddedServer
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface EmbeddedServer {

	int start();

	void stop();

	boolean isStarted();

}
