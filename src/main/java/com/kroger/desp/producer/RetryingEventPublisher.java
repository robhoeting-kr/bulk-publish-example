package com.kroger.desp.producer;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.StopWatch;
import org.springframework.util.concurrent.ListenableFutureCallback;

/***
 *   This class manages the publishing of events and the handling of the returned
 *   futures.  It performs retries on events that fail.  The number of retries
 *   can be configured in the application.yml using the
 *   property: spring.kafka.producer.properties.max.send.attempts
 *
 *   It is intended to be created as a prototype bean so dependencies
 *   and configurations can be injected into it.  This version, inside the quickstart
 *   project is a reference implementation, but your specific situation may require
 *   other customizations.
 */
public class RetryingEventPublisher<T extends SpecificRecord> implements ListenableFutureCallback<SendResult<String, T>> {
    private final Logger logger = LoggerFactory.getLogger(RetryingEventPublisher.class);

    @Value(value = "${spring.kafka.producer.properties.max.send.attempts:10}")
    private int maxSendAttempts;

    private final StopWatch stopWatch = new StopWatch();
    int attemptNumber = 0;

    private final String sendMessage = "Sending Message, attempt #%s of %s";
    private final String successMsg = "Success on try #%s in %sms; partition: %s offset: %s";
    private final String errorMsg = "******* Failed send attempt: %s - %s; elapsed time: %s";
    private final String maxSendMessage = "Message delivery has been attempted: %s times, no more retries";

    @Autowired
    private KafkaTemplate<String, T> testEventKafkaTemplate;
    private T event;
    private String key;
    private String topic;

    public void send(String targetTopic, String key, T event) {
        this.event = event;
        this.key = key;
        this.topic = targetTopic;
        logger.info("event: " + event.toString());
        this.internalSend();
    }

    private byte[] getValueType() {
        return event.getSchema().getName().getBytes();
    }

    private void internalSend() {
        this.attemptNumber++;
        if (this.attemptNumber > this.maxSendAttempts) {
            logger.info(String.format(maxSendMessage, this.maxSendAttempts));
            return;
        }
        this.stopWatch.start();
        logger.info(String.format(sendMessage, this.attemptNumber, this.maxSendAttempts));
        ProducerRecord<String, T> record =
                new ProducerRecord<>(topic, key, event);
        record.headers().add(new RecordHeader("eventType", getValueType()));
        this.testEventKafkaTemplate.send(record).addCallback(this);
    }

    @Override
    public void onSuccess(SendResult<String, T> result) {
        logger.info("event of type " + this.event.getSchema().getName() + " published ");
        this.stopWatch.stop();
        RecordMetadata md = result.getRecordMetadata();
        String msg = String.format(successMsg, attemptNumber, stopWatch.getLastTaskTimeMillis(), md.partition(), md.offset());
        if (this.attemptNumber > 1) {
            msg += "; total elapsed time: " + this.stopWatch.getTotalTimeMillis() + "ms";
        }
        logger.info(msg);
    }

    @Override
    public void onFailure(Throwable ex) {
        this.stopWatch.stop();
        logger.error(String.format(errorMsg, ex.getCause(), ex.getCause().getMessage(), stopWatch.getLastTaskTimeMillis()));
        if (ex.getCause() instanceof RetriableException) {
            logger.info("RETRYING sending of failed message (max attempts: " + maxSendAttempts +")");
            this.internalSend();
        }
        else {
            logger.info("The sending of message can not be retried.  The failure is Final");
        }
    }
}
