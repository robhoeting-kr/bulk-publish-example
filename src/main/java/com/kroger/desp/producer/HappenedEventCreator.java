package com.kroger.desp.producer;

import com.github.javafaker.Faker;
import com.kroger.desp.commons.despelmr.test.Person;
import com.kroger.desp.events.despelmr.test.BadThingHappened;
import com.kroger.desp.events.despelmr.test.CoolThingHappened;
import com.kroger.desp.events.despelmr.test.TestEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HappenedEventCreator {

    private Faker faker = new Faker();

    @Autowired
    HeaderCreator headerCreator;

    @Autowired
    ThingThatHappenedCreator thingThatHappenedCreator;

    public CoolThingHappened.Builder generateCoolThingHappenedEvent() {
        Person p = Person.newBuilder().setFirstName(faker.name().firstName()).setLastName(faker.name().lastName()).build();
        return
                CoolThingHappened.newBuilder()
                        .setCoolThingThatHappened(thingThatHappenedCreator.generateThing())
                        .setEventHeader(headerCreator.generateHeader("my-good-thing-producer", CoolThingHappened.getClassSchema().getFullName()))
                        .setPerson(p);

    }

    public BadThingHappened.Builder generateBadThingHappenedEvent() {
        Person p = Person.newBuilder().setFirstName(faker.name().firstName()).setLastName(faker.name().lastName()).build();
        return
                BadThingHappened.newBuilder()
                        .setBadThingThatHappened(thingThatHappenedCreator.generateThing())
                        .setEventHeader(headerCreator.generateHeader("my-bad-thing-producer", CoolThingHappened.getClassSchema().getFullName()))
                        .setPerson(p);

    }


}
