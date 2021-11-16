package com.kroger.desp;
import com.kroger.desp.consumer.KafkaConsumer;
import com.kroger.desp.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * The controller class representing the entry point for restful calls
 * The event-specific producers are autowired in, and utilized
 * in specific endpoints/request mappings
 */
@RestController
public class ProducerController {
    Logger logger = LoggerFactory.getLogger(ProducerController.class);

    @Autowired
    KafkaProducer kafkaProducer;

    @Autowired
    KafkaConsumer kafkaConsumer;

    @Autowired
    @Qualifier("threadPoolTaskExecutor")
    Executor threadPoolTaskExecutor;


    /**
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST, path = "/publish")
    public void postEvents(@RequestParam(required = false, defaultValue = "1", value="count") int count) {
        sendBatch(count);
    }
    /**
     * @return the string
     */
    @RequestMapping("/health")
    public String healthCheck() {
        return "I am Alive";
    }

    public void sendBatch(int cnt) {
        List<String> productIds = makeSequence(1, cnt);
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        productIds.forEach(
                productId -> {
                    completableFutures.add(CompletableFuture.runAsync(() -> publishEvent(productId), threadPoolTaskExecutor)
                            .whenComplete((reviewLoadProcessorData, throwable) -> {
                                if (Objects.isNull(throwable)) {
                                    System.out.println("Event Posted successfully");
                                }
                            })
                            .exceptionally(throwable -> {
                                System.out.println("EXCEPTION OCCURRED WHILE POSTING EVENT TO DESP " + throwable.getCause());
                                return null;
                            }));
                }
        );
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        voidCompletableFuture.whenComplete((aVoid, throwable) -> {
            if (Objects.isNull(throwable)) {
                System.out.println("ReviewLoadProcessor.postReviews :: ===============Process Completed===============");
            }
        }).toCompletableFuture().join();

    }

    private void publishEvent(String productId) {
        kafkaProducer.publishTestEvent(1, 1, productId);
    }

    private ArrayList<String> makeSequence(int begin, int end) {
        ArrayList<String> ret = new ArrayList<>(end - begin + 1);
        for (int i=begin; i<=end; i++) {
            ret.add(""+i);
        }
        return ret;
    }

}
