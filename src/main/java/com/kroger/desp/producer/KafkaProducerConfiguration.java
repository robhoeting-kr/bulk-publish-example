package com.kroger.desp.producer;

import java.util.*;

import com.kroger.desp.events.despelmr.test.BadThingHappened;
import com.kroger.desp.events.despelmr.test.CoolThingHappened;
import com.kroger.desp.events.despelmr.test.TestEvent;
import com.kroger.streaming.configuration.DespKafkaProperties;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This class does the following for configuration:
 *    - allows the schema registry and bootstrap servers to be injected from config files
 *    - configures producer
 *    - configuration and creation of event-specific producers singletons
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(DespKafkaProperties.class)
public class KafkaProducerConfiguration {

    @Autowired
    private DespKafkaProperties despKafkaProperties;

    @Value(value = "${kafka.producer.block.ms:1000}")
    private int producerMaxBlockMs;

    private Map<String, Object> getBaseProducerSettings() {
        Map<String, Object> properties =  despKafkaProperties.buildProducerProperties();
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, producerMaxBlockMs);
        Runtime rt = Runtime.getRuntime();

        return properties;
    }


    @Bean
    public KafkaTemplate<String, TestEvent> testEventKafkaTemplate() {
        ProducerFactory<String, TestEvent> pf = new DefaultKafkaProducerFactory<>(getBaseProducerSettings());
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public KafkaTemplate<String, CoolThingHappened> coolThingHappenedKafkaTemplate() {
        ProducerFactory<String, CoolThingHappened> pf  = new DefaultKafkaProducerFactory<>(getBaseProducerSettings());
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public KafkaTemplate<String, BadThingHappened> badThingHappenedKafkaTemplate() {
        ProducerFactory<String, BadThingHappened> pf = new DefaultKafkaProducerFactory<>(getBaseProducerSettings());
        return new KafkaTemplate<>(pf);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RetryingEventPublisher<BadThingHappened> getBadThingHappenedMessageSender() {
        return new RetryingEventPublisher<>();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RetryingEventPublisher<CoolThingHappened> getCoolThingHappenedMessageSender() {
        return new RetryingEventPublisher<>();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RetryingEventPublisher<TestEvent> getTestMessageSender() {
        return new RetryingEventPublisher<>();
    }
}
