package com.practicasalma.proyectoalma.exception;

/**
 * Excepción lanzada cuando se intenta registrar una entidad que ya existe en la base de datos.
 * <p>
 * Ejemplo: intentar dar de alta a un alumno con un DNI que ya está registrado
 * para otro alumno en la misma tabla.
 * </p>
 * <p>
 * Nota: el mismo DNI puede coexistir en diferentes tablas (p. ej., un Socio y un Docente
 * pueden compartir DNI). Esta excepción sólo aplica a duplicados dentro del mismo tipo de entidad.
 * </p>
 */
public class EntidadDuplicadaException extends AlmaException {

    /**
     * @param message descripción del duplicado detectado
     */
    public EntidadDuplicadaException(String message) {
        super(message);
    }
}
