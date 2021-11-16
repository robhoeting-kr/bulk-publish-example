package com.kroger.desp.producer;
import com.github.javafaker.Faker;
import com.kroger.desp.events.despelmr.test.TestEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TestEventCreator {

    private Faker faker = new Faker();

    @Autowired
    HeaderCreator headerCreator;

    public  TestEvent.Builder generateTestEvent(int sampleInt){
        return
             TestEvent.newBuilder()
                    .setSampleString(faker.name().fullName())
                    .setSampleStringOptional(faker.address().toString())
                    .setSampleInt(sampleInt)
                    .setSampleDouble(faker.number().randomDouble(4, 3, 4))
                    .setSampleFloat((float) faker.number().randomDouble(4, 3, 4))
                    .setSampleLong(faker.number().randomNumber())
                    .setSampleList(getRandomList())
                    .setSampleMap(getRandomMap())
                     .setSampleBoolean(true)
                    .setEventHeader(headerCreator.generateHeader("my-event-producer", TestEvent.getClassSchema().getFullName()));
    }

    public Map<String, String> getRandomMap() {
        int len = faker.number().numberBetween(2, 8);
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < len; i++) {
            map.put(faker.name().firstName()+i, faker.name().lastName());
        }
        return map;
    }

    public List<String> getRandomList() {
        int len = faker.number().numberBetween(2, 8);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            list.add(faker.name().firstName());
        }
        return list;
    }

}
