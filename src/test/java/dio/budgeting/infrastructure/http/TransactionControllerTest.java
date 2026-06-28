package dio.budgeting.infrastructure.http;

import dio.budgeting.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private dio.budgeting.application.TransactionService transactionService;

    @MockBean
    private dio.budgeting.infrastructure.ai.AiService aiService;

    @Test
    void shouldCreateIncomeViaRest() throws Exception {
        Transaction savedTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("5000.00"))
                .type(Transaction.TransactionType.INCOME)
                .description("Salário")
                .category("Salário")
                .build();

        when(transactionService.createIncome(any(), anyString(), anyString()))
                .thenReturn(savedTransaction);

        mockMvc.perform(post("/api/transactions/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"amount": 5000.00, "description": "Salário", "category": "Salário"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.description").value("Salário"));
    }

    @Test
    void shouldCreateExpenseViaRest() throws Exception {
        Transaction savedTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("150.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .description("Supermercado")
                .category("Alimentação")
                .build();

        when(transactionService.createExpense(any(), anyString(), anyString()))
                .thenReturn(savedTransaction);

        mockMvc.perform(post("/api/transactions/expense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"amount": 150.00, "description": "Supermercado", "category": "Alimentação"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    void shouldGetBalance() throws Exception {
        when(transactionService.getBalance()).thenReturn(new BigDecimal("550.00"));

        mockMvc.perform(get("/api/transactions/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(550.00));
    }

    @Test
    void shouldListAllTransactions() throws Exception {
        Transaction t1 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("1000.00"))
                .type(Transaction.TransactionType.INCOME)
                .description("Salário")
                .category("Salário")
                .build();

        Transaction t2 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("300.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .description("Aluguel")
                .category("Moradia")
                .build();

        when(transactionService.findAll()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}