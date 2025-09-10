package com.jpmc.midascore.messaging;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.component.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
@Profile("kafka")

public class TransactionListener {
    private static final Logger log = LoggerFactory.getLogger(TransactionListener.class);
    private final TransactionService transactionService;

    public TransactionListener(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(
        topics = "${general.kafka-topic}",
        groupId = "midas-core",
        containerFactory = "transactionKafkaListenerContainerFactory"
    )
    public void onMessage(Transaction transaction) {
        // Set a breakpoint here during TaskTwoTests
        log.info("Recevied trasaction: {}", transaction);
        transactionService.processTransaction(transaction);
    }
    
}
