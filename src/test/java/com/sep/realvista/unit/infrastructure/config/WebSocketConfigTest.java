package com.sep.realvista.unit.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.sep.realvista.infrastructure.config.WebSocketConfig;
import com.sep.realvista.infrastructure.security.websocket.WebSocketAuthenticationInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    @Mock
    WebSocketAuthenticationInterceptor interceptor;

    @Test
    @DisplayName("configureMessageConverters registers a MappingJackson2MessageConverter with SNAKE_CASE ObjectMapper")
    void configureMessageConverters_registersSnakeCaseConverter() {
        WebSocketConfig config = new WebSocketConfig(interceptor);
        List<MessageConverter> converters = new ArrayList<>();

        boolean result = config.configureMessageConverters(converters);

        // Must return false so Spring does NOT add default converters on top
        assertThat(result).isFalse();

        // At least one MappingJackson2MessageConverter must be present
        List<MappingJackson2MessageConverter> jacksonConverters = converters.stream()
                .filter(c -> c instanceof MappingJackson2MessageConverter)
                .map(c -> (MappingJackson2MessageConverter) c)
                .toList();
        assertThat(jacksonConverters).isNotEmpty();

        // The converter's ObjectMapper must use SNAKE_CASE naming strategy
        ObjectMapper om = jacksonConverters.get(0).getObjectMapper();
        assertThat(om.getPropertyNamingStrategy())
                .isInstanceOf(PropertyNamingStrategies.SnakeCaseStrategy.class);
    }
}
