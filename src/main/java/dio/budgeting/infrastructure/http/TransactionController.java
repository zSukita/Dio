package dio.budgeting.infrastructure.http;

import dio.budgeting.application.TransactionService;
import dio.budgeting.domain.Transaction;
import dio.budgeting.infrastructure.ai.AiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AiService aiService;

    @PostMapping("/income")
    public ResponseEntity<ApiResponse<Transaction>> createIncome(@Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactionService.createIncome(request.amount(), request.description(), request.category());
        return ApiResponse.created(transaction);
    }

    @PostMapping("/expense")
    public ResponseEntity<ApiResponse<Transaction>> createExpense(@Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactionService.createExpense(request.amount(), request.description(), request.category());
        return ApiResponse.created(transaction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Transaction>> findById(@PathVariable("id") String id) {
        return transactionService.findById(UUID.fromString(id))
                .map(ApiResponse::ok)
                .orElse(ApiResponse.notFound("Transação não encontrada"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> findAll() {
        return ApiResponse.ok(transactionService.findAll());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<Transaction>>> findByType(@PathVariable("type") String type) {
        return ApiResponse.ok(transactionService.findByType(Transaction.TransactionType.valueOf(type.toUpperCase())));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Transaction>>> findByCategory(@PathVariable("category") String category) {
        return ApiResponse.ok(transactionService.findByCategory(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable("id") String id) {
        transactionService.deleteTransaction(UUID.fromString(id));
        return ApiResponse.ok(null);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Transaction>>> searchByDescription(@RequestParam("q") String q) {
        return ApiResponse.ok(transactionService.searchByDescription(q));
    }

    @GetMapping("/largest")
    public ResponseEntity<ApiResponse<List<Transaction>>> getLargestTransactions() {
        return ApiResponse.ok(transactionService.getLargestTransactions());
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBalance() {
        return ApiResponse.ok(Map.of("balance", transactionService.getBalance()));
    }

    @GetMapping("/balance/since")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBalanceSince(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        return ApiResponse.ok(Map.of("balance", transactionService.getBalanceSince(startDate)));
    }

    @GetMapping("/income/total")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTotalIncome(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.ok(Map.of("totalIncome", transactionService.getTotalIncomeBetween(startDate, endDate)));
    }

    @GetMapping("/expense/total")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTotalExpense(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.ok(Map.of("totalExpense", transactionService.getTotalExpenseBetween(startDate, endDate)));
    }

    @GetMapping("/expense/by-category")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExpensesByCategory(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.ok(Map.of("categories", transactionService.getExpensesByCategoryBetween(startDate, endDate)));
    }

    @GetMapping("/income/by-category")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getIncomeByCategory(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.ok(Map.of("categories", transactionService.getIncomeByCategoryBetween(startDate, endDate)));
    }

    @GetMapping("/summary/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlySummary(@RequestParam(defaultValue = "3") int months) {
        return ApiResponse.ok(Map.of("months", transactionService.getMonthlySummary(months)));
    }

    @GetMapping("/summary/daily")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailySummary(@RequestParam(defaultValue = "7") int days) {
        return ApiResponse.ok(Map.of("days", transactionService.getDailySummary(days)));
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<Transaction>>> findByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.ok(transactionService.findByDateRange(startDate, endDate));
    }

    @PostMapping(value = "/ai/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<byte[]>> processVoiceCommand(@RequestParam("audio") MultipartFile audioFile) {
        try {
            byte[] audioResponse = aiService.processAudioCommand(audioFile);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .body(new ApiResponse<>(true, "Áudio processado com sucesso", audioResponse, null));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao processar comando de voz: " + e.getMessage());
        }
    }

    @PostMapping("/ai/text")
    public ResponseEntity<ApiResponse<Map<String, String>>> processTextCommand(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.isBlank()) {
            return ApiResponse.badRequest("O campo 'text' é obrigatório");
        }
        String response = aiService.processTextCommand(text);
        return ApiResponse.ok(Map.of("response", response));
    }

    public record CreateTransactionRequest(
            @NotNull @DecimalMin(value = "0.01", message = "Valor deve ser positivo") BigDecimal amount,
            @NotBlank @Size(max = 255) String description,
            @NotBlank @Size(max = 100) String category
    ) {}
}
