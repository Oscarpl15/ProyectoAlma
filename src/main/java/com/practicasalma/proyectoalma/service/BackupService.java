package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.util.config.GestorConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class BackupService {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public boolean esCopiaDebida() {
        int frecuencia = GestorConfig.getBackupFrecuencia();
        if (frecuencia <= 0) return false;
        String ultimaFecha = GestorConfig.getBackupUltimaFecha();
        if (ultimaFecha == null || ultimaFecha.isBlank()) return true;
        try {
            LocalDate ultima = LocalDate.parse(ultimaFecha, FORMATO);
            return !LocalDate.now().isBefore(ultima.plusDays(frecuencia));
        } catch (Exception e) {
            return true;
        }
    }

    public void realizarCopia(String destino) throws IOException {
        File dirDestino = new File(destino);
        borrarCopiasAnteriores(dirDestino);

        String fechaHoy = LocalDate.now().format(FORMATO);
        File carpetaCopia = new File(dirDestino, "CopiaSeguridad_" + fechaHoy);
        carpetaCopia.mkdirs();

        String rutaBBDD = GestorConfig.getRutaBBDD();
        if (rutaBBDD != null && !rutaBBDD.isBlank()) {
            File carpetaBBDD = new File(rutaBBDD).getParentFile();
            if (carpetaBBDD != null && carpetaBBDD.exists()) {
                copiarCarpeta(carpetaBBDD.toPath(), carpetaCopia.toPath().resolve(carpetaBBDD.getName()));
            }
        }

        String rutaDocs = GestorConfig.getRutaDocumentos();
        if (rutaDocs != null && !rutaDocs.isBlank()) {
            File carpetaDocs = new File(rutaDocs);
            if (carpetaDocs.exists()) {
                copiarCarpeta(carpetaDocs.toPath(), carpetaCopia.toPath().resolve(carpetaDocs.getName()));
            }
        }
    }

    public void registrarFechaHoy() {
        GestorConfig.setBackupUltimaFecha(LocalDate.now().format(FORMATO));
    }

    private void borrarCopiasAnteriores(File dirDestino) throws IOException {
        File[] anteriores = dirDestino.listFiles(f -> f.isDirectory() && f.getName().startsWith("CopiaSeguridad_"));
        if (anteriores == null) return;
        for (File f : anteriores) {
            Files.walk(f.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private void copiarCarpeta(Path origen, Path destino) throws IOException {
        try {
            Files.walk(origen).forEach(src -> {
                Path dst = destino.resolve(origen.relativize(src));
                try {
                    if (Files.isDirectory(src)) {
                        Files.createDirectories(dst);
                    } else {
                        Files.createDirectories(dst.getParent());
                        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioe) throw ioe;
            throw e;
        }
    }
}
