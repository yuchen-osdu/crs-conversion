package org.opengroup.osdu.crs.middleware;

import org.opengroup.osdu.crs.api.exception.BadRequestException;
import org.opengroup.osdu.crs.util.AppError;
import org.opengroup.osdu.crs.util.AppException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ValidationException;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { BadRequestException.class, IllegalArgumentException.class })
    public ResponseEntity<AppError> handleBadRequest(Exception  e) {
        AppError appError = AppError.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Bad request")
                .reason("Error")
                .build();

        return ResponseEntity.badRequest().body(appError);
    }

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<AppError> handleValidationException(ValidationException e) {
        AppError appError = AppError.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();

        return ResponseEntity.badRequest().body(appError);
    }

    @NonNull
    @Override
    public ResponseEntity<Object> handleNoHandlerFoundException(@NonNull NoHandlerFoundException ex, @NonNull HttpHeaders headers,
                                                                @NonNull HttpStatus status, @NonNull WebRequest request) {
        AppError appError = AppError.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message("Resource not found.")
                .reason("Resource not found.")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body(appError);
    }

    @NonNull
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex, @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatus status, @NonNull WebRequest request) {
        return getBadInputResponse();
    }

    @NonNull
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex, @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatus status, @NonNull WebRequest request) {
        return getBadInputResponse();
    }

    @ExceptionHandler(value = HttpStatusCodeException.class)
    public ResponseEntity<AppError> handleHttpStatusCodeException(HttpStatusCodeException e) {
        HttpStatus statusCode = e.getStatusCode();
        if (statusCode == HttpStatus.METHOD_NOT_ALLOWED) {
            return getResponse(new AppException(statusCode.value(), "Method not allowed.", "Method not allowed.", e));
        }
        return getResponse(new AppException(statusCode.value(), "Server error.", "An unknown error has occurred.", e));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<AppError> handleGenericException(Exception e) {
        if (e instanceof AppException) {
            return getResponse((AppException) e);
        } else {
            return getResponse(new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server error.", "An unknown error has occurred.", e));
        }
    }

    private ResponseEntity<Object> getBadInputResponse() {
        String errorMessage = "bad input type or format, check the input type and format.";
        AppError appError = AppError.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(errorMessage)
                .build();

        return ResponseEntity.badRequest().body(appError);
    }

    private ResponseEntity<AppError> getResponse(AppException appException) {
        AppError appError = appException.getError();
        return ResponseEntity.status(appError.getCode()).contentType(MediaType.APPLICATION_JSON).body(appError);
    }
}
