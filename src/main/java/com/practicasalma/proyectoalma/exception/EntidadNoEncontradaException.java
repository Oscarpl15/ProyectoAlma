package com.practicasalma.proyectoalma.exception;

/**
 * Excepción lanzada cuando se busca una entidad por su ID en la base de datos y no existe.
 * <p>
 * Se usa en los DAOs cuando {@code em.find()} devuelve {@code null}, garantizando
 * que ninguna capa superior recibe un {@code null} inesperado.
 * </p>
 */
public class EntidadNoEncontradaException extends AlmaException {

    /**
     * @param message descripción de la entidad no encontrada (p. ej., "Alumno con id 5 no encontrado.")
     */
    public EntidadNoEncontradaException(String message) {
        super(message);
    }
}
