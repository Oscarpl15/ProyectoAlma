package com.practicasalma.proyectoalma.exception;

/**
 * Excepción lanzada cuando ocurre un error técnico en la capa de acceso a datos (DAO).
 * <p>
 * Envuelve excepciones de JPA/Hibernate para desacoplar las capas superiores
 * de los detalles de la implementación de persistencia.
 * </p>
 * <p>
 * Si se captura esta excepción en un controller, debe mostrarse como un error
 * técnico genérico al usuario y registrarse para depuración.
 * </p>
 */
public class PersistenciaException extends AlmaException {

    /**
     * @param message descripción del error de base de datos
     */
    public PersistenciaException(String message) {
        super(message);
    }

    /**
     * @param message descripción del error de base de datos
     * @param cause   excepción JPA/Hibernate original
     */
    public PersistenciaException(String message, Throwable cause) {
        super(message, cause);
    }
}
