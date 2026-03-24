package com.practicasalma.proyectoalma;

import com.practicasalma.proyectoalma.service.GeneradorPdfService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainPruebaPdf {

    public static void main(String[] args) {
        Path escritorio = Paths.get(System.getProperty("user.home"), "Desktop");
        Path plantilla = escritorio.resolve("modelo_certificado_donaciones_deafult.pdf");

        if (!Files.exists(plantilla)) {
            plantilla = escritorio.resolve("modelo_certificado_donaciones_default.pdf");
        }

        if (!Files.exists(plantilla)) {
            System.err.println("No se encontró la plantilla en el escritorio.");
            System.err.println("Busca uno de estos nombres:");
            System.err.println("- modelo_certificado_donaciones_deafult.pdf");
            System.err.println("- modelo_certificado_donaciones_default.pdf");
            return;
        }

        Path rutaSalida = escritorio.resolve("prueba.pdf");

        GeneradorPdfService generador = new GeneradorPdfService();
        generarConDatosInventados(generador, plantilla, rutaSalida);

        System.out.println("PDF de prueba generado en: " + rutaSalida);
    }

    private static void generarConDatosInventados(GeneradorPdfService generador, Path plantilla, Path salida) {
        generador.generarCertificadoDonacion(
                plantilla.toString(),
                salida.toString(),
                "2024",
                "Lucia Martin Gomez",
                "12345678Z",
                "Calle Mayor 45, Alcala de Henares",
                "doscientos cincuenta euros",
                "250",
                "18",
                "marzo",
                "2025"
        );
    }

    private static void generarConDatosEnOrden(GeneradorPdfService generador, Path plantilla, Path salida) {
        generador.generarCertificadoDonacion(
                plantilla.toString(),
                salida.toString(),
                "ANO_INICIAL",
                "NOMBRE",
                "DNI",
                "DOMICILIO",
                "CANTIDAD_LETRA",
                "CANTIDAD_NUMERO",
                "DIA",
                "MES",
                "ANO_FINAL"
        );
    }
}
