package dio.budgeting.application.ai;

import dio.budgeting.application.TransactionService;
import dio.budgeting.domain.Transaction;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Tool(name = "get_total_income", description = "Retorna o total de receitas em um período")
    public BigDecimal getTotalIncome(
            @ToolParam(description = "Data inicial no formato ISO (ex: 2024-01-01T00:00:00)") String startDate,
            @ToolParam(description = "Data final no formato ISO (ex: 2024-12-31T23:59:59)") String endDate
    ) {
        return transactionService.getTotalIncomeBetween(LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }

    @Tool(name = "get_total_expense", description = "Retorna o total de despesas em um período")
    public BigDecimal getTotalExpense(
            @ToolParam(description = "Data inicial no formato ISO (ex: 2024-01-01T00:00:00)") String startDate,
            @ToolParam(description = "Data final no formato ISO (ex: 2024-12-31T23:59:59)") String endDate
    ) {
        return transactionService.getTotalExpenseBetween(LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }

    @Tool(name = "get_expenses_by_category", description = "Retorna o total de despesas agrupadas por categoria em um período")
    public Map<String, BigDecimal> getExpensesByCategory(
            @ToolParam(description = "Data inicial no formato ISO (ex: 2024-01-01T00:00:00)") String startDate,
            @ToolParam(description = "Data final no formato ISO (ex: 2024-12-31T23:59:59)") String endDate
    ) {
        return transactionService.getExpensesByCategoryBetween(LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }

    @Tool(name = "get_monthly_summary", description = "Retorna o resumo mensal (receitas, despesas e saldo) dos últimos N meses")
    public List<Map<String, Object>> getMonthlySummary(
            @ToolParam(description = "Número de meses para considerar (ex: 3 para últimos 3 meses)") int monthsBack
    ) {
        return transactionService.getMonthlySummary(monthsBack);
    }

    @Tool(name = "delete_transaction", description = "Remove uma transação pelo ID")
    public void deleteTransaction(
            @ToolParam(description = "ID da transação no formato UUID (ex: 550e8400-e29b-41d4-a716-446655440000)") String id
    ) {
        transactionService.deleteTransaction(java.util.UUID.fromString(id));
    }

    @Tool(name = "get_balance_since", description = "Retorna o saldo a partir de uma data específica")
    public Map<String, Object> getBalanceSince(
            @ToolParam(description = "Data inicial no formato ISO (ex: 2024-01-01T00:00:00)") String startDate
    ) {
        return Map.of("balance", transactionService.getBalanceSince(java.time.LocalDateTime.parse(startDate)));
    }

    @Tool(name = "get_largest_transactions", description = "Retorna as 5 maiores transações (independente de tipo)")
    public List<Transaction> getLargestTransactions() {
        return transactionService.getLargestTransactions();
    }

    @Tool(name = "search_transactions", description = "Busca transações pelo texto da descrição")
    public List<Transaction> searchTransactions(
            @ToolParam(description = "Texto para buscar na descrição das transações") String query
    ) {
        return transactionService.searchByDescription(query);
    }

    @Tool(name = "get_income_by_category", description = "Retorna o total de receitas agrupadas por categoria em um período")
    public Map<String, BigDecimal> getIncomeByCategory(
            @ToolParam(description = "Data inicial no formato ISO (ex: 2024-01-01T00:00:00)") String startDate,
            @ToolParam(description = "Data final no formato ISO (ex: 2024-12-31T23:59:59)") String endDate
    ) {
        return transactionService.getIncomeByCategoryBetween(java.time.LocalDateTime.parse(startDate), java.time.LocalDateTime.parse(endDate));
    }

    @Tool(name = "get_transaction_counts", description = "Retorna o total de receitas e despesas cadastradas")
    public Map<String, Object> getTransactionCounts() {
        return Map.of("totalIncomes", transactionService.countIncomes(), "totalExpenses", transactionService.countExpenses());
    }

    @Tool(name = "get_daily_summary", description = "Retorna o resumo diário (receitas, despesas e saldo por dia) dos últimos N dias")
    public List<Map<String, Object>> getDailySummary(
            @ToolParam(description = "Número de dias para considerar (ex: 7 para última semana)") int daysBack
    ) {
        return transactionService.getDailySummary(daysBack);
    }
}