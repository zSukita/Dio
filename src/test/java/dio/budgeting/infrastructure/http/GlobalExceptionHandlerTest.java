package dio.budgeting.infrastructure.http;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleIllegalArgument() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgument(
                new IllegalArgumentException("Valor deve ser positivo"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("Valor deve ser positivo", response.getBody().message());
    }

    @Test
    void shouldHandleValidation() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(null, "request");
        bindingResult.addError(new FieldError("request", "amount", "must be positive"));

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(
                new MethodArgumentNotValidException(null, bindingResult));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertTrue(response.getBody().message().contains("amount"));
    }

    @Test
    void shouldHandleInvalidFormat() {
        InvalidFormatException ife = mock(InvalidFormatException.class);
        when(ife.getPath()).thenReturn(java.util.Collections.singletonList(
                new com.fasterxml.jackson.databind.JsonMappingException.Reference(null, "amount")));
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("msg", ife);

        ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidFormat(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("Formato inválido para o campo 'amount'", response.getBody().message());
    }

    @Test
    void shouldHandleInvalidFormatWithoutCause() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("msg");

        ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidFormat(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Corpo da requisição inválido", response.getBody().message());
    }

    @Test
    void shouldHandleDateTimeParse() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleDateTimeParse(
                new DateTimeParseException("invalid date", "abc", 0));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertTrue(response.getBody().message().contains("ISO 8601"));
    }

    @Test
    void shouldHandleMissingParam() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleMissingParam(
                new MissingServletRequestParameterException("startDate", "String"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertTrue(response.getBody().message().contains("startDate"));
    }

    @Test
    void shouldHandleTypeMismatch() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", java.time.LocalDateTime.class, "startDate", null, null);

        ResponseEntity<ApiResponse<Void>> response = handler.handleTypeMismatch(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertTrue(response.getBody().message().contains("startDate"));
    }

    @Test
    void shouldHandleGenericException() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneric(new RuntimeException("erro interno"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("Erro interno do servidor", response.getBody().message());
    }
}
