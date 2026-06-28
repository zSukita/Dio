# Budgeting API com IA

API Spring Boot + Spring AI para controle de orçamento pessoal com comandos de voz e texto.

Enviar "qual meu saldo?" ou "crie uma despesa de 50 reais de transporte" — a IA entende, executa e responde.

## Fluxo Principal

1. Envia áudio ou texto com um comando financeiro
2. Áudio é transcrito (Whisper) → IA interpreta e escolhe a ferramenta
3. Função real da aplicação é executada (criar transação, consultar saldo etc.)
4. IA gera resposta amigável — se foi áudio, converte para fala (TTS)

## Tecnologias

- **Java 21** + **Spring Boot 3.3.2** + **Spring AI 1.0.0-M6**
- **OpenAI** (GPT-4o-mini, Whisper, TTS-1)
- **H2** (dev) / **PostgreSQL** (prod)
- **JPA / Hibernate**

## Como Executar

```bash
export OPENAI_API_KEY="sua-chave-aqui"

./mvnw.cmd spring-boot:run                           # H2 em memória
docker-compose up -d; ./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod  # PostgreSQL
```

A aplicação inicia em `http://localhost:8080`.

## Endpoints REST

### Transações

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/transactions/income` | Criar receita |
| POST | `/api/transactions/expense` | Criar despesa |
| GET | `/api/transactions` | Listar todas |
| GET | `/api/transactions/{id}` | Buscar por ID |
| GET | `/api/transactions/type/{type}` | Filtrar por tipo (`INCOME` / `EXPENSE`) |
| GET | `/api/transactions/category/{category}` | Filtrar por categoria |
| DELETE | `/api/transactions/{id}` | Remover transação |

### Consultas Financeiras

| Método | Rota | Parâmetros | Descrição |
|--------|------|-----------|-----------|
| GET | `/api/transactions/balance` | — | Saldo total |
| GET | `/api/transactions/balance/since` | `startDate` | Saldo desde uma data |
| GET | `/api/transactions/income/total` | `startDate`, `endDate` | Total de receitas no período |
| GET | `/api/transactions/expense/total` | `startDate`, `endDate` | Total de despesas no período |
| GET | `/api/transactions/income/by-category` | `startDate`, `endDate` | Receitas agrupadas por categoria |
| GET | `/api/transactions/expense/by-category` | `startDate`, `endDate` | Despesas agrupadas por categoria |
| GET | `/api/transactions/summary/monthly` | `months` (default: 3) | Resumo mensal |
| GET | `/api/transactions/summary/daily` | `days` (default: 7) | Resumo diário |
| GET | `/api/transactions/range` | `startDate`, `endDate` | Transações no período |
| GET | `/api/transactions/largest` | — | 5 maiores transações |
| GET | `/api/transactions/search` | `q` | Busca por descrição |

### Inteligência Artificial

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/transactions/ai/text` | Comando de texto processado pela IA |
| POST | `/api/transactions/ai/voice` | Comando de voz (áudio → resposta em áudio) |

---

## Formato das Datas

Todos os parâmetros de data usam **ISO 8601**:

```
2024-01-01T00:00:00
2024-12-31T23:59:59
```

Exemplo:
```
GET /api/transactions/income/total?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
```

---

## Formato das Respostas

Todas as respostas seguem o wrapper `ApiResponse`:

