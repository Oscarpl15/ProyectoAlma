package com.practicasalma.proyectoalma.exception;

/**
 * Excepción lanzada cuando falla una operación sobre el sistema de ficheros de documentos.
 * <p>
 * Ejemplos: error al copiar una foto de perfil, error al mover el contenido del directorio
 * de documentos a una nueva ubicación.
 * </p>
 */
public class DocumentoException extends AlmaException {

    /**
     * @param message descripción del error de fichero
     */
    public DocumentoException(String message) {
        super(message);
    }

    /**
     * @param message descripción del error de fichero
     * @param cause   excepción de E/S original
     */
    public DocumentoException(String message, Throwable cause) {
        super(message, cause);
    }
}
