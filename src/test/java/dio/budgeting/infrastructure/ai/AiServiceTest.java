package dio.budgeting.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.Model;
import org.springframework.ai.openai.audio.speech.Speech;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    @Mock
    private Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> transcriptionModel;

    @Mock
    private SpeechModel speechModel;

    @InjectMocks
    private AiService aiService;

    @Test
    void shouldTranscribeAudio() throws Exception {
        MultipartFile audioFile = mock(MultipartFile.class);
        when(audioFile.getBytes()).thenReturn("fake audio content".getBytes());

        AudioTranscription transcription = new AudioTranscription("comprei almoço por trinta reais");
        AudioTranscriptionResponse transcriptionResponse = new AudioTranscriptionResponse(transcription);
        when(transcriptionModel.call(any(AudioTranscriptionPrompt.class))).thenReturn(transcriptionResponse);

        String transcribed = aiService.transcribeAudio(audioFile);
        assertEquals("comprei almoço por trinta reais", transcribed);
    }

    @Test
    void shouldProcessTextCommand() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("Receita de R$ 5.000,00 criada com sucesso!");

        String response = aiService.processTextCommand("Crie uma receita de 5000 reais de salário");
        assertEquals("Receita de R$ 5.000,00 criada com sucesso!", response);
    }

    @Test
    void shouldGenerateSpeech() {
        Speech speech = new Speech("audio bytes".getBytes());
        SpeechResponse speechResponse = new SpeechResponse(speech);
        when(speechModel.call(any(SpeechPrompt.class))).thenReturn(speechResponse);

        byte[] audio = aiService.generateSpeech("Receita criada com sucesso");
        assertNotNull(audio);
        assertTrue(audio.length > 0);
    }
}