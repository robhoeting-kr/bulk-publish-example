package com.kroger.desp.consumer;

import com.kroger.desp.events.despelmr.test.TestEvent;
import com.kroger.streaming.configuration.DespKafkaProperties;
import com.kroger.streaming.interceptors.DespKafkaConsumerHeaderFilterDeserialization;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@Configuration
@EnableKafka
@EnableConfigurationProperties(DespKafkaProperties.class)
public class KafkaConsumer {
    Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private DespKafkaProperties despKafkaProperties;


    @Bean("consumerFactory")
    public ConsumerFactory<String, SpecificRecord> consumerFactory() {
        Map<String, Object> props = despKafkaProperties.buildConsumerProperties();
        props.put(ErrorHandlingDeserializer2.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer.class.getName());
        props.put(ErrorHandlingDeserializer2.VALUE_FUNCTION, FailedConversionProvider.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DespKafkaConsumerHeaderFilterDeserialization.class);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, DespKafkaConsumerHeaderFilterDeserialization.class);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean("consumer")
    public org.apache.kafka.clients.consumer.Consumer<String, SpecificRecord> consumer(ConsumerFactory<String, SpecificRecord> consumerFactory){
        return consumerFactory.createConsumer();
    }



    @Bean("kafkaListenerContainerFactory")
    @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, SpecificRecord> listenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SpecificRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setErrorHandler(new SeekToCurrentErrorHandler());
        return factory;
    }



    int cnt = 0;
    @KafkaListener(topics = "#{'${target-topic}'.split(',')}", containerFactory = "kafkaListenerContainerFactory")
    public void topicListener(ConsumerRecord<String, SpecificRecord> record,
                              Acknowledgment acknowledgment,
                              @Headers MessageHeaders messageHeaders) {

         if (record.value() != null) {
             TestEvent c = (TestEvent) record.value();
             cnt ++;
             logger.info("HANDLING >>> Normal event - " + c.getEventHeader().getId() + " Total Events Consumed = " + cnt);
         }
         else {
             logger.info("**********************************skipping over event: {}-{}", record.partition(), record.offset());
         }

        acknowledgment.acknowledge();
    }





}
