package com.practicasalma.proyectoalma.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfStamper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class GeneradorPdfService {

    private static final String CAMPO_ANO_INICIAL = "ANO_INICIAL";
    private static final String CAMPO_NOMBRE = "NOMBRE";
    private static final String CAMPO_DNI = "DNI";
    private static final String CAMPO_DOMICILIO = "DOMICILIO";
    private static final String CAMPO_CANTIDAD_LETRA = "CANTIDAD_LETRA";
    private static final String CAMPO_CANTIDAD_NUMERO = "CANTIDAD_NUMERO";
    private static final String CAMPO_DIA = "DIA";
    private static final String CAMPO_MES = "MES";
    private static final String CAMPO_ANO_FINAL = "ANO_FINAL";

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
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(ruta.toFile());
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("Hola texto de prueba"));
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Error al generar el PDF de prueba.", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void generarCertificadoDonacion(
            String rutaPlantilla,
            String rutaSalida,
            String anoInicial,
            String nombre,
            String dni,
            String domicilio,
            String cantidadLetra,
            String cantidadNumero,
            String dia,
            String mes,
            String anoFinal
    ) {
        if (rutaPlantilla == null || rutaPlantilla.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta de la plantilla PDF es obligatoria.");
        }

        if (rutaSalida == null || rutaSalida.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta de salida del PDF es obligatoria.");
        }

        Path rutaDestino = Paths.get(rutaSalida.trim());

        try {
            Path carpetaPadre = rutaDestino.getParent();
            if (carpetaPadre != null) {
                Files.createDirectories(carpetaPadre);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de salida para el certificado.", e);
        }

        Map<String, String> valores = construirValoresReemplazo(
                anoInicial,
                nombre,
                dni,
                domicilio,
                cantidadLetra,
                cantidadNumero,
                dia,
                mes,
                anoFinal
        );

        try (PdfReader reader = new PdfReader(rutaPlantilla.trim());
             FileOutputStream outputStream = new FileOutputStream(rutaDestino.toFile());
             PdfStamper stamper = new PdfStamper(reader, outputStream)) {

            boolean reemplazoPorCampos = reemplazarPorAcroForm(stamper, valores);

            if (!reemplazoPorCampos) {
                reemplazarEnContenido(reader, valores);
            }

            stamper.setFormFlattening(true);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el certificado de donaciones.", e);
        }
    }

    private boolean reemplazarPorAcroForm(PdfStamper stamper, Map<String, String> valores)
            throws IOException, DocumentException {
        AcroFields form = stamper.getAcroFields();
        if (form == null || form.getFields().isEmpty()) {
            return false;
        }

        boolean huboReemplazo = false;

        for (Map.Entry<String, String> entrada : valores.entrySet()) {
            if (form.getFieldItem(entrada.getKey()) != null) {
                form.setField(entrada.getKey(), entrada.getValue());
                huboReemplazo = true;
            }
        }

        return huboReemplazo;
    }

    private void reemplazarEnContenido(PdfReader reader, Map<String, String> valores) throws IOException {
        int totalPaginas = reader.getNumberOfPages();

        for (int pagina = 1; pagina <= totalPaginas; pagina++) {
            byte[] contenidoOriginal = reader.getPageContent(pagina);
            if (contenidoOriginal == null || contenidoOriginal.length == 0) {
                continue;
            }

            String texto = new String(contenidoOriginal, StandardCharsets.ISO_8859_1);
            String textoReemplazado = texto;

            for (Map.Entry<String, String> entrada : valores.entrySet()) {
                textoReemplazado = textoReemplazado.replace(entrada.getKey(), escaparPdfLiteral(entrada.getValue()));
            }

            if (!Objects.equals(texto, textoReemplazado)) {
                PdfDictionary paginaDic = reader.getPageN(pagina);
                PRStream streamNuevo = new PRStream(reader, textoReemplazado.getBytes(StandardCharsets.ISO_8859_1));
                streamNuevo.flateCompress();
                paginaDic.put(PdfName.CONTENTS, streamNuevo);
            }
        }
    }

    private String normalizarValor(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.trim();
    }

    private Map<String, String> construirValoresReemplazo(
            String anoInicial,
            String nombre,
            String dni,
            String domicilio,
            String cantidadLetra,
            String cantidadNumero,
            String dia,
            String mes,
            String anoFinal
    ) {
        String valorAnoInicial = normalizarValor(anoInicial);
        String valorNombre = normalizarValor(nombre);
        String valorDni = normalizarValor(dni);
        String valorDomicilio = normalizarValor(domicilio);
        String valorCantidadLetra = normalizarValor(cantidadLetra);
        String valorCantidadNumero = normalizarValor(cantidadNumero);
        String valorDia = normalizarValor(dia);
        String valorMes = normalizarValor(mes);
        String valorAnoFinal = normalizarValor(anoFinal);

        Map<String, String> valores = new LinkedHashMap<>();

        agregarAlias(valores, valorAnoInicial,
                CAMPO_ANO_INICIAL,
                "ANO_INICAL",
                "ANO_INICIO",
                "ANO_INCIO",
                "ANO INICIAL",
                "ANO INICIO",
                "A\u00D1O_INICIAL",
                "A\u00D1O_INICAL",
                "A\u00D1O_INICIO",
                "A\u00D1O_INCIO",
                "A\u00D1O INICIAL",
                "A\u00D1O INICIO"
        );

        agregarAlias(valores, valorNombre, CAMPO_NOMBRE);
        agregarAlias(valores, valorDni, CAMPO_DNI);
        agregarAlias(valores, valorDomicilio, CAMPO_DOMICILIO);

        agregarAlias(valores, valorCantidadLetra,
                CAMPO_CANTIDAD_LETRA,
                "CANTIDAD EN LETRA",
                "CANTIDAD_LETRA"
        );

        agregarAlias(valores, valorCantidadNumero,
                CAMPO_CANTIDAD_NUMERO,
                "CANTIDAD EN NUMERO",
                "CANTIDAD EN N\u00DAMERO",
                "CANTIDAD_NUMERO"
        );

        agregarAlias(valores, valorDia, CAMPO_DIA);
        agregarAlias(valores, valorMes, CAMPO_MES);

        agregarAlias(valores, valorAnoFinal,
                CAMPO_ANO_FINAL,
                "ANO FINAL",
                "A\u00D1O_FINAL",
                "A\u00D1O FINAL"
        );

        return valores;
    }

    private void agregarAlias(Map<String, String> valores, String valor, String... aliases) {
        for (String alias : aliases) {
            valores.put(alias, valor);
        }
    }

    private String escaparPdfLiteral(String valor) {
        return valor
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }
}
