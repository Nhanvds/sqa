package com.doan.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class Unauthorized extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public Unauthorized(String message) {
        super(message);
    }
}