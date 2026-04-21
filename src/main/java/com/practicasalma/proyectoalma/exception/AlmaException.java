package com.practicasalma.proyectoalma.exception;

public class AlmaException extends RuntimeException {

    public AlmaException(String message) {
        super(message);
    }

    public AlmaException(String message, Throwable cause) {
        super(message, cause);
    }
}
