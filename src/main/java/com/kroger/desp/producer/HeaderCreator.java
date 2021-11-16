package com.kroger.desp.producer;
import com.kroger.desp.commons.despelmr.test.EventHeader;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.UUID;

@Component
public class HeaderCreator {
    public EventHeader generateHeader(String source, String type){
       EventHeader.Builder header = EventHeader.newBuilder();
       header.setId(UUID.randomUUID().toString());
       header.setSource(source);
       header.setTime(new Date().getTime());
       header.setType(type);
       return header.build();
    }
}
