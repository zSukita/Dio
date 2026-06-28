package dio.budgeting.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.audio.transcription.TranscriptionModel;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

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
    private TranscriptionModel transcriptionModel;

    @Mock
    private SpeechModel speechModel;

    @InjectMocks
    private AiService aiService;

    @Test
    void shouldTranscribeAudio() throws Exception {
        MultipartFile audioFile = mock(MultipartFile.class);
        when(audioFile.getBytes()).thenReturn("fake audio content".getBytes());
        when(audioFile.getOriginalFilename()).thenReturn("test.mp3");

        AudioTranscriptionResponse transcriptionResponse = mock(AudioTranscriptionResponse.class);
        var result = mock(org.springframework.ai.model.Result.class);
        when(result.getOutput()).thenReturn("comprei almoço por trinta reais");
        when(transcriptionResponse.getResult()).thenReturn(result);
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
        var output = mock(org.springframework.ai.model.Result.class);
        when(output.getOutput()).thenReturn("audio bytes".getBytes());
        SpeechResponse speechResponse = mock(SpeechResponse.class);
        when(speechResponse.getResult()).thenReturn(output);
        when(speechModel.call(any())).thenReturn(speechResponse);

        byte[] audio = aiService.generateSpeech("Receita criada com sucesso");
        assertNotNull(audio);
        assertTrue(audio.length > 0);
    }
}