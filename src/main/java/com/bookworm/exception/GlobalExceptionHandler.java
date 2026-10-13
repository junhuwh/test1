package com.bookworm.exception;

import com.bookworm.dto.response.ProblemDetailResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetailResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ProblemDetailResponse response = ProblemDetailResponse.builder()
            .type(URI.create("about:blank"))
            .title("Not Found")
            .status(HttpStatus.NOT_FOUND.value())
            .detail(ex.getMessage())
            .instance(URI.create(request.getRequestURI()))
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ProblemDetailResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        ProblemDetailResponse response = ProblemDetailResponse.builder()
            .type(URI.create("about:blank"))
            .title("Conflict")
            .status(HttpStatus.CONFLICT.value())
            .detail(ex.getMessage())
            .instance(URI.create(request.getRequestURI()))
            .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ProblemDetailResponse> handleBusinessViolation(BusinessRuleViolationException ex, HttpServletRequest request) {
        ProblemDetailResponse response = ProblemDetailResponse.builder()
            .type(URI.create("about:blank"))
            .title("Unprocessable Entity")
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .detail(ex.getMessage())
            .instance(URI.create(request.getRequestURI()))
            .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetailResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        ProblemDetailResponse response = ProblemDetailResponse.builder()
            .type(URI.create("about:blank"))
            .title("Unauthorized")
            .status(HttpStatus.UNAUTHORIZED.value())
            .detail(ex.getMessage())
            .instance(URI.create(request.getRequestURI()))
            .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        ProblemDetailResponse response = ProblemDetailResponse.builder()
            .type(URI.create("about:blank"))
            .title("Bad Request")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail(details)
            .instance(URI.create(request.getRequestURI()))
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        ProblemDetailResponse response = ProblemDetailResponse.builder()
            .type(URI.create("about:blank"))
            .title("Internal Server Error")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .detail(ex.getMessage())
            .instance(URI.create(request.getRequestURI()))
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
