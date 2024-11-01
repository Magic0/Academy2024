package com.mitocode.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

public class ValidationCustomException extends ResponseStatusException {

    private final Map<String, Object> errorResponse;

    public ValidationCustomException(HttpStatusCode status, Map<String, Object> errorResponse) {
        super(status);
        this.errorResponse = errorResponse;
    }

    public Map<String, Object> getErrorResponse() {
        return errorResponse;
    }
}

