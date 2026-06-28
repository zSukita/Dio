package dio.budgeting.infrastructure.ai;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.audio.transcription.TranscriptionModel;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechOptions;
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
    private final TranscriptionModel transcriptionModel;
    private final SpeechModel speechModel;

    public AiService(ChatClient.Builder chatClientBuilder,
                     TranscriptionModel transcriptionModel,
                     SpeechModel speechModel) {
        this.chatClient = chatClientBuilder.build();
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

    public String processTextCommand(String text) {
        String systemPrompt = """
            Você é um assistente financeiro pessoal. Ajude o usuário a gerenciar suas finanças
            criando receitas, despesas e consultando saldos e transações.
            
            Use as ferramentas disponíveis para executar as ações solicitadas.
            Responda sempre em português brasileiro de forma natural e amigável.
            """;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(text)
                .call()
                .content();
    }

    public byte[] generateSpeech(String text) {
        SpeechPrompt prompt = new SpeechPrompt(text, SpeechOptions.builder()
                .voice("alloy")
                .build().build();
        SpeechResponse response = speechModel.call(prompt);
        return response.getResult().getOutput();
    }

    public byte[] processAudioCommand(MultipartFile audioFile) throws IOException {
        // 1. Transcribe audio to text
        String transcribedText = transcribeAudio(audioFile);

        // 2. Process text with AI (which may call tools)
        String aiResponse = processTextCommand(transcribedText);

        // 3. Generate speech from AI response
        return generateSpeech(aiResponse);
    }
}