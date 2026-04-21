package com.practicasalma.proyectoalma.exception;

/**
 * Excepción base de la aplicación Fundación Alma.
 * <p>
 * Todas las excepciones de negocio y de infraestructura del proyecto heredan de esta clase,
 * lo que permite capturarlas todas con un único bloque {@code catch (AlmaException e)} cuando
 * sea necesario manejar cualquier error propio de la aplicación.
 * </p>
 * <p>
 * Extiende {@link RuntimeException} para evitar la obligación de declarar {@code throws}
 * en cada método de la cadena de llamadas (unchecked exception).
 * </p>
 */
public class AlmaException extends RuntimeException {

    /**
     * Crea una excepción con un mensaje descriptivo.
     *
     * @param message descripción del error
     */
    public AlmaException(String message) {
        super(message);
    }

    /**
     * Crea una excepción con mensaje y causa raíz.
     *
     * @param message descripción del error
     * @param cause   excepción original que provocó este error
     */
    public AlmaException(String message, Throwable cause) {
        super(message, cause);
    }
}
