package com.practicasalma.proyectoalma.util.doc;

import com.practicasalma.proyectoalma.exception.DocumentoException;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import javafx.scene.control.Alert;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

/**
 * Utilidades para gestionar los documentos y ficheros asociados a las entidades del sistema.
 * <p>
 * La estructura de directorios es:
 * <pre>
 * [ruta.documentos]/
 *   Alumnos/
 *     Juan García López/
 *       foto.jpg
 *       informe.pdf
 *   Docentes/
 *     ...
 *   Socios/
 *     ...
 *   Voluntarios/
 *     ...
 * </pre>
 * La ruta raíz se lee de {@link GestorConfig#getRutaDocumentos()}.
 * Si no está configurada, se muestra un diálogo de error y la operación se aborta.
 * </p>
 * <p>
 * Singleton estático — no debe instanciarse.
 * </p>
 */
public class GestorDocumentos {

    private GestorDocumentos() {}

    /**
     * Copia un fichero al directorio de la entidad indicada, creándolo si no existe.
     *
     * @param archivo   fichero origen a copiar
     * @param tipo      categoría de entidad: {@code "Alumnos"}, {@code "Docentes"}, etc.
     * @param nombre    nombre de la persona (para construir el subdirectorio)
     * @param apellidos apellidos de la persona
     */
    public static void copiarDocumento(File archivo, String tipo, String nombre, String apellidos) {
        File dir = resolverDirectorio(tipo, nombre, apellidos);
        if (dir == null) return;

        File destino = new File(dir, archivo.getName());
        try {
            Files.copy(archivo.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            mostrarInfo("Documento añadido en:\n" + dir.getAbsolutePath());
        } catch (IOException e) {
            mostrarError("No se pudo copiar el documento: " + e.getMessage());
        }
    }

    /**
     * Abre el directorio de documentos de la entidad en el explorador del sistema operativo.
     * Crea el directorio si no existía.
     *
     * @param tipo      categoría de entidad
     * @param nombre    nombre de la persona
     * @param apellidos apellidos de la persona
     */
    public static void abrirDirectorio(String tipo, String nombre, String apellidos) {
        File dir = resolverDirectorio(tipo, nombre, apellidos);
        if (dir == null) return;

        if (!dir.exists()) dir.mkdirs();

        try {
            Desktop.getDesktop().open(dir);
        } catch (IOException e) {
            mostrarError("No se pudo abrir el directorio: " + e.getMessage());
        }
    }

    /**
     * Mueve todo el contenido de un directorio a otro, borrando el origen tras la copia.
     * <p>
     * Se usa cuando el usuario cambia el directorio de documentos y elige mover los ficheros existentes.
     * Si algún fichero individual falla, se lanza {@link DocumentoException}.
     * </p>
     *
     * @param origen  directorio fuente (debe existir y ser un directorio)
     * @param destino directorio destino (se crea si no existe)
     */
    public static void moverContenido(File origen, File destino) {
        if (!origen.exists() || !origen.isDirectory()) return;
        try {
            Files.walk(origen.toPath())
                    .forEach(src -> {
                        Path dst = destino.toPath().resolve(origen.toPath().relativize(src));
                        try {
                            if (Files.isDirectory(src)) {
                                Files.createDirectories(dst);
                            } else {
                                Files.createDirectories(dst.getParent());
                                Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            throw new DocumentoException("Error moviendo " + src.getFileName() + ": " + e.getMessage(), e);
                        }
                    });
            // Borrar el árbol de directorios de origen en orden inverso (hijos antes que padres)
            Files.walk(origen.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            mostrarError("Error al mover el contenido: " + e.getMessage());
        }
    }

    /**
     * Comprueba si un directorio existe y tiene al menos un fichero o subdirectorio.
     *
     * @param dir directorio a comprobar
     * @return {@code true} si el directorio existe y no está vacío
     */
    public static boolean tieneContenido(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return false;
        String[] contenido = dir.list();
        return contenido != null && contenido.length > 0;
    }

    /**
     * Devuelve el {@link File} correspondiente al directorio de la entidad,
     * creándolo si no existía.
     *
     * @param tipo      categoría de entidad
     * @param nombre    nombre de la persona
     * @param apellidos apellidos de la persona
     * @return directorio de la entidad, o {@code null} si el directorio raíz no está configurado
     */
    public static File getDirectorio(String tipo, String nombre, String apellidos) {
        return resolverDirectorio(tipo, nombre, apellidos);
    }

    // Construye la ruta completa del directorio y lo crea si no existe.
    private static File resolverDirectorio(String tipo, String nombre, String apellidos) {
        String rutaDocs = GestorConfig.getRutaDocumentos();
        if (rutaDocs == null || rutaDocs.isBlank()) {
            mostrarError("No hay directorio de documentos configurado.\nConfigúralo en Ajustes → Cambiar directorio documentos.");
            return null;
        }

        String nombreCarpeta = (nombre + " " + apellidos).trim();
        File dir = new File(rutaDocs, tipo + File.separator + nombreCarpeta);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private static void mostrarError(String mensaje) {
        com.practicasalma.proyectoalma.util.ui.FxUtils.mostrarAlerta(
                javafx.scene.control.Alert.AlertType.ERROR, "Error", mensaje);
    }

    private static void mostrarInfo(String mensaje) {
        com.practicasalma.proyectoalma.util.ui.FxUtils.mostrarAlerta(
                javafx.scene.control.Alert.AlertType.INFORMATION, "Documento añadido", mensaje);
    }
}
