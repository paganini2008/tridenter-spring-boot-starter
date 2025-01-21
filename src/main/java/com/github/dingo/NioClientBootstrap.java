package com.github.dingo;

import java.util.Collection;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import com.github.doodler.common.cloud.AffectedApplicationInfo;
import com.github.doodler.common.cloud.AffectedApplicationInfo.AffectedType;
import com.github.doodler.common.cloud.ApplicationInfoManager;
import com.github.doodler.common.cloud.SiblingApplicationInfoChangeEvent;
import com.github.doodler.common.utils.MapUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: NioClientBootstrap
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class NioClientBootstrap {

    private final TransmitterNioProperties nioProperties;
    private final NioClient nioClient;
    private final ChannelSwitcher channelSwitch;
    private final ApplicationInfoManager applicationInfoManager;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent(ApplicationReadyEvent event) {
        if (nioProperties.getClient().isConnectWithSelf()) {
            final String serverLocation = applicationInfoManager.getMetadata()
                    .get(TransmitterConstants.TRANSMITTER_SERVER_LOCATION);
            if (StringUtils.isBlank(serverLocation)) {
                throw new TransmitterException("Unable to connect with myself");
            }
            nioClient.connect(serverLocation, addr -> {
                log.info("Successfully connected to address: {}", addr.toString());
                channelSwitch.enableInternalChannel(addr);
            });
        } else {
            log.warn(
                    "=============================================================================================");
            log.warn(
                    "| No available internal channel will be used in the future until external channel coming in.|");
            log.warn(
                    "=============================================================================================");
        }
    }

    @EventListener(SiblingApplicationInfoChangeEvent.class)
    public void onSiblingApplicationInfoChangeEvent(SiblingApplicationInfoChangeEvent event) {
        Collection<AffectedApplicationInfo> affectedApplications = event.getAffectedApplications();
        if (CollectionUtils.isNotEmpty(affectedApplications)) {
            affectedApplications.stream()
                    .filter(app -> app.getAffectedType().equals(AffectedType.ONLINE))
                    .forEach(app -> {
                        Map<String, String> metadata = app.getApplicationInfo().getMetadata();
                        if (MapUtils.isEmpty(metadata)) {
                            log.warn("No Metadata in AppInfo: {}", app.getApplicationInfo());
                            return;
                        }
                        String serviceLocation = (String) metadata
                                .get(TransmitterConstants.TRANSMITTER_SERVER_LOCATION);
                        if (StringUtils.isBlank(serviceLocation)) {
                            log.warn("No 'TRANSMITTER_SERVER_LOCATION' in Metadata. AppInfo: {}",
                                    app.getApplicationInfo());
                            return;
                        }
                        nioClient.connect(serviceLocation, addr -> {
                            log.info("Successfully connected to address: {}", addr.toString());
                            channelSwitch.enableExternalChannel(addr,
                                    nioProperties.isDefaultExternalChannelAccessable());
                        });
                    });
        }
    }
}
