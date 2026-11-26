package com.lmoving.intranet.operationimport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Proporciona el ObjectMapper de Jackson 2 utilizado por el importador Excel.
 * En Spring Boot 4 la configuración automática puede exponer Jackson 3,
 * cuyo tipo no es compatible con com.fasterxml.jackson.databind.ObjectMapper.
 */
@Configuration(proxyBeanMethods = false)
public class OperationImportJacksonConfig {

    @Bean
    public ObjectMapper operationImportObjectMapper() {
        return new ObjectMapper();
    }
}
