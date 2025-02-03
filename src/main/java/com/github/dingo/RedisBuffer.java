/*
 * Copyright 2017-2025 Fred Feng (paganini.fy@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.dingo;

import static com.github.doodler.common.Constants.ISO8601_DATE_TIME_PATTERN;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.doodler.common.events.Buffer;
import com.github.doodler.common.utils.JacksonUtils;

/**
 * 
 * @Description: RedisBuffer
 * @Author: Fred Feng
 * @Date: 26/12/2024
 * @Version 1.0.0
 */
public class RedisBuffer implements Buffer<Packet> {

    private final String namespace;
    private final RedisTemplate<String, Packet> redisTemplate;

    public RedisBuffer(String namespace, RedisConnectionFactory redisConnectionFactory) {
        this.namespace = namespace;

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = jsonRedisSerializer();
        RedisTemplate<String, Packet> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();

        this.redisTemplate = redisTemplate;
    }

    private static Jackson2JsonRedisSerializer<Object> jsonRedisSerializer() {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        om.setDateFormat(new SimpleDateFormat(ISO8601_DATE_TIME_PATTERN));
        SimpleModule javaTimeModule = JacksonUtils.getJavaTimeModuleForWebMvc();
        om.registerModule(javaTimeModule);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<Object>(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        return jackson2JsonRedisSerializer;
    }

    @Override
    public void put(Packet packet) {
        redisTemplate.opsForList().leftPush(getKey(namespace), packet);
    }

    protected String getKey(String namespace) {
        return namespace;
    }

    @Override
    public long size() {
        Number result = redisTemplate.opsForList().size(getKey(namespace));
        return result != null ? result.longValue() : 0;
    }

    @Override
    public Packet poll() {
        return redisTemplate.opsForList().rightPop(getKey(namespace));
    }

    @Override
    public Collection<Packet> poll(long fetchSize) {
        if (fetchSize == 1) {
            return Collections.singletonList(poll());
        }
        List<Packet> results = redisTemplate.opsForList().rightPop(getKey(namespace), fetchSize);
        return results != null && results.size() > 0 ? Collections.unmodifiableCollection(results)
                : Collections.emptyList();
    }

}
