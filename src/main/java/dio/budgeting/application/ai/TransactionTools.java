package dio.budgeting.application.ai;

import dio.budgeting.application.TransactionService;
import dio.budgeting.domain.Transaction;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TransactionTools {

    private final TransactionService transactionService;

    public TransactionTools(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Tool(description = "Cria uma nova receita (entrada de dinheiro)")
    public Transaction createIncome(
            @ToolParam(description = "Valor da receita") BigDecimal amount,
            @ToolParam(description = "Descrição da receita") String description,
            @ToolParam(description = "Categoria da receita (ex: Salário, Freelance, Investimentos)") String category
    ) {
        return transactionService.createIncome(amount, description, category);
    }

    @Tool(description = "Cria uma nova despesa (saída de dinheiro)")
    public Transaction createExpense(
            @ToolParam(description = "Valor da despesa") BigDecimal amount,
            @ToolParam(description = "Descrição da despesa") String description,
            @ToolParam(description = "Categoria da despesa (ex: Alimentação, Transporte, Lazer)") String category
    ) {
        return transactionService.createExpense(amount, description, category);
    }

    @Tool(description = "Consulta o saldo atual (receitas - despesas)")
    public BigDecimal getBalance() {
        return transactionService.getBalance();
    }

    @Tool(description = "Consulta o saldo desde uma data específica")
    public BigDecimal getBalanceSince(
            @ToolParam(description = "Data inicial no formato ISO (ex: 2024-01-01T00:00:00)") String startDate
    ) {
        return transactionService.getBalanceSince(LocalDateTime.parse(startDate));
    }

    @Tool(description = "Lista todas as transações")
    public List<Transaction> listAllTransactions() {
        return transactionService.findAll();
    }

    @Tool(description = "Lista transações por tipo (INCOME ou EXPENSE)")
    public List<Transaction> listTransactionsByType(
            @ToolParam(description = "Tipo da transação: INCOME ou EXPENSE") String type
    ) {
        return transactionService.findByType(Transaction.TransactionType.valueOf(type.toUpperCase()));
    }

    @Tool(description = "Lista transações por categoria")
    public List<Transaction> listTransactionsByCategory(
            @ToolParam(description = "Categoria da transação") String category
    ) {
        return transactionService.findByCategory(category);
    }

    @Tool(description = "Lista transações desde uma data específica")
    public List<Transaction> listTransactionsSince(
            @ToolParam(description = "Data inicial no formato ISO (ex: 2024-01-01T00:00:00)") String startDate
    ) {
        return transactionService.findSince(LocalDateTime.parse(startDate));
    }
}