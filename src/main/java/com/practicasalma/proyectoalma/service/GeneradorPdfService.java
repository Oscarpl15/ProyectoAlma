package com.practicasalma.proyectoalma.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GeneradorPdfService {

    public void generarPdfPrueba(String rutaSalida) {
        if (rutaSalida == null || rutaSalida.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta de salida del PDF es obligatoria.");
        }

        Path ruta = Paths.get(rutaSalida.trim());

        try {
            Path carpetaPadre = ruta.getParent();
            if (carpetaPadre != null) {
                Files.createDirectories(carpetaPadre);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de salida para el PDF.", e);
        }

        Document document = new Document();
        try (FileOutputStream outputStream = new FileOutputStream(ruta.toFile())) {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("Hola texto de prueba"));
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Error al generar el PDF de prueba.", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }
}
