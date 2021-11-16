package com.kroger.desp.producer;

import com.github.javafaker.Faker;
import com.kroger.desp.commons.despelmr.test.EventHeader;
import com.kroger.desp.commons.despelmr.test.ThingThatHappened;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Component
public class ThingThatHappenedCreator {
    private Faker faker = new Faker();
    public ThingThatHappened generateThing(){
        ThingThatHappened.Builder thing = ThingThatHappened.newBuilder();
       thing.setId(UUID.randomUUID().toString());
       thing.setDate(LocalDate.now());
       thing.setName( faker.resolve("hacker.ingverb") + " " + faker.resolve("hacker.noun"));
       return thing.build();
    }
}
