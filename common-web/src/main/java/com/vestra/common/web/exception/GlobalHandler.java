package com.vestra.common.web.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log log = LogFactory.getLog(GlobalHandler.class);

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException e) {
        ProblemDetail detail = ProblemDetail.forStatus(e.getHttpStatus().value());
        detail.setTitle(e.getTitle());
        detail.setDetail(e.getDetail());
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException e) {
        ProblemDetail detail = ProblemDetail.forStatus(400);
        detail.setTitle("Validation Error");
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        detail.setProperty("validationErrors", errors);
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception e) {
        log.error("Beklenmeyen sistem hatası: ", e);
        ProblemDetail detail = ProblemDetail.forStatus(500);
        detail.setTitle("Sunucu Hatası");
        detail.setDetail("Beklenmeyen bir sunucu hatası oluştu. Lütfen daha sonra tekrar deneyiniz.");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }
}
