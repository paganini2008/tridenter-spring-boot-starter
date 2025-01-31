package com.github.dingo.rpc;

import static com.github.dingo.TransmitterConstants.ATTR_PACKET_HANDLER;
import static com.github.dingo.TransmitterConstants.TRANSMITTER_SERVER_LOCATION;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.github.dingo.ChannelContext;
import com.github.dingo.NioClient;
import com.github.dingo.Packet;
import com.github.dingo.Partitioner;
import com.github.dingo.SelectedChannelCallback;
import com.github.doodler.common.utils.NetUtils;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @Description: RpcTemplate
 * @Author: Fred Feng
 * @Date: 30/12/2024
 * @Version 1.0.0
 */
@RequiredArgsConstructor
public class RpcTemplate {

    private final NioClient nioClient;
    private final Partitioner partitioner;
    private final DiscoveryClient discoveryClient;

    public Object invokeTargetMethod(String className, String beanName, String methodName,
            Object[] arguments, long timeout, TimeUnit timeUnit) {
        Map<String, Object> kwargs = Map.of("className", className, "beanName", beanName,
                "methodName", methodName, "arguments", arguments);
        Packet packet = Packet.wrap(kwargs);
        packet.setField(ATTR_PACKET_HANDLER, MethodInvocationPacketHandler.class.getName());
        return nioClient.sendAndReturn(packet, partitioner, timeout, timeUnit);
    }

    public Object invokeTargetMethod(String serviceId, String className, String beanName,
            String methodName, Object[] arguments, long timeout, TimeUnit timeUnit) {
        Map<String, Object> kwargs = Map.of("className", className, "beanName", beanName,
                "methodName", methodName, "arguments", arguments);
        Packet packet = Packet.wrap(kwargs);
        packet.setField(ATTR_PACKET_HANDLER, MethodInvocationPacketHandler.class.getName());
        List<SocketAddress> socketAddresses = lookupSocketAddressesFromDiscoveryClient(serviceId);
        return nioClient.sendAndReturn(packet, new SelectedChannelCallback() {
            @Override
            public <T> T doSelectChannel(ChannelContext<T> channelContext) {
                List<T> list = channelContext.getChannels(s -> socketAddresses.contains(s));
                return partitioner.selectChannel(packet, list);
            }
        }, timeout, timeUnit);
    }

    public Object invokeTargetMethod(String className, String beanName, String methodName,
            Object[] arguments) {
        Map<String, Object> kwargs = Map.of("className", className, "beanName", beanName,
                "methodName", methodName, "arguments", arguments);
        Packet packet = Packet.wrap(kwargs);
        packet.setField(ATTR_PACKET_HANDLER, MethodInvocationPacketHandler.class.getName());
        return nioClient.sendAndReturn(packet, partitioner);
    }

    public Object invokeTargetMethod(String serviceId, String className, String beanName,
            String methodName, Object[] arguments) {
        Map<String, Object> kwargs = Map.of("className", className, "beanName", beanName,
                "methodName", methodName, "arguments", arguments);
        Packet packet = Packet.wrap(kwargs);
        packet.setField(ATTR_PACKET_HANDLER, MethodInvocationPacketHandler.class.getName());
        List<SocketAddress> socketAddresses = lookupSocketAddressesFromDiscoveryClient(serviceId);
        return nioClient.sendAndReturn(packet, new SelectedChannelCallback() {
            @Override
            public <T> T doSelectChannel(ChannelContext<T> channelContext) {
                List<T> list = channelContext.getChannels(s -> socketAddresses.contains(s));
                return partitioner.selectChannel(packet, list);
            }
        });
    }

    private List<SocketAddress> lookupSocketAddressesFromDiscoveryClient(String serviceId) {
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceId);
        return serviceInstances.stream().map(i -> i.getMetadata().get(TRANSMITTER_SERVER_LOCATION))
                .map(s -> NetUtils.parse(s)).toList();
    }
}
