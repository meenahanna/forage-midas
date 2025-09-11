package com.jpmc.midascore.component;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
@Service

public class TransactionService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;
    private final String incentiveApiUrl;

    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository, RestTemplate restTemplate, @Value("${incentives.api-url:http://localhost:8080/incentive}") String incentiveApiUrl) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
        this.incentiveApiUrl = incentiveApiUrl;
    }

    @Transactional
    public void processTransaction(Transaction tx) {
        // Validate sender
        UserRecord sender = userRepository.findById(tx.getSenderId());
        if (sender == null) return;

        // Validate recipient
        UserRecord recipient = userRepository.findById(tx.getRecipientId());
        if (recipient == null) return;

        // Validate funds
        float amount = tx.getAmount();
        if (sender.getBalance() < amount) return;

        // Call Incentive API (safe fallback to 0 if unavailable)
        float incentiveAmt = 0f;
        try {
            Incentive resp = restTemplate.postForObject(incentiveApiUrl, tx, Incentive.class);
            if (resp != null) {
                incentiveAmt = Math.max(0f, resp.getAmount());
            }   
        } catch (Exception ignored) {
            // If the service is down or unreachable, we just proceed with 0 incentive.
        }

        // Adjust balances first (still inside the same transaction)
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount + incentiveAmt);

        // Persist transaction record
        TransactionRecord record = new TransactionRecord(sender, recipient, amount, incentiveAmt);
        transactionRepository.save(record);

        // Persist user updates
        userRepository.save(sender);
        userRepository.save(recipient);
    }  
}
