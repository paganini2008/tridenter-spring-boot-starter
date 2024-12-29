package com.github.doodler.common.transmitter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;
import com.github.doodler.common.utils.ConvertUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: Packet
 * @Author: Fred Feng
 * @Date: 26/12/2024
 * @Version 1.0.0
 */
@Slf4j
public class Packet extends HashMap<String, Object> {

    private static final long serialVersionUID = 1821760688638671763L;

    public static final String DEFAULT_TOPIC = "default";
    public static final String STR_PING = "PING";
    public static final String STR_PONG = "PONG";
    public static final Packet PING = new Packet(DEFAULT_TOPIC, STR_PING);
    public static final Packet PONG = new Packet(DEFAULT_TOPIC, STR_PONG);

    public Packet() {
        this(DEFAULT_TOPIC);
    }

    public Packet(String topic) {
        this(topic, null);
    }

    public Packet(String topic, String content) {
        setField("timestamp", System.currentTimeMillis());
        setTopic(topic);
        setContent(content);
    }

    public void setTopic(String topic) {
        setField("topic", topic);
    }

    public void setCollection(String collection) {
        setField("collection", collection);
    }

    public void setContent(String content) {
        setField("content", content);
    }

    public void setLength(int length) {
        setField("length", length);
    }

    public void setPartitioner(String partitioner) {
        setField("partitioner", partitioner);
    }

    public String getTopic() {
        return getStringField("topic");
    }

    public String getCollection() {
        return getStringField("collection");
    }

    public String getContent() {
        return getStringField("content");
    }

    public long getTimestamp() {
        return getField("timestamp", Long.class);
    }

    public String getPartitioner() {
        return getField("partitioner", String.class);
    }

    public boolean hasField(String fieldName) {
        return containsKey(fieldName);
    }

    public void setField(String fieldName, Object value) {
        put(fieldName, value);
    }

    public Object getField(String fieldName) {
        return get(fieldName);
    }

    public String getStringField(String fieldName) {
        return getField(fieldName, String.class);
    }

    public <T> T getField(String fieldName, Class<T> requiredType) {
        return ConvertUtils.convert(getField(fieldName), requiredType);
    }

    public void fill(Object object) {
        for (String key : keySet()) {
            try {
                PropertyUtils.setProperty(object, key, get(key));
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
    }

    public void append(Map<String, ?> m) {
        putAll(m);
    }

    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(this);
    }

    public Packet copy() {
        return Packet.wrap(this);
    }

    public boolean isPing() {
        return STR_PING.equalsIgnoreCase(getContent());
    }

    public boolean isPong() {
        return STR_PONG.equalsIgnoreCase(getContent());
    }

    public static Packet byString(String content) {
        Packet packet = new Packet();
        packet.setContent(content);
        return packet;
    }

    public static Packet wrap(Map<String, ?> kwargs) {
        Packet packet = new Packet();
        packet.append(kwargs);
        return packet;
    }
}
