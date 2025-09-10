package com.jpmc.midascore.component;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service

public class TransactionService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void processTransaction(Transaction tx) {
        UserRecord sender = userRepository.findById(tx.getSenderId());
        if (sender == null) return;

        UserRecord recipient = userRepository.findById(tx.getRecipientId());
        if (recipient == null) return;

        float amount = tx.getAmount();
        if (sender.getBalance() < amount) return;

        // Adjust balances first (still inside the same transaction)
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        // Persist transaction record
        TransactionRecord record = new TransactionRecord(sender, recipient, amount);
        transactionRepository.save(record);

        // Persist user updates
        userRepository.save(sender);
        userRepository.save(recipient);
    }  
}
