package com.practicasalma.proyectoalma.exception;

public class PersistenciaException extends AlmaException {

    public PersistenciaException(String message) {
        super(message);
    }

    public PersistenciaException(String message, Throwable cause) {
        super(message, cause);
    }
}
