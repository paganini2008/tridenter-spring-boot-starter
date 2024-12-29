package com.github.doodler.common.transmitter;

import java.util.Collection;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
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
public class NioClientBootstrap implements InitializingBean {

    private final TransmitterNioProperties nioProperties;
    private final NioClient nioClient;
    private final ApplicationInfoManager applicationInfoManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (nioProperties.isConnectWithSelf()) {
            String serverLocation = applicationInfoManager.getMetadata()
                    .get(TransmitterConstants.TRANSMITTER_SERVER_LOCATION);
            nioClient.connect(serverLocation, addr -> {
                log.info("Successfully connected to address: {}", addr.toString());
            });
        }
    }

    @EventListener(SiblingApplicationInfoChangeEvent.class)
    public void onSiblingApplicationInfoChangeEvent(SiblingApplicationInfoChangeEvent event) {
        Collection<AffectedApplicationInfo> affectedApplications = event.getAffects();
        if (CollectionUtils.isNotEmpty(affectedApplications)) {
            affectedApplications.stream()
                    .filter(app -> app.getAffectedType().equals(AffectedType.ONLINE))
                    .forEach(app -> {
                        Map<String, String> metadata = app.getApplicationInfo().getMetadata();
                        if (MapUtils.isEmpty(metadata)) {
                            log.warn("No metadata in app: {}", app);
                            return;
                        }
                        String serverLocation = (String) metadata
                                .get(TransmitterConstants.TRANSMITTER_SERVER_LOCATION);
                        if (StringUtils.isBlank(serverLocation)) {
                            log.warn("No 'TRANSMITTER_SERVER_LOCATION' in metadata: {}", metadata);
                            return;
                        }
                        nioClient.connect(serverLocation, addr -> {
                            log.info("Successfully connected to address: {}", addr.toString());
                        });
                    });
        }
    }



}