```json
{
  "success": true,
  "message": null,
  "data": { ... },
  "timestamp": "2024-01-01T00:00:00"
}
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `success` | boolean | `true` para sucesso, `false` para erro |
| `message` | string ou null | Mensagem de erro ou `null` em sucesso |
| `data` | any | Payload da resposta |
| `timestamp` | string | ISO 8601 do momento da resposta |

### Exemplos

**Criar receita** (201 Created):
```json
{
  "success": true,
  "message": "Recurso criado com sucesso",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 5000.00,
    "type": "INCOME",
    "description": "Salário mensal",
    "category": "Salário",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

**Erro de validação** (400 Bad Request):
```json
{
  "success": false,
  "message": "Valor deve ser positivo",
  "data": null,
  "timestamp": "2024-01-01T10:00:00"
}
```

**Erro interno** (500):
```json
{
  "success": false,
  "message": "Erro interno do servidor",
  "data": null,
  "timestamp": "2024-01-01T10:00:00"
}
```

---

## Como Testar

### Pelo terminal (PowerShell)

```powershell
$env:OPENAI_API_KEY = "sua-chave"

# Criar receita
Invoke-WebRequest -Uri "http://localhost:8080/api/transactions/income" `
  -Method Post -Headers @{"Content-Type"="application/json"} `
  -Body '{"amount":5000,"description":"Salario","category":"Salario"}'

# Saldo total
Invoke-WebRequest -Uri "http://localhost:8080/api/transactions/balance"

# Total de despesas em janeiro
Invoke-WebRequest -Uri "http://localhost:8080/api/transactions/expense/total?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59"

# Despesas agrupadas por categoria
Invoke-WebRequest -Uri "http://localhost:8080/api/transactions/expense/by-category?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59"

# Buscar transações por descrição
Invoke-WebRequest -Uri "http://localhost:8080/api/transactions/search?q=supermercado"

# Resumo mensal (últimos 6 meses)
Invoke-WebRequest -Uri "http://localhost:8080/api/transactions/summary/monthly?months=6"

# 5 maiores transações
Invoke-WebRequest -Uri "http://localhost:8080/api/transactions/largest"

# Comando de texto com IA
Invoke-WebRequest -Uri "http://localhost:8080/api/transactions/ai/text" `
  -Method Post -Headers @{"Content-Type"="application/json"} `
  -Body '{"text":"qual meu saldo atual"}'

# Comando de texto: criar receita via IA
Invoke-WebRequest -Uri "http://localhost:8080/api/transactions/ai/text" `
  -Method Post -Headers @{"Content-Type"="application/json"} `
  -Body '{"text":"crie uma receita de 5000 reais de salário"}'
```

### Testes automatizados

```bash
# Configurar variáveis e executar
$env:OPENAI_API_KEY = "sk-placeholder"
./mvnw.cmd test
```

> **Nota**: O Maven wrapper `.mvn/wrapper/maven-wrapper.jar` pode não funcionar. Use Maven 3.9.8 manualmente se necessário.

---

## Comandos que a IA Entende

A IA (alimentada pelo sistema de Tool Calling) pode executar estas ações:

| Comando falado/digitado | Ação |
|------------------------|------|
| "crie uma receita de 5000 reais de salário" | `create_income(5000, "Salário", "Salário")` |
| "adicione uma despesa de 50 reais de transporte" | `create_expense(50, "Transporte", "Transporte")` |
| "qual meu saldo?" | `get_balance()` |
| "quanto gastei em alimentação esse mês?" | `get_expenses_by_category(dataInicio, dataFim)` |
| "liste minhas receitas" | `get_transactions_by_type("INCOME")` |
| "quanto recebi de freelas?" | `get_income_by_category(dataInicio, dataFim)` |
| "me mostre as maiores transações" | `get_largest_transactions()` |
| "busque por supermercado" | `search_transactions("supermercado")` |
| "delete a transação 550e..." | `delete_transaction("550e...")` |
| "resumo dos últimos 3 meses" | `get_monthly_summary(3)` |

A IA retorna respostas detalhadas com emojis para texto, e concisas sem emojis para voz.

---

## Tratamento de Erros

| HTTP | Causa |
|------|-------|
| 400 | `IllegalArgumentException`, validação de DTO, data inválida, parâmetro ausente |
| 404 | Transação não encontrada |
| 500 | Erro interno do servidor |

---

## Estrutura do Projeto

```
src/main/java/dio/budgeting/
├── BudgetingApplication.java
├── domain/              # Entidade Transaction + TransactionRepository (JPA)
├── application/         # TransactionService + TransactionTools (@Tool)
└── infrastructure/
    ├── ai/              # AiService (Chat + Whisper + TTS)
    ├── config/          # AiConfig (ChatClient, ToolCallbackProvider)
    ├── http/            # TransactionController, ApiResponse, GlobalExceptionHandler
    └── jpa/             # (reservado para adaptadores JPA customizados)
```

## Licença

MIT
