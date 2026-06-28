package dio.budgeting.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByTypeOrderByCreatedAtDesc(Transaction.TransactionType type);

    List<Transaction> findByCategoryOrderByCreatedAtDesc(String category);

    @Query("SELECT SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END) FROM Transaction t")
    Optional<BigDecimal> calculateBalance();

    @Query("SELECT SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END) FROM Transaction t WHERE t.createdAt >= :startDate")
    Optional<BigDecimal> calculateBalanceSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt >= :startDate ORDER BY t.createdAt DESC")
    List<Transaction> findByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT t FROM Transaction t WHERE t.category = :category AND t.createdAt >= :startDate ORDER BY t.createdAt DESC")
    List<Transaction> findByCategoryAndCreatedAtAfter(@Param("category") String category, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.createdAt >= :startDate AND t.createdAt < :endDate")
    BigDecimal sumByTypeBetween(@Param("type") Transaction.TransactionType type, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t.category, SUM(t.amount) as total FROM Transaction t WHERE t.type = 'EXPENSE' AND t.createdAt >= :startDate AND t.createdAt < :endDate GROUP BY t.category ORDER BY total DESC")
    List<Object[]> sumExpensesByCategoryBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt >= :startDate AND t.createdAt < :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Transaction> findTop5ByOrderByAmountDesc();

    List<Transaction> findByDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(String description);

    @Query("SELECT t.category, SUM(t.amount) as total FROM Transaction t WHERE t.type = 'INCOME' AND t.createdAt >= :startDate AND t.createdAt < :endDate GROUP BY t.category ORDER BY total DESC")
    List<Object[]> sumIncomeByCategoryBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long countByType(Transaction.TransactionType type);
}