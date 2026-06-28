# Budgeting API com IA

Uma API Spring Boot que integra inteligência artificial para processar comandos de voz e texto relacionados a transações financeiras. O usuário pode falar ou digitar algo como "crie uma despesa de 50 reais de transporte" e a IA entende a intenção, executa a ação no sistema e responde com a confirmação.

## Fluxo Principal

1. Usuário envia um áudio ou texto com um comando financeiro
2. O áudio é transcrito para texto (Whisper)
3. A IA analisa o texto e decide qual ferramenta chamar
4. Uma função real da aplicação é executada (criar transação, consultar saldo, etc.)
5. A IA gera uma resposta amigável
6. Se foi áudio, a resposta é convertida para fala (TTS)

## Tecnologias

- **Java 21** + **Spring Boot 3.3.2**
- **Spring AI 1.0.0-M6** (ChatClient, Tool Calling, Whisper, TTS)
- **OpenAI** (GPT-4o-mini, Whisper, TTS-1)
- **H2** (desenvolvimento) / **PostgreSQL** (produção)
- **JPA / Hibernate** para persistência
- **Docker Compose** para o banco PostgreSQL

## Como Executar

```bash
# 1. Configure a chave da OpenAI
export OPENAI_API_KEY="sua-chave-aqui"

# 2. Execute a aplicação (H2 em memória)
./mvnw.cmd spring-boot:run

# Para usar PostgreSQL (produção):
docker-compose up -d
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
```

A aplicação inicia em `http://localhost:8080`.

## Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/transactions/income` | Criar receita |
| POST | `/api/transactions/expense` | Criar despesa |
| GET | `/api/transactions` | Listar todas |
| GET | `/api/transactions/{id}` | Buscar por ID |
| GET | `/api/transactions/type/{type}` | Filtrar por tipo (INCOME/EXPENSE) |
| GET | `/api/transactions/category/{category}` | Filtrar por categoria |
| GET | `/api/transactions/balance` | Saldo total |
| POST | `/api/transactions/ai/text` | Comando de texto com IA |
| POST | `/api/transactions/ai/voice` | Comando de voz (áudio → resposta em áudio) |

## Como Testar o Fluxo Principal

### 1. Pelo terminal (PowerShell)

```powershell
$env:OPENAI_API_KEY = "sua-chave"

# Criar receita via REST
Invoke-WebRequest -Uri "http://localhost:8080/api/transactions/income" `
  -Method Post -Headers @{"Content-Type"="application/json"} `
  -Body '{"amount":5000,"description":"Salario","category":"Salario"}'

# Consultar saldo
Invoke-WebRequest -Uri "http://localhost:8080/api/transactions/balance"

# Comando de texto com IA
Invoke-WebRequest -Uri "http://localhost:8080/api/transactions/ai/text" `
  -Method Post -Headers @{"Content-Type"="application/json"} `
  -Body '{"text":"qual meu saldo atual"}'
```

### 2. Testes automatizados

```bash
./mvnw.cmd test
```

## Melhoria Implementada

Além da estrutura base, adicionei suporte a **múltiplos perfis de ambiente** (dev com H2, prod com PostgreSQL) e **testes completos** para todas as camadas: domínio, aplicação, controladores REST e serviço de IA com mocks.

## O que Aprendi

- Configurar o Spring AI com modelos da OpenAI (Chat, Whisper, TTS)
- Usar `ChatClient` para interagir com LLMs de forma programática
- Implementar **Tool Calling** com `@Tool` e `MethodToolCallbackProvider` para a IA executar funções reais da aplicação
- Integrar transcrição de áudio com `AudioTranscriptionPrompt` e `AudioTranscriptionResponse`
- Integrar texto para fala com `SpeechPrompt` e `SpeechModel`
- Organizar o projeto seguindo responsabilidades claras: domínio → aplicação → infraestrutura
- Lidar com desafios de compatibilidade entre versões (JDK 25 + Lombok + Byte Buddy)
- Conectar IA a uma aplicação real respeitando as camadas e a separação de responsabilidades

## Estrutura do Projeto

```
src/main/java/dio/budgeting/
├── BudgetingApplication.java
├── domain/          # Entidade Transaction e repositório JPA
├── application/     # Casos de uso (TransactionService) e ferramentas da IA (TransactionTools)
└── infrastructure/  # Controladores REST, configuração do Spring AI, adaptadores JPA
```

## Licença

MIT
