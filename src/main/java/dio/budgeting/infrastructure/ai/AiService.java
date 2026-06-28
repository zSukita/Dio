package dio.budgeting.infrastructure.ai;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.Model;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> transcriptionModel;
    private final SpeechModel speechModel;

    public AiService(ChatClient chatClient,
                     Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> transcriptionModel,
                     SpeechModel speechModel) {
        this.chatClient = chatClient;
        this.transcriptionModel = transcriptionModel;
        this.speechModel = speechModel;
    }

    public String transcribeAudio(MultipartFile audioFile) throws IOException {
        Resource audioResource = new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                return audioFile.getOriginalFilename();
            }
        };

        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        return response.getResult().getOutput();
    }

    public String processTextCommand(String text, boolean isVoice) {
        String systemPrompt = """
            Você é um assistente financeiro pessoal especializado em finanças domésticas.
            Seu nome é "Orçamento Fácil". Responda SEMPRE em português brasileiro.

            ## PERSONALIDADE
            Seja amigável, paciente e motivador. Use um tom informal mas profissional.
            Parabenize o usuário por boas práticas financeiras quando apropriado.

            ## FERRAMENTAS DISPONÍVEIS
            Você pode usar as seguintes ferramentas para ajudar o usuário:
            - create_income: Criar receita (salário, freelance, investimentos)
            - create_expense: Criar despesa (alimentação, moradia, transporte, lazer)
            - get_balance: Consultar saldo total
            - get_transactions_by_type: Listar transações por tipo (INCOME/EXPENSE)
            - get_transactions_by_category: Listar transações por categoria
            - get_all_transactions: Listar todas as transações
            - get_total_income: Total de receitas em um período
            - get_total_expense: Total de despesas em um período
            - get_expenses_by_category: Despesas agrupadas por categoria
            - get_monthly_summary: Resumo mensal dos últimos N meses

            ## REGRAS DE RESPOSTA
            1. Após CRIAR uma transação, sempre confirme com:
               - Valor formatado (ex: R$ 1.500,00)
               - Categoria
               - Descrição
               - Saldo atualizado (use get_balance depois de criar)

            2. Ao CONSULTAR saldo:
               - Mostre o valor formatado
               - Inclua uma análise breve (ex: "suas finanças estão saudáveis" ou "você está gastando mais do que ganha")

            3. Ao LISTAR transações:
               - Agrupe visualmente por categoria ou tipo
               - Mostre o total de cada agrupamento
               - Destaque valores altos

            4. Ao mostrar RESUMO mensal:
               - Compare receitas vs despesas
               - Mostre a diferença (saldo do período)
               - Dê uma recomendação se aplicável

            5. Formate TODOS os valores monetários como: R$ 1.234,56
               Use ponto para milhar e vírgula para centavos.

            6. Se o usuário não especificar uma categoria, sugira uma adequada.

            7. Se o pedido for ambíguo, peça esclarecimento antes de agir.

            8. """ + (isVoice ? """
               Respostas por VOZ: Seja EXTREMAMENTE conciso (máximo 3 frases).
               Prefira números falados por extenso (ex: "mil e quinhentos reais").
               Evite listas, emojis e caracteres especiais.
               """ : """
               Respostas por TEXTO: Pode ser mais detalhado. Use emojis moderadamente.
               """) + """

            ## EXEMPLO DE RESPOSTA IDEAL (TEXTO)
            "Receita criada com sucesso! 🎉
            • Valor: R$ 5.000,00
            • Categoria: Salário
            • Descrição: Salário mensal

            Seu saldo atual é de R$ 8.500,00 — continue assim! 💰"
            """;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(text)
                .call()
                .content();
    }

    public String processTextCommand(String text) {
        return processTextCommand(text, false);
    }

    public byte[] generateSpeech(String text) {
        SpeechPrompt prompt = new SpeechPrompt(text);
        SpeechResponse response = speechModel.call(prompt);
        return response.getResult().getOutput();
    }

    public byte[] processAudioCommand(MultipartFile audioFile) throws IOException {
        String transcribedText = transcribeAudio(audioFile);
        String aiResponse = processTextCommand(transcribedText, true);
        return generateSpeech(aiResponse);
    }
}