package com.tech4um.projectWS.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)// Mapeia para 409
public class ConflictException extends RuntimeException {

    public ConflictException(String msg) {
        super(msg);
    }
}
