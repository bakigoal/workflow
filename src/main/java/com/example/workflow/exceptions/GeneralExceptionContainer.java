package com.example.workflow.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GeneralExceptionContainer extends RuntimeException {

    private final ApiError apiError;

    @Override
    public String getMessage() {
        return apiError.getCode().getReasonPhrase() + ", " + apiError.getErrorText();
    }
}
