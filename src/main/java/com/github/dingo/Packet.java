package com.github.dingo;

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
        setTimestamp(System.currentTimeMillis());
        setTopic(topic);
        setContent(content);
    }

    public void setTimestamp(long ms) {
        setField("timestamp", ms);
    }

    public void setTopic(String topic) {
        setField("topic", topic);
    }

    public void setContent(String content) {
        setField("content", content);
    }

    public void setLength(long length) {
        setField("length", length);
    }

    public void setPartitioner(String partitioner) {
        setField("partitioner", partitioner);
    }

    public void setMode(String mode) {
        setField("mode", mode);
    }

    public void setObject(Object data) {
        setField("data", data);
    }

    public String getTopic() {
        return getStringField("topic");
    }

    public String getContent() {
        return getStringField("content");
    }

    public Long getLength() {
        return getLongField("length");
    }

    public long getTimestamp() {
        return getLongField("timestamp");
    }

    public String getPartitioner() {
        return getStringField("partitioner");
    }

    public String getMode() {
        return getStringField("mode");
    }

    public Object getObject() {
        return getField("data");
    }

    public boolean hasField(String fieldName) {
        return containsKey(fieldName);
    }

    public void setField(String fieldName, Object value) {
        if (value != null) {
            put(fieldName, value);
        }
    }

    public Object getField(String fieldName) {
        return get(fieldName);
    }

    public Object getField(String fieldName, Object defaultValue) {
        return getOrDefault(fieldName, defaultValue);
    }

    public Double getDoubleField(String fieldName) {
        return getField(fieldName, Double.class);
    }

    public Integer getIntegerField(String fieldName) {
        return getField(fieldName, Integer.class);
    }

    public Long getLongField(String fieldName) {
        return getField(fieldName, Long.class);
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

    public static Packet wrap(String content) {
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
