package com.kroger.desp.consumer;

import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 *  The primary challenge with deal with serialization issues is due to the fact that it occurs deep
 *  in the bowls of the kafka api.  Spring solves this by taking over the serialization with a
 *  special class called ErrorHandlingDeserializer2
 *  https://docs.spring.io/spring-kafka/docs/2.4.2.RELEASE/reference/html/#error-handling-deserializer
 *
 *  This consumer has been configured to return an instance of this class upon a failed deserialization.
 *  It's job in life is to hold an instance of FailedDeserializationInfo (provided by spring kafka)
 *  and attempt to determine the root cause of the exception based on what it sees in the
 *  deserializationException
 */


public class BadSpecificRecord implements SpecificRecord {

    private final FailedDeserializationInfo failedDeserializationInfo;
    private DeserializationException deserializationException;

    public BadSpecificRecord(FailedDeserializationInfo failedDeserializationInfo) {

        this.failedDeserializationInfo = failedDeserializationInfo;
        for ( Header h : this.failedDeserializationInfo.getHeaders()) {
            if (h.key().equals("springDeserializerExceptionValue")) {
                this.deserializationException = (DeserializationException) toObject(h.value());
            }
        }
    }

    public enum ProbableDeserializationErrorType {
        NON_AVRO,
        WRONG_SCHEMA_REGISTRY,
        MISSING_JAR,
        WRONG_JAR_VERSION,
        UNKNOWN
    }

    public class ProbableDeserializationError {
        public ProbableDeserializationError(ProbableDeserializationErrorType errorType, String errorMessage) {
            ErrorType = errorType;
            ErrorMessage = errorMessage;
        }

        public ProbableDeserializationErrorType ErrorType;
        public String ErrorMessage;
    }

    @Override
    public void put(int i, Object o) {

    }

    @Override
    public Object get(int i) {
        return null;
    }

    @Override
    public Schema getSchema() {
        return null;
    }

    public FailedDeserializationInfo getFailedDeserializationInfo() {
        return failedDeserializationInfo;
    }

    @Override
    public String toString() {
        return failedDeserializationInfo.toString();
    }

    public String getDataAsString() {
        return new String(this.failedDeserializationInfo.getData());
    }

    /**
     *  Helper method to convert byte array to object.
     *
     */
    private static Object toObject(byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace(); // ClassNotFoundException
        }
        return obj;
    }

    public DeserializationException getDeserializationException() {
        return deserializationException;
    }

    public String getMostSpecificError() {
        return this.deserializationException.getMostSpecificCause().toString();
    }

    /***
     *   This method makes a "best guess" on the nature of the deserialization error.  Odds are a serialization
     *   exception falls in to one of the following scenarios:
     *       -   The message is JSON formatted, a simple string, or otherwise non-Avro serialized
     *       -   The consumer is referencing the wrong schema registry for the cluster
     *       -   The consumer is not referencing the JAR defining the correct event type
     *       -   The consumer is not referencing the correct VERSION of the JAR defining the event type
     *
     */
    public ProbableDeserializationError getProbableError() {
        if (this.deserializationException != null) {
            String rootCause = this.deserializationException.getRootCause().toString();
            if (rootCause.contains("Unknown magic byte!")) {
                if (hasNonAscii(this.failedDeserializationInfo.getData())) {
                    return new ProbableDeserializationError(ProbableDeserializationErrorType.WRONG_SCHEMA_REGISTRY,
                            "The wrong schema registry is likely being referenced");
                }
                return new ProbableDeserializationError(ProbableDeserializationErrorType.NON_AVRO,
                        "The record is not serialized in Avro, and cannot be decoded");
            }
            if (rootCause.contains("Could not find class")) {
                return new ProbableDeserializationError(ProbableDeserializationErrorType.MISSING_JAR,
                  "It is likely a JAR dependency is missing, which contains the your specific record");
            }
            if (rootCause.contains("org.apache.avro.AvroTypeException")) {
                return new ProbableDeserializationError(ProbableDeserializationErrorType.WRONG_JAR_VERSION,
                  "Avro Conversion issue.  The specific record JAR dependency was found, but is likely not compatible with the message.");
            }
            return new ProbableDeserializationError(ProbableDeserializationErrorType.UNKNOWN, rootCause);
        }
        return new ProbableDeserializationError(ProbableDeserializationErrorType.UNKNOWN, "Unknown");
    }

    /**
     * This function helps us determine whether or not this is an avro formatted record or not
     * Not perfect, by any means, but if we detect non ascii chars in it is probably
     * Avro encoded.
     */
    public boolean hasNonAscii(byte[] valueToCheck) {
        CharsetDecoder decoder = Charset.forName("US-ASCII").newDecoder();
        try {
            CharBuffer buffer = decoder.decode(ByteBuffer.wrap(valueToCheck));
            return false;
        } catch (CharacterCodingException e) {
            System.err.println("The information contains a non ASCII character(s).");
            return true;
        }
    }


}
