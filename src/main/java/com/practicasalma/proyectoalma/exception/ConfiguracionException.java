package com.practicasalma.proyectoalma.exception;

/**
 * Excepción lanzada cuando la aplicación no está correctamente configurada.
 * <p>
 * Casos de uso:
 * <ul>
 *   <li>La base de datos no ha sido inicializada antes de intentar usarla.</li>
 *   <li>No se puede leer o escribir el fichero de configuración {@code ~/.alma/config.properties}.</li>
 *   <li>La ruta configurada para la BD o los documentos no es válida.</li>
 * </ul>
 * </p>
 */
public class ConfiguracionException extends AlmaException {

    /**
     * @param message descripción del problema de configuración
     */
    public ConfiguracionException(String message) {
        super(message);
    }

    /**
     * @param message descripción del problema de configuración
     * @param cause   excepción técnica original
     */
    public ConfiguracionException(String message, Throwable cause) {
        super(message, cause);
    }
}
