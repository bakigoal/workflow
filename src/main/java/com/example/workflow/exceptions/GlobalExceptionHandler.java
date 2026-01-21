package com.example.workflow.exceptions;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralExceptionContainer.class)
    public ErrorResponse handle(HttpServletResponse res, GeneralExceptionContainer ex) {
        log.error("General exception occurred: {}", ex.getMessage());
        res.setStatus(ex.getApiError().getCode().value());
        return createErrorResponse(ex.getApiError());
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handle(HttpServletResponse res, Exception ex) {
        log.error("Exception occurred: {}", ex.getMessage());
        var error = ApiError.ERROR_500;
        res.setStatus(error.getCode().value());
        return createErrorResponse(error);
    }

    private ErrorResponse createErrorResponse(ApiError apiError) {
        return new ErrorResponse(apiError.getErrorText());
    }
}
