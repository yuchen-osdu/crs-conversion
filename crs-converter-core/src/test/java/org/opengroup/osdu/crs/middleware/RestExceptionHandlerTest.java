package org.opengroup.osdu.crs.middleware;

import org.junit.Test;
import org.opengroup.osdu.crs.api.exception.BadRequestException;
import org.opengroup.osdu.crs.util.AppError;
import org.opengroup.osdu.crs.util.AppException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mock;

public class RestExceptionHandlerTest {

    @Test
    public void shouldReturnBadRequest() {
        RestExceptionHandler handler = new RestExceptionHandler();
        BadRequestException exception = new BadRequestException("Bad request");

        ResponseEntity<AppError> response = handler.handleBadRequest(exception);

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = response.getBody();
        assertEquals(400, body.getCode());
        assertEquals("Error", body.getReason());
        assertEquals("Bad request", body.getMessage());
    }

    @Test
    public void should_useValuesInAppExceptionInResponse_When_AppExceptionIsHandledByGlobalExceptionMapper() {
        RestExceptionHandler handler = new RestExceptionHandler();
        AppException exception = new AppException(409, "any reason", "any message");

        ResponseEntity<AppError> response = handler.handleGenericException(exception);

        assertEquals(409, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = response.getBody();
        assertEquals(409, body.getCode());
        assertEquals("any reason", body.getReason());
        assertEquals("any message", body.getMessage());
    }

    @Test
    public void should_useGenericValuesInResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {
        RestExceptionHandler handler = new RestExceptionHandler();
        Exception exception = new Exception("any message");

        ResponseEntity<AppError> response = handler.handleGenericException(exception);

        assertEquals(500, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = response.getBody();
        assertEquals(500, body.getCode());
        assertEquals("Server error.", body.getReason());
        assertEquals("An unknown error has occurred.", body.getMessage());
    }

    @Test
    public void should_returnNotFoundResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {
        RestExceptionHandler handler = new RestExceptionHandler();
        NoHandlerFoundException exception = new NoHandlerFoundException("Any method", "Any requestURL", new HttpHeaders());

        ResponseEntity<Object> response = handler.handleNoHandlerFoundException(exception, new HttpHeaders(), HttpStatus.NOT_FOUND, mock(WebRequest.class));

        assertEquals(404, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = (AppError) response.getBody();
        assertEquals(404, body.getCode());
        assertEquals("Resource not found.", body.getReason());
        assertEquals("Resource not found.", body.getMessage());
    }

    @Test
    public void should_returnNotAllowedResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {
        RestExceptionHandler handler = new RestExceptionHandler();
        HttpStatusCodeException exception = new HttpServerErrorException(HttpStatus.METHOD_NOT_ALLOWED);

        ResponseEntity<AppError> response = handler.handleHttpStatusCodeException(exception);

        assertEquals(405, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = response.getBody();
        assertEquals(405, body.getCode());
        assertEquals("Method not allowed.", body.getReason());
        assertEquals("Method not allowed.", body.getMessage());
    }

}
