package com.practicasalma.proyectoalma.exception;

/**
 * Excepción lanzada cuando los datos introducidos no superan una validación de formato
 * o una regla de negocio.
 * <p>
 * Ejemplos de uso: DNI con formato incorrecto, teléfono sin 9 dígitos,
 * fecha de nacimiento futura, IBAN inválido.
 * </p>
 * <p>
 * Debe capturarse en el controller para mostrar al usuario un mensaje de advertencia
 * sin registrar el error como fallo técnico.
 * </p>
 */
public class ValidacionException extends AlmaException {

    /**
     * Crea la excepción con el mensaje que se mostrará al usuario.
     *
     * @param message descripción del dato inválido
     */
    public ValidacionException(String message) {
        super(message);
    }
}
