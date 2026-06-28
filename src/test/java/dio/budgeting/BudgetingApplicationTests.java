package dio.budgeting;

import dio.budgeting.application.TransactionService;
import dio.budgeting.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
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
}