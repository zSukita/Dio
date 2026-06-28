package dio.budgeting;

import dio.budgeting.application.TransactionService;
import dio.budgeting.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BudgetingApplicationTests {

    @Autowired
    private TransactionService transactionService;

    @Test
    void contextLoads() {
        assertNotNull(transactionService);
    }

    @Test
    void shouldCreateIncome() {
        Transaction income = transactionService.createIncome(
                new BigDecimal("5000.00"),
                "Salário mensal",
                "Salário"
        );

        assertNotNull(income.getId());
        assertEquals(new BigDecimal("5000.00"), income.getAmount());
        assertEquals(Transaction.TransactionType.INCOME, income.getType());
        assertEquals("Salário mensal", income.getDescription());
        assertEquals("Salário", income.getCategory());
    }

    @Test
    void shouldCreateExpense() {
        Transaction expense = transactionService.createExpense(
                new BigDecimal("150.00"),
                "Supermercado",
                "Alimentação"
        );

        assertNotNull(expense.getId());
        assertEquals(new BigDecimal("150.00"), expense.getAmount());
        assertEquals(Transaction.TransactionType.EXPENSE, expense.getType());
        assertEquals("Supermercado", expense.getDescription());
        assertEquals("Alimentação", expense.getCategory());
    }

    @Test
    void shouldCalculateBalance() {
        transactionService.createIncome(new BigDecimal("1000.00"), "Salário", "Salário");
        transactionService.createExpense(new BigDecimal("300.00"), "Aluguel", "Moradia");
        transactionService.createExpense(new BigDecimal("150.00"), "Supermercado", "Alimentação");

        BigDecimal balance = transactionService.getBalance();
        assertEquals(new BigDecimal("550.00"), balance);
    }

    @Test
    void shouldFindByType() {
        transactionService.createIncome(new BigDecimal("1000.00"), "Salário", "Salário");
        transactionService.createIncome(new BigDecimal("500.00"), "Freelance", "Trabalho");
        transactionService.createExpense(new BigDecimal("300.00"), "Aluguel", "Moradia");

        List<Transaction> incomes = transactionService.findByType(Transaction.TransactionType.INCOME);
        List<Transaction> expenses = transactionService.findByType(Transaction.TransactionType.EXPENSE);

        assertEquals(2, incomes.size());
        assertEquals(1, expenses.size());
    }

    @Test
    void shouldFindByCategory() {
        transactionService.createExpense(new BigDecimal("150.00"), "Supermercado", "Alimentação");
        transactionService.createExpense(new BigDecimal("80.00"), "Restaurante", "Alimentação");
        transactionService.createExpense(new BigDecimal("50.00"), "Ônibus", "Transporte");

        List<Transaction> alimentacao = transactionService.findByCategory("Alimentação");
        List<Transaction> transporte = transactionService.findByCategory("Transporte");

        assertEquals(2, alimentacao.size());
        assertEquals(1, transporte.size());
    }

    @Test
    void shouldGetTotalIncomeBetweenDates() {
        transactionService.createIncome(new BigDecimal("5000.00"), "Salário", "Salário");
        transactionService.createIncome(new BigDecimal("2000.00"), "Freelance", "Trabalho");
        transactionService.createExpense(new BigDecimal("1500.00"), "Aluguel", "Moradia");

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        BigDecimal totalIncome = transactionService.getTotalIncomeBetween(start, end);
        assertEquals(new BigDecimal("7000.00"), totalIncome);
    }

    @Test
    void shouldGetTotalExpenseBetweenDates() {
        transactionService.createIncome(new BigDecimal("5000.00"), "Salário", "Salário");
        transactionService.createExpense(new BigDecimal("1500.00"), "Aluguel", "Moradia");
        transactionService.createExpense(new BigDecimal("300.00"), "Supermercado", "Alimentação");

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        BigDecimal totalExpense = transactionService.getTotalExpenseBetween(start, end);
        assertEquals(new BigDecimal("1800.00"), totalExpense);
    }

    @Test
    void shouldGetExpensesByCategory() {
        transactionService.createExpense(new BigDecimal("1500.00"), "Aluguel", "Moradia");
        transactionService.createExpense(new BigDecimal("300.00"), "Supermercado", "Alimentação");
        transactionService.createExpense(new BigDecimal("100.00"), "Restaurante", "Alimentação");

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Map<String, BigDecimal> byCategory = transactionService.getExpensesByCategoryBetween(start, end);
        assertEquals(new BigDecimal("400.00"), byCategory.get("Alimentação"));
        assertEquals(new BigDecimal("1500.00"), byCategory.get("Moradia"));
    }

    @Test
    void shouldGetMonthlySummary() {
        transactionService.createIncome(new BigDecimal("5000.00"), "Salário", "Salário");
        transactionService.createExpense(new BigDecimal("1500.00"), "Aluguel", "Moradia");

        List<Map<String, Object>> summary = transactionService.getMonthlySummary(3);
        assertFalse(summary.isEmpty());
        assertEquals(new BigDecimal("5000.00"), summary.get(0).get("totalIncome"));
        assertEquals(new BigDecimal("1500.00"), summary.get(0).get("totalExpense"));
        assertEquals(new BigDecimal("3500.00"), summary.get(0).get("balance"));
    }

    @Test
    void shouldFindByDateRange() {
        transactionService.createIncome(new BigDecimal("5000.00"), "Salário", "Salário");
        transactionService.createExpense(new BigDecimal("1500.00"), "Aluguel", "Moradia");

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        List<Transaction> transactions = transactionService.findByDateRange(start, end);
        assertEquals(2, transactions.size());
    }

    @Test
    void shouldDeleteTransaction() {
        Transaction income = transactionService.createIncome(new BigDecimal("1000.00"), "Salário", "Salário");
        assertEquals(1, transactionService.findAll().size());

        transactionService.deleteTransaction(income.getId());
        assertEquals(0, transactionService.findAll().size());
    }

    @Test
    void shouldGetBalanceSince() {
        transactionService.createIncome(new BigDecimal("5000.00"), "Salário", "Salário");
        transactionService.createExpense(new BigDecimal("1500.00"), "Aluguel", "Moradia");

        LocalDateTime since = LocalDateTime.now().minusDays(1);
        BigDecimal balance = transactionService.getBalanceSince(since);
        assertEquals(new BigDecimal("3500.00"), balance);
    }

    @Test
    void shouldGetLargestTransactions() {
        transactionService.createExpense(new BigDecimal("50.00"), "Ônibus", "Transporte");
        transactionService.createExpense(new BigDecimal("1500.00"), "Aluguel", "Moradia");
        transactionService.createExpense(new BigDecimal("300.00"), "Supermercado", "Alimentação");
        transactionService.createIncome(new BigDecimal("5000.00"), "Salário", "Salário");
        transactionService.createExpense(new BigDecimal("200.00"), "Restaurante", "Alimentação");
        transactionService.createExpense(new BigDecimal("100.00"), "Uber", "Transporte");

        List<Transaction> largest = transactionService.getLargestTransactions();
        assertEquals(5, largest.size());
        assertEquals(new BigDecimal("5000.00"), largest.get(0).getAmount());
    }

    @Test
    void shouldSearchByDescription() {
        transactionService.createIncome(new BigDecimal("5000.00"), "Salário mensal", "Salário");
        transactionService.createExpense(new BigDecimal("1500.00"), "Aluguel", "Moradia");
        transactionService.createExpense(new BigDecimal("300.00"), "Supermercado", "Alimentação");

        List<Transaction> results = transactionService.searchByDescription("salário");
        assertEquals(1, results.size());
        assertEquals("Salário mensal", results.get(0).getDescription());
    }

    @Test
    void shouldGetIncomeByCategory() {
        transactionService.createIncome(new BigDecimal("5000.00"), "Salário", "Salário");
        transactionService.createIncome(new BigDecimal("2000.00"), "Freelance", "Trabalho");
        transactionService.createExpense(new BigDecimal("1500.00"), "Aluguel", "Moradia");

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Map<String, BigDecimal> byCategory = transactionService.getIncomeByCategoryBetween(start, end);
        assertEquals(new BigDecimal("5000.00"), byCategory.get("Salário"));
        assertEquals(new BigDecimal("2000.00"), byCategory.get("Trabalho"));
    }

    @Test
    void shouldGetTransactionCounts() {
        transactionService.createIncome(new BigDecimal("5000.00"), "Salário", "Salário");
        transactionService.createIncome(new BigDecimal("2000.00"), "Freelance", "Trabalho");
        transactionService.createExpense(new BigDecimal("1500.00"), "Aluguel", "Moradia");

        assertEquals(2, transactionService.countIncomes());
        assertEquals(1, transactionService.countExpenses());
    }

    @Test
    void shouldGetDailySummary() {
        transactionService.createIncome(new BigDecimal("5000.00"), "Salário", "Salário");
        transactionService.createExpense(new BigDecimal("1500.00"), "Aluguel", "Moradia");

        List<Map<String, Object>> summary = transactionService.getDailySummary(7);
        assertFalse(summary.isEmpty());
        assertEquals(new BigDecimal("5000.00"), summary.get(0).get("totalIncome"));
        assertEquals(new BigDecimal("1500.00"), summary.get(0).get("totalExpense"));
    }
}