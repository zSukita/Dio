package dio.budgeting.application;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository repository;

    @Transactional
    public Transaction createTransaction(BigDecimal amount, Transaction.TransactionType type, String description, String category) {
        Transaction transaction = Transaction.builder()
                .amount(amount)
                .type(type)
                .description(description)
                .category(category)
                .build();
        return repository.save(transaction);
    }

    @Transactional
    public Transaction createIncome(BigDecimal amount, String description, String category) {
        return createTransaction(amount, Transaction.TransactionType.INCOME, description, category);
    }

    @Transactional
    public Transaction createExpense(BigDecimal amount, String description, String category) {
        return createTransaction(amount, Transaction.TransactionType.EXPENSE, description, category);
    }

    public Optional<Transaction> findById(UUID id) {
        return repository.findById(id);
    }

    public List<Transaction> findAll() {
        return repository.findAll();
    }

    public List<Transaction> findByType(Transaction.TransactionType type) {
        return repository.findByTypeOrderByCreatedAtDesc(type);
    }

    public List<Transaction> findByCategory(String category) {
        return repository.findByCategoryOrderByCreatedAtDesc(category);
    }

    public BigDecimal getBalance() {
        return repository.calculateBalance().orElse(BigDecimal.ZERO);
    }

    public BigDecimal getBalanceSince(LocalDateTime startDate) {
        return repository.calculateBalanceSince(startDate).orElse(BigDecimal.ZERO);
    }

    public List<Transaction> findSince(LocalDateTime startDate) {
        return repository.findByCreatedAtAfter(startDate);
    }

    public List<Transaction> findByCategorySince(String category, LocalDateTime startDate) {
        return repository.findByCategoryAndCreatedAtAfter(category, startDate);
    }
}