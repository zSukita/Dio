package dio.budgeting.infrastructure.http;

import dio.budgeting.application.TransactionService;
import dio.budgeting.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private TransactionService transactionService;

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
                        .content("{\"amount\": 5000.00, \"description\": \"Salário\", \"category\": \"Salário\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(5000.00))
                .andExpect(jsonPath("$.data.type").value("INCOME"))
                .andExpect(jsonPath("$.data.description").value("Salário"));
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
                        .content("{\"amount\": 150.00, \"description\": \"Supermercado\", \"category\": \"Alimentação\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(150.00))
                .andExpect(jsonPath("$.data.type").value("EXPENSE"));
    }

    @Test
    void shouldGetBalance() throws Exception {
        when(transactionService.getBalance()).thenReturn(new BigDecimal("550.00"));

        mockMvc.perform(get("/api/transactions/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(550.00));
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
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldDeleteTransaction() throws Exception {
        doNothing().when(transactionService).deleteTransaction(any());

        mockMvc.perform(delete("/api/transactions/" + UUID.randomUUID()))
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldSearchByDescription() throws Exception {
        Transaction t = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("5000.00"))
                .type(Transaction.TransactionType.INCOME)
                .description("Salário mensal")
                .category("Salário")
                .build();

        when(transactionService.searchByDescription("salário")).thenReturn(List.of(t));

        mockMvc.perform(get("/api/transactions/search").param("q", "salário"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].description").value("Salário mensal"));
    }

    @Test
    void shouldGetLargestTransactions() throws Exception {
        Transaction t = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("5000.00"))
                .type(Transaction.TransactionType.INCOME)
                .description("Salário")
                .category("Salário")
                .build();

        when(transactionService.getLargestTransactions()).thenReturn(List.of(t));

        mockMvc.perform(get("/api/transactions/largest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].amount").value(5000.00));
    }

    @Test
    void shouldGetBalanceSince() throws Exception {
        when(transactionService.getBalanceSince(any())).thenReturn(new BigDecimal("3500.00"));

        mockMvc.perform(get("/api/transactions/balance/since")
                        .param("startDate", "2024-01-01T00:00:00"))
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(3500.00));
    }

    @Test
    void shouldReturn404WhenTransactionNotFound() throws Exception {
        when(transactionService.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturn400ForInvalidAmount() throws Exception {
        mockMvc.perform(post("/api/transactions/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": -10, \"description\": \"Teste\", \"category\": \"Teste\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturn400ForMissingTextField() throws Exception {
        mockMvc.perform(post("/api/transactions/ai/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
