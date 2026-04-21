package com.practicasalma.proyectoalma.util;

import javafx.scene.control.Alert;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class GestorDocumentos {

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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private static void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Documento añadido");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}