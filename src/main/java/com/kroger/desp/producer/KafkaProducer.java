package com.kroger.desp.producer;
import com.kroger.desp.events.despelmr.test.BadThingHappened;
import com.kroger.desp.events.despelmr.test.CoolThingHappened;
import com.kroger.desp.events.despelmr.test.TestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    ObjectProvider<RetryingEventPublisher<TestEvent>> testEventPublisherProvider;
    @Autowired
    ObjectProvider<RetryingEventPublisher<CoolThingHappened>> coolThingPublisherProvider;
    @Autowired
    ObjectProvider<RetryingEventPublisher<BadThingHappened>> badThingPublisherProvider;

    /**
     * The Item updated event producer.
     */
    @Autowired
    KafkaTemplate<String, TestEvent> testEventKafkaTemplate;

    @Autowired
    TestEventCreator testEventCreator;


    @Value( "${target-topic}" )
    private String targetTopic;

    public void publishTestEvent(int i, int failCount, String id){
        logger.info("Sending EVENT #" + i + " To topic: " + targetTopic);
        TestEvent event = testEventCreator.generateTestEvent(failCount).build();
        event.getEventHeader().setId(id);
        final RetryingEventPublisher<TestEvent> publisher = testEventPublisherProvider.getObject();
        publisher.send(targetTopic, event.getEventHeader().getId(), event);
    }

}
