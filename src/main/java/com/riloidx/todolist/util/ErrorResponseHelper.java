package com.riloidx.todolist.util;

import com.riloidx.todolist.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.stream.Collectors;

public class ErrorResponseHelper {
    public static ErrorResponse buildErrorResponse(Exception e,
                                                   HttpStatus httpStatus,
                                                   HttpServletRequest request) {
        return ErrorResponse.of(
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                e.getMessage(),
                request.getRequestURI()
        );
    }

    public static ErrorResponse buildValidationErrorResponse(MethodArgumentNotValidException e,
                                                             HttpServletRequest request) {
        return ErrorResponse.validation(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                request.getRequestURI(),
                getValidationErrors(e)
        );
    }

    public static Map<String, String> getValidationErrors(MethodArgumentNotValidException e) {
        return e.getBindingResult().getFieldErrors().stream()
                .filter(fe -> fe.getDefaultMessage() != null)
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing));
    }
}