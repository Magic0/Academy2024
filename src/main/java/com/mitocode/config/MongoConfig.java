package com.mitocode.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MongoConfig  implements InitializingBean {

    @Lazy
    private final MappingMongoConverter mongoConverter;

    @Override
    public void afterPropertiesSet() {
        mongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
    }
}
