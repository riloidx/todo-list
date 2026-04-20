package com.riloidx.todolist.exception;


import com.riloidx.todolist.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.riloidx.todolist.util.ErrorResponseHelper.buildErrorResponse;
import static com.riloidx.todolist.util.ErrorResponseHelper.buildValidationErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                                               HttpServletRequest request) {
        log.warn("Validation error on request to {}: {}", request.getRequestURI(), e.getMessage());
        var body = buildValidationErrorResponse(e, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(TaskNotFoundException e,
                                                                HttpServletRequest request) {
        log.warn("Resource not found on {}: {}", request.getRequestURI(), e.getMessage());
        var body = buildErrorResponse(e, HttpStatus.NOT_FOUND, request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(TaskAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(TaskAlreadyExistsException e,
                                                                     HttpServletRequest request) {
        log.warn("Resource already exists on {}: {}", request.getRequestURI(), e.getMessage());
        var body = buildErrorResponse(e, HttpStatus.CONFLICT, request);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalStateException e,
                                                                        HttpServletRequest request) {
        log.warn("Illegal state on {}: {}", request.getRequestURI(), e.getMessage());
        var body = buildErrorResponse(e, HttpStatus.UNAUTHORIZED, request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(TaskAlreadyInStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(TaskAlreadyInStateException e,
                                                                        HttpServletRequest request) {
        log.warn("Bad request state on {}: {}", request.getRequestURI(), e.getMessage());
        var body = buildErrorResponse(e, HttpStatus.BAD_REQUEST, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(AccessDeniedException e,
                                                                     HttpServletRequest request) {
        log.warn("Access denied on {}: {}", request.getRequestURI(), e.getMessage());
        var body = buildErrorResponse(e, HttpStatus.FORBIDDEN, request);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e,
                                                         HttpServletRequest request) {
        log.error("Internal server error on {}: {}", request.getRequestURI(), e.getMessage(), e);
        var body = buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR, request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
