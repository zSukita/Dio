package dio.budgeting.application.ai;

import dio.budgeting.application.TransactionService;
import dio.budgeting.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionToolsTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionTools transactionTools;

    @Test
    void shouldCreateIncome() {
        Transaction expected = Transaction.builder().id(UUID.randomUUID()).build();
        when(transactionService.createIncome(any(), anyString(), anyString())).thenReturn(expected);

        Transaction result = transactionTools.createIncome(new BigDecimal("5000.00"), "Salário", "Salário");

        assertSame(expected, result);
        verify(transactionService).createIncome(new BigDecimal("5000.00"), "Salário", "Salário");
    }

    @Test
    void shouldCreateExpense() {
        Transaction expected = Transaction.builder().id(UUID.randomUUID()).build();
        when(transactionService.createExpense(any(), anyString(), anyString())).thenReturn(expected);

        Transaction result = transactionTools.createExpense(new BigDecimal("150.00"), "Supermercado", "Alimentação");

        assertSame(expected, result);
        verify(transactionService).createExpense(new BigDecimal("150.00"), "Supermercado", "Alimentação");
    }

    @Test
    void shouldGetBalance() {
        when(transactionService.getBalance()).thenReturn(new BigDecimal("3500.00"));

        Map<String, Object> result = transactionTools.getBalance();

        assertEquals(new BigDecimal("3500.00"), result.get("balance"));
    }

    @Test
    void shouldGetTransactionsByType() {
        List<Transaction> expected = List.of(Transaction.builder().id(UUID.randomUUID()).build());
        when(transactionService.findByType(Transaction.TransactionType.INCOME)).thenReturn(expected);

        List<Transaction> result = transactionTools.getTransactionsByType("INCOME");

        assertSame(expected, result);
    }

    @Test
    void shouldGetTransactionsByCategory() {
        List<Transaction> expected = List.of(Transaction.builder().id(UUID.randomUUID()).build());
        when(transactionService.findByCategory("Alimentação")).thenReturn(expected);

        List<Transaction> result = transactionTools.getTransactionsByCategory("Alimentação");

        assertSame(expected, result);
    }

    @Test
    void shouldGetAllTransactions() {
        List<Transaction> expected = List.of(Transaction.builder().id(UUID.randomUUID()).build());
        when(transactionService.findAll()).thenReturn(expected);

        List<Transaction> result = transactionTools.getAllTransactions();

        assertSame(expected, result);
    }

    @Test
    void shouldGetTotalIncome() {
        when(transactionService.getTotalIncomeBetween(any(), any())).thenReturn(new BigDecimal("7000.00"));

        BigDecimal result = transactionTools.getTotalIncome("2024-01-01T00:00:00", "2024-12-31T23:59:59");

        assertEquals(new BigDecimal("7000.00"), result);
        verify(transactionService).getTotalIncomeBetween(
                LocalDateTime.parse("2024-01-01T00:00:00"),
                LocalDateTime.parse("2024-12-31T23:59:59"));
    }

    @Test
    void shouldGetTotalExpense() {
        when(transactionService.getTotalExpenseBetween(any(), any())).thenReturn(new BigDecimal("1800.00"));

        BigDecimal result = transactionTools.getTotalExpense("2024-01-01T00:00:00", "2024-12-31T23:59:59");

        assertEquals(new BigDecimal("1800.00"), result);
    }

    @Test
    void shouldGetExpensesByCategory() {
        Map<String, BigDecimal> expected = Map.of("Alimentação", new BigDecimal("400.00"));
        when(transactionService.getExpensesByCategoryBetween(any(), any())).thenReturn(expected);

        Map<String, BigDecimal> result = transactionTools.getExpensesByCategory("2024-01-01T00:00:00", "2024-12-31T23:59:59");

        assertSame(expected, result);
    }

    @Test
    void shouldGetMonthlySummary() {
        List<Map<String, Object>> expected = List.of(Map.of("month", 1, "totalIncome", BigDecimal.valueOf(5000)));
        when(transactionService.getMonthlySummary(3)).thenReturn(expected);

        List<Map<String, Object>> result = transactionTools.getMonthlySummary(3);

        assertSame(expected, result);
    }

    @Test
    void shouldDeleteTransaction() {
        UUID id = UUID.randomUUID();
        doNothing().when(transactionService).deleteTransaction(id);

        transactionTools.deleteTransaction(id.toString());

        verify(transactionService).deleteTransaction(id);
    }

    @Test
    void shouldGetBalanceSince() {
        when(transactionService.getBalanceSince(any())).thenReturn(new BigDecimal("3500.00"));

        Map<String, Object> result = transactionTools.getBalanceSince("2024-01-01T00:00:00");

        assertEquals(new BigDecimal("3500.00"), result.get("balance"));
        verify(transactionService).getBalanceSince(LocalDateTime.parse("2024-01-01T00:00:00"));
    }

    @Test
    void shouldGetLargestTransactions() {
        List<Transaction> expected = List.of(Transaction.builder().id(UUID.randomUUID()).build());
        when(transactionService.getLargestTransactions()).thenReturn(expected);

        List<Transaction> result = transactionTools.getLargestTransactions();

        assertSame(expected, result);
    }

    @Test
    void shouldSearchTransactions() {
        List<Transaction> expected = List.of(Transaction.builder().id(UUID.randomUUID()).build());
        when(transactionService.searchByDescription("salário")).thenReturn(expected);

        List<Transaction> result = transactionTools.searchTransactions("salário");

        assertSame(expected, result);
    }

    @Test
    void shouldGetIncomeByCategory() {
        Map<String, BigDecimal> expected = Map.of("Salário", new BigDecimal("5000.00"));
        when(transactionService.getIncomeByCategoryBetween(any(), any())).thenReturn(expected);

        Map<String, BigDecimal> result = transactionTools.getIncomeByCategory("2024-01-01T00:00:00", "2024-12-31T23:59:59");

        assertSame(expected, result);
    }

    @Test
    void shouldGetTransactionCounts() {
        when(transactionService.countIncomes()).thenReturn(3L);
        when(transactionService.countExpenses()).thenReturn(5L);

        Map<String, Object> result = transactionTools.getTransactionCounts();

        assertEquals(3L, result.get("totalIncomes"));
        assertEquals(5L, result.get("totalExpenses"));
    }

    @Test
    void shouldGetDailySummary() {
        List<Map<String, Object>> expected = List.of(Map.of("date", "2024-01-01"));
        when(transactionService.getDailySummary(7)).thenReturn(expected);

        List<Map<String, Object>> result = transactionTools.getDailySummary(7);

        assertSame(expected, result);
    }
}
