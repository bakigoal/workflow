package com.example.workflow.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApiError {
    ERROR_500(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR"),
    ERROR_TRANSFER_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "Не найден переход на следующий шаг"),
    ;

    private final HttpStatus code;
    private final String errorText;
}
