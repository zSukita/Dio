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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;
    private final AiService aiService;

    @PostMapping("/income")
    public ResponseEntity<Transaction> createIncome(@Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactionService.createIncome(request.amount(), request.description(), request.category());
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/expense")
    public ResponseEntity<Transaction> createExpense(@Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactionService.createExpense(request.amount(), request.description(), request.category());
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> findById(@PathVariable @NotNull String id) {
        return transactionService.findById(java.util.UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> findAll() {
        return ResponseEntity.ok(transactionService.findAll());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Transaction>> findByType(@PathVariable String type) {
        return ResponseEntity.ok(transactionService.findByType(Transaction.TransactionType.valueOf(type.toUpperCase())));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Transaction>> findByCategory(@PathVariable String category) {
        return ResponseEntity.ok(transactionService.findByCategory(category));
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance() {
        return ResponseEntity.ok(Map.of("balance", transactionService.getBalance()));
    }

    @GetMapping("/balance/since")
    public ResponseEntity<Map<String, Object>> getBalanceSince(@RequestParam String startDate) {
        return ResponseEntity.ok(Map.of("balance", transactionService.getBalanceSince(java.time.LocalDateTime.parse(startDate))));
    }

    @GetMapping("/income/total")
    public ResponseEntity<Map<String, Object>> getTotalIncome(
            @RequestParam String startDate, @RequestParam String endDate) {
        return ResponseEntity.ok(Map.of("totalIncome",
                transactionService.getTotalIncomeBetween(java.time.LocalDateTime.parse(startDate), java.time.LocalDateTime.parse(endDate))));
    }

    @GetMapping("/expense/total")
    public ResponseEntity<Map<String, Object>> getTotalExpense(
            @RequestParam String startDate, @RequestParam String endDate) {
        return ResponseEntity.ok(Map.of("totalExpense",
                transactionService.getTotalExpenseBetween(java.time.LocalDateTime.parse(startDate), java.time.LocalDateTime.parse(endDate))));
    }

    @GetMapping("/expense/by-category")
    public ResponseEntity<Map<String, Object>> getExpensesByCategory(
            @RequestParam String startDate, @RequestParam String endDate) {
        return ResponseEntity.ok(Map.of("categories",
                transactionService.getExpensesByCategoryBetween(java.time.LocalDateTime.parse(startDate), java.time.LocalDateTime.parse(endDate))));
    }

    @GetMapping("/summary/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlySummary(@RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(Map.of("months", transactionService.getMonthlySummary(months)));
    }

    @GetMapping("/range")
    public ResponseEntity<List<Transaction>> findByDateRange(
            @RequestParam String startDate, @RequestParam String endDate) {
        return ResponseEntity.ok(transactionService.findByDateRange(
                java.time.LocalDateTime.parse(startDate), java.time.LocalDateTime.parse(endDate)));
    }

    @PostMapping(value = "/ai/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> processVoiceCommand(@RequestParam("audio") MultipartFile audioFile) {
        try {
            byte[] audioResponse = aiService.processAudioCommand(audioFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .body(audioResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/ai/text")
    public ResponseEntity<Map<String, String>> processTextCommand(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String response = aiService.processTextCommand(text);
        return ResponseEntity.ok(Map.of("response", response));
    }

    public record CreateTransactionRequest(
            @NotNull @DecimalMin(value = "0.01", message = "Valor deve ser positivo") BigDecimal amount,
            @NotBlank @Size(max = 255) String description,
            @NotBlank @Size(max = 100) String category
    ) {}
}