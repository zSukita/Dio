package dio.budgeting.application;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    public BigDecimal getTotalIncomeBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return repository.sumByTypeBetween(Transaction.TransactionType.INCOME, startDate, endDate);
    }

    public BigDecimal getTotalExpenseBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return repository.sumByTypeBetween(Transaction.TransactionType.EXPENSE, startDate, endDate);
    }

    public Map<String, BigDecimal> getExpensesByCategoryBetween(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> result = repository.sumExpensesByCategoryBetween(startDate, endDate);
        Map<String, BigDecimal> map = new java.util.LinkedHashMap<>();
        for (Object[] row : result) {
            map.put((String) row[0], (BigDecimal) row[1]);
        }
        return map;
    }

    public List<Map<String, Object>> getMonthlySummary(int monthsBack) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusMonths(monthsBack);
        LocalDateTime endDate = now.plusDays(1);
        List<Transaction> transactions = repository.findByDateRange(startDate, endDate);
        Map<String, BigDecimal[]> monthly = new java.util.LinkedHashMap<>();
        for (Transaction t : transactions) {
            String key = t.getCreatedAt().getYear() + "-" + t.getCreatedAt().getMonthValue();
            monthly.putIfAbsent(key, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal[] amounts = monthly.get(key);
            if (t.getType() == Transaction.TransactionType.INCOME) {
                amounts[0] = amounts[0].add(t.getAmount());
            } else {
                amounts[1] = amounts[1].add(t.getAmount());
            }
        }
        List<Map<String, Object>> summary = new java.util.ArrayList<>();
        List<String> keys = new java.util.ArrayList<>(monthly.keySet());
        java.util.Collections.sort(keys, java.util.Collections.reverseOrder());
        for (String key : keys) {
            String[] parts = key.split("-");
            BigDecimal[] amounts = monthly.get(key);
            Map<String, Object> month = new java.util.LinkedHashMap<>();
            month.put("year", Integer.valueOf(parts[0]));
            month.put("month", Integer.valueOf(parts[1]));
            month.put("totalIncome", amounts[0]);
            month.put("totalExpense", amounts[1]);
            month.put("balance", amounts[0].subtract(amounts[1]));
            summary.add(month);
        }
        return summary;
    }

    public List<Transaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByDateRange(startDate, endDate);
    }
}