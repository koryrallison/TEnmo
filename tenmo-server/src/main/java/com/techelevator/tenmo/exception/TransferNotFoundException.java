package com.techelevator.tenmo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Transfer Not Found")
public class TransferNotFoundException extends Exception {
    public TransferNotFoundException() {
        super("Transfer Not Found");
    }
}
