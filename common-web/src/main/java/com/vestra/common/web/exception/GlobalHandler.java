package com.vestra.common.web.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler
    public ProblemDetail handleApiException(ApiException e){
        ProblemDetail detail = ProblemDetail.forStatus(e.getHttpStatus().value());
        detail.setTitle(e.getTitle());
        detail.setDetail(e.getDetail());
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    @ExceptionHandler
    public ProblemDetail handleValidationException(MethodArgumentNotValidException e){
        ProblemDetail detail = ProblemDetail.forStatus(400);
        detail.setTitle("Validation Error");
        Map<String,String> errors = new HashMap<>();
        for (FieldError error: e.getBindingResult().getFieldErrors()){
            errors.put(error.getField(),error.getDefaultMessage());
        }
        detail.setProperty("validationErrors",errors);
        return detail;
    }
}
