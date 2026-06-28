package dio.budgeting.application.ai;

import dio.budgeting.application.TransactionService;
import dio.budgeting.domain.Transaction;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class TransactionTools {

    private final TransactionService transactionService;

    public TransactionTools(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Tool(name = "create_income", description = "Cria uma nova receita (entrada de dinheiro)")
    public Transaction createIncome(
            @ToolParam(description = "Valor da receita (positivo)") BigDecimal amount,
            @ToolParam(description = "Descrição da receita") String description,
            @ToolParam(description = "Categoria da receita (ex: Salário, Freelance, Investimentos)") String category
    ) {
        return transactionService.createIncome(amount, description, category);
    }

    @Tool(name = "create_expense", description = "Cria uma nova despesa (saída de dinheiro)")
    public Transaction createExpense(
            @ToolParam(description = "Valor da despesa (positivo)") BigDecimal amount,
            @ToolParam(description = "Descrição da despesa") String description,
            @ToolParam(description = "Categoria da despesa (ex: Alimentação, Transporte, Lazer, Moradia)") String category
    ) {
        return transactionService.createExpense(amount, description, category);
    }

    @Tool(name = "get_balance", description = "Retorna o saldo total (receitas - despesas)")
    public Map<String, Object> getBalance() {
        return Map.of("balance", transactionService.getBalance());
    }

    @Tool(name = "get_transactions_by_type", description = "Lista transações por tipo (INCOME ou EXPENSE)")
    public List<Transaction> getTransactionsByType(
            @ToolParam(description = "Tipo da transação: INCOME ou EXPENSE") String type
    ) {
        return transactionService.findByType(Transaction.TransactionType.valueOf(type.toUpperCase()));
    }

    @Tool(name = "get_transactions_by_category", description = "Lista transações por categoria")
    public List<Transaction> getTransactionsByCategory(
            @ToolParam(description = "Categoria da transação") String category
    ) {
        return transactionService.findByCategory(category);
    }

    @Tool(name = "get_all_transactions", description = "Lista todas as transações")
    public List<Transaction> getAllTransactions() {
        return transactionService.findAll();
    }
}