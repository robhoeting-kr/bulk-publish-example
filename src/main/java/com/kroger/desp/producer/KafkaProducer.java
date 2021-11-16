package com.kroger.desp.producer;
import com.kroger.desp.events.despelmr.test.BadThingHappened;
import com.kroger.desp.events.despelmr.test.CoolThingHappened;
import com.kroger.desp.events.despelmr.test.TestEvent;
import org.apache.kafka.clients.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

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

    @Autowired
    HappenedEventCreator happenedEventCreator;

    @Value( "${target-topic}" )
    private String targetTopic;

    public void publishTestEvent(int i, int failCount, String id){
        logger.info("Sending EVENT #" + i + " To topic: " + targetTopic);
        TestEvent event = testEventCreator.generateTestEvent(failCount).build();
        event.getEventHeader().setId(id);
        final RetryingEventPublisher<TestEvent> publisher = testEventPublisherProvider.getObject();
        publisher.send(targetTopic, event.getEventHeader().getId(), event);
    }

    @Autowired
    KafkaTemplate<String, CoolThingHappened> coolThingHappenedKafkaTemplate;

    public void publishCoolThingHappened(int i){
        logger.info("Sending Cool Thing EVENT #" + i + " To topic: " + targetTopic);
        CoolThingHappened event = happenedEventCreator.generateCoolThingHappenedEvent().build();
        final RetryingEventPublisher<CoolThingHappened> publisher = coolThingPublisherProvider.getObject();
        publisher.send(targetTopic, event.getEventHeader().getId(), event);
    }

    @Autowired
    KafkaTemplate<String, BadThingHappened> badThingHappenedKafkaTemplate;

    public void publishBadThingHappened(int i){
        logger.info("Sending Bad Thing EVENT #" + i + " To topic: " + targetTopic);
        BadThingHappened event = happenedEventCreator.generateBadThingHappenedEvent().build();
        final RetryingEventPublisher<BadThingHappened> publisher = badThingPublisherProvider.getObject();
        publisher.send(targetTopic, event.getEventHeader().getId(), event);
    }
}
