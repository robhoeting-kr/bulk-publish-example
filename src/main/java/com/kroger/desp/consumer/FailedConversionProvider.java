package com.kroger.desp.consumer;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;

import java.util.function.Function;

public class FailedConversionProvider implements Function<FailedDeserializationInfo, SpecificRecord> {

    Logger logger = LoggerFactory.getLogger(FailedConversionProvider.class);

    @Override
    public SpecificRecord apply(FailedDeserializationInfo info) {
        logger.error(info.getException().getMessage(), info.getException());
        return new BadSpecificRecord(info);
    }
}
