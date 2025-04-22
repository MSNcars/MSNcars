package com.msn.msncars.exception.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
    Override default behaviour of spring boot to return more fine-grained errors - for example when validation of
    object annotated with @Valid fails.
 */
@ControllerAdvice
public class ValidationAndConstraintExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request
    ) {

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<ValidationError> validationErrors = new ArrayList<>();

        for (FieldError fieldError : fieldErrors) {
            validationErrors.add(new ValidationError(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        ValidationErrorResponse response = new ValidationErrorResponse(validationErrors);

        return handleExceptionInternal(e, response, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e, WebRequest request) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        List<ValidationError> validationErrors = new ArrayList<>();

        for (ConstraintViolation<?> violation : constraintViolations) {
            validationErrors.add(new ValidationError(violation.getPropertyPath().toString(), violation.getMessage()));
        }

        ValidationErrorResponse response = new ValidationErrorResponse(validationErrors);

        return handleExceptionInternal(e, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

}
