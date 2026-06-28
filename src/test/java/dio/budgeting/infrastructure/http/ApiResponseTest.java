package dio.budgeting.infrastructure.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void shouldCreateOkResponse() {
        ResponseEntity<ApiResponse<String>> response = ApiResponse.ok("data");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals("data", response.getBody().data());
        assertNull(response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void shouldCreateOkResponseWithNullData() {
        ResponseEntity<ApiResponse<Object>> response = ApiResponse.ok(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().data());
    }

    @Test
    void shouldCreateCreatedResponse() {
        ResponseEntity<ApiResponse<String>> response = ApiResponse.created("new-resource");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals("Recurso criado com sucesso", response.getBody().message());
        assertEquals("new-resource", response.getBody().data());
    }

    @Test
    void shouldCreateBadRequestResponse() {
        ResponseEntity<ApiResponse<Void>> response = ApiResponse.badRequest("erro de validação");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("erro de validação", response.getBody().message());
        assertNull(response.getBody().data());
    }

    @Test
    void shouldCreateNotFoundResponse() {
        ResponseEntity<ApiResponse<Void>> response = ApiResponse.notFound("não encontrado");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("não encontrado", response.getBody().message());
    }

    @Test
    void shouldCreateErrorResponse() {
        ResponseEntity<ApiResponse<Void>> response = ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "erro interno");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("erro interno", response.getBody().message());
    }

    @Test
    void shouldSetTimestampWhenNull() {
        ApiResponse<String> response = new ApiResponse<>(true, "msg", "data", null);

        assertNotNull(response.timestamp());
    }

    @Test
    void shouldPreserveTimestampWhenProvided() {
        LocalDateTime fixed = LocalDateTime.of(2024, 1, 1, 0, 0);
        ApiResponse<String> response = new ApiResponse<>(true, "msg", "data", fixed);

        assertEquals(fixed, response.timestamp());
    }

    @Test
    void shouldExcludeNullFields() {
        ApiResponse<String> response = new ApiResponse<>(true, null, null, null);

        assertEquals(true, response.success());
        assertNull(response.message());
        assertNull(response.data());
        assertNotNull(response.timestamp());
    }
}
