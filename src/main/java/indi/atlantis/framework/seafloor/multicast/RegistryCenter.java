package indi.atlantis.framework.seafloor.multicast;

import java.util.List;

import indi.atlantis.framework.seafloor.ApplicationInfo;

/**
 * 
 * RegistryCenter
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface RegistryCenter {

	void registerApplication(ApplicationInfo applicationInfo);

	void removeApplication(ApplicationInfo applicationInfo);

	List<ApplicationInfo> getApplications();

	List<ApplicationInfo> getApplications(String applicationName);

	int countOfApplication();

}
