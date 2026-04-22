package com.practicasalma.proyectoalma.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfStamper;
import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Socio;

import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
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
    private static final String NOMBRE_PLANTILLA_1 = "modelo_certificado_donaciones_deafult.pdf";
    private static final String NOMBRE_PLANTILLA_2 = "modelo_certificado_donaciones_default.pdf";

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

    public void generarPdfGrafica(String rutaSalida, String titulo, javafx.scene.image.Image grafica, List<String> resumen) {
        if (rutaSalida == null || rutaSalida.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta de salida del PDF es obligatoria.");
        }
        if (grafica == null || grafica.getWidth() <= 0 || grafica.getHeight() <= 0) {
            throw new IllegalArgumentException("La imagen de la grafica es obligatoria.");
        }

        Path ruta = Paths.get(rutaSalida.trim());

        try {
            Path carpetaPadre = ruta.getParent();
            if (carpetaPadre != null) {
                Files.createDirectories(carpetaPadre);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de salida para el PDF de grafica.", e);
        }

        Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(ruta.toFile());
            PdfWriter.getInstance(document, outputStream);
            document.open();

            String tituloSeguro = (titulo == null || titulo.trim().isEmpty()) ? "Grafica" : titulo.trim();
            document.add(new Paragraph("INFORME VISUAL"));
            document.add(new Paragraph("Titulo: " + tituloSeguro));
            document.add(new Paragraph("Fecha: " + LocalDate.now()));
            document.add(new Paragraph(" "));

            byte[] graficaPng = convertirImagenPng(grafica);
            Image imagenPdf = Image.getInstance(graficaPng);

            float anchoMaximo = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
            float altoMaximo = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin() - 140f;
            imagenPdf.scaleToFit(anchoMaximo, altoMaximo);
            imagenPdf.setAlignment(Image.MIDDLE);
            document.add(imagenPdf);

            if (resumen != null && !resumen.isEmpty()) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Resumen:"));
                for (String linea : resumen) {
                    if (linea != null && !linea.trim().isEmpty() && !"-".equals(linea.trim())) {
                        document.add(new Paragraph("- " + linea.trim()));
                    }
                }
            }
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Error al generar el PDF de la grafica.", e);
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

    private byte[] convertirImagenPng(javafx.scene.image.Image imagen) throws IOException {
        BufferedImage bufferedImage = convertirImagenBuffered(imagen);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }

    private BufferedImage convertirImagenBuffered(javafx.scene.image.Image imagen) {
        int ancho = (int) Math.round(imagen.getWidth());
        int alto = (int) Math.round(imagen.getHeight());
        BufferedImage buffered = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        javafx.scene.image.PixelReader pixelReader = imagen.getPixelReader();
        if (pixelReader == null) {
            throw new IllegalArgumentException("No se pudo leer la imagen de la grafica.");
        }

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                buffered.setRGB(x, y, pixelReader.getArgb(x, y));
            }
        }
        return buffered;
    }

    public void generarInformeSocio(String rutaSalida, Socio socio) {
        if (rutaSalida == null || rutaSalida.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta de salida del PDF es obligatoria.");
        }
        if (socio == null) {
            throw new IllegalArgumentException("El socio es obligatorio para generar el informe.");
        }

        Path ruta = Paths.get(rutaSalida.trim());

        try {
            Path carpetaPadre = ruta.getParent();
            if (carpetaPadre != null) {
                Files.createDirectories(carpetaPadre);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de salida para el informe.", e);
        }

        Document document = new Document();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(ruta.toFile());
            PdfWriter.getInstance(document, outputStream);
            document.open();

            document.add(new Paragraph("INFORME DE SOCIO"));
            document.add(new Paragraph("Fecha: " + LocalDate.now()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Nombre: " + valorSeguro(socio.getNombre())));
            document.add(new Paragraph("Apellidos: " + valorSeguro(socio.getApellidos())));
            document.add(new Paragraph("DNI/NIF: " + valorSeguro(socio.getDocumentoIdentidad())));
            document.add(new Paragraph("Correo: " + valorSeguro(socio.getCorreo())));
            document.add(new Paragraph("Telefono: " + valorSeguro(socio.getTelefono())));
            document.add(new Paragraph("Direccion: " + valorSeguro(socio.getDireccion())));
            document.add(new Paragraph("Tipo entidad: " + valorSeguro(socio.getTipoEntidad())));
            document.add(new Paragraph("Periodicidad: " + valorSeguro(socio.getPeriodicidad())));
            document.add(new Paragraph("Cuota: " + (socio.getCuota() != null ? socio.getCuota() + " EUR" : "-")));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Donaciones:"));

            if (socio.getDonaciones() == null || socio.getDonaciones().isEmpty()) {
                document.add(new Paragraph("- Sin donaciones registradas"));
            } else {
                for (Donacion donacion : socio.getDonaciones()) {
                    String fecha = donacion.getFecha() != null ? donacion.getFecha().toString() : "-";
                    String importe = donacion.getImporte() != null ? donacion.getImporte().toPlainString() + " EUR" : "-";
                    String forma = valorSeguro(donacion.getFormaDonacion());
                    document.add(new Paragraph("- " + fecha + " | " + importe + " | " + forma));
                }
            }
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Error al generar el informe PDF del socio.", e);
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

    public void generarInformeSocioConPlantilla(String rutaSalida, Socio socio) {
        if (socio == null) {
            throw new IllegalArgumentException("El socio es obligatorio para generar el informe.");
        }

        String rutaPlantilla = resolverRutaPlantillaCertificado();

        List<Donacion> donaciones = socio.getDonaciones();
        BigDecimal totalDonado = BigDecimal.ZERO;
        int anoInicial = LocalDate.now().getYear();

        if (donaciones != null && !donaciones.isEmpty()) {
            for (Donacion donacion : donaciones) {
                if (donacion.getImporte() != null) {
                    totalDonado = totalDonado.add(donacion.getImporte());
                }
            }

            Donacion masReciente = donaciones.stream()
                    .filter(d -> d.getFecha() != null)
                    .max(Comparator.comparing(Donacion::getFecha))
                    .orElse(null);

            if (masReciente != null) {
                anoInicial = masReciente.getFecha().getYear();
            }
        }

        LocalDate hoy = LocalDate.now();
        String nombreCompleto = (valorSeguro(socio.getNombre()) + " " + valorSeguro(socio.getApellidos())).trim();
        String domicilio = construirDomicilioObligatorio(socio);
        BigDecimal cantidadInforme = obtenerCantidadInforme(totalDonado, socio.getCuota());
        String cantidadNumero = formatearCantidad(cantidadInforme);
        String cantidadLetra = convertirImporteALetras(cantidadInforme);

        generarCertificadoDonacion(
                rutaPlantilla,
                rutaSalida,
                String.valueOf(anoInicial),
                nombreCompleto,
                valorSeguro(socio.getDocumentoIdentidad()),
                domicilio,
                cantidadNumero,
                cantidadLetra,
                String.valueOf(hoy.getDayOfMonth()),
                hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toUpperCase(Locale.ROOT),
                String.valueOf(hoy.getYear())
        );
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
                "CANTIDAD EN N\u00DAMERO€",
                "CANTIDAD EN NUMERO€",
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

    private String valorSeguro(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "-";
        }
        return valor.trim();
    }

    private String construirDomicilio(Socio socio) {
        String direccion = socio.getDireccion() != null ? socio.getDireccion().trim() : "";
        String ciudad = socio.getCiudad() != null ? socio.getCiudad().trim() : "";
        String cp = socio.getCodigoPostal() != null ? socio.getCodigoPostal().trim() : "";

        StringBuilder sb = new StringBuilder();
        if (!direccion.isEmpty()) {
            sb.append(direccion);
        }
        if (!cp.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(cp);
        }
        if (!ciudad.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(ciudad);
        }

        String resultado = sb.toString().trim();
        return resultado.isEmpty() ? "-" : resultado;
    }

    private String construirDomicilioObligatorio(Socio socio) {
        String domicilio = construirDomicilio(socio);
        if ("-".equals(domicilio) || domicilio.isBlank()) {
            throw new IllegalArgumentException("El domicilio del socio es obligatorio para generar el informe.");
        }
        return domicilio;
    }

    private BigDecimal obtenerCantidadInforme(BigDecimal totalDonado, Double cuotaSocio) {
        BigDecimal cantidad = totalDonado != null ? totalDonado : BigDecimal.ZERO;
        if (cantidad.compareTo(BigDecimal.ZERO) > 0) {
            return cantidad;
        }

        if (cuotaSocio != null && cuotaSocio > 0) {
            return BigDecimal.valueOf(cuotaSocio);
        }

        return BigDecimal.ZERO;
    }

    private String formatearCantidad(BigDecimal cantidad) {
        if (cantidad == null) {
            return "0";
        }
        return cantidad.stripTrailingZeros().toPlainString();
    }

    private String convertirImporteALetras(BigDecimal importe) {
        if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
            return "cero euros";
        }

        BigDecimal normalizado = importe.setScale(2, RoundingMode.HALF_UP);
        BigInteger euros = normalizado.setScale(0, RoundingMode.DOWN).toBigInteger();
        int centimos = normalizado.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        String letrasEuros = ajustarUnoAntesDeSustantivo(numeroALetras(euros));
        String moneda = BigInteger.ONE.equals(euros) ? "euro" : "euros";

        if (centimos > 0) {
            return letrasEuros + " " + moneda + " con " + String.format("%02d", centimos);
        }

        return letrasEuros + " " + moneda;
    }

    private String ajustarUnoAntesDeSustantivo(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }

        if ("uno".equals(texto)) {
            return "un";
        }

        if (texto.endsWith("veintiuno")) {
            return texto.substring(0, texto.length() - "veintiuno".length()) + "veintiun";
        }

        if (texto.endsWith(" y uno")) {
            return texto.substring(0, texto.length() - " y uno".length()) + " y un";
        }

        if (texto.endsWith(" uno")) {
            return texto.substring(0, texto.length() - " uno".length()) + " un";
        }

        return texto;
    }

    private String numeroALetras(BigInteger numero) {
        if (numero == null || BigInteger.ZERO.equals(numero)) {
            return "cero";
        }

        if (numero.signum() < 0) {
            return "menos " + numeroALetras(numero.abs());
        }

        List<Integer> grupos = new ArrayList<>();
        BigInteger base = BigInteger.valueOf(1000);
        BigInteger actual = numero;

        while (actual.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] division = actual.divideAndRemainder(base);
            grupos.add(division[1].intValue());
            actual = division[0];
        }

        StringBuilder resultado = new StringBuilder();
        for (int i = grupos.size() - 1; i >= 0; i--) {
            int grupo = grupos.get(i);
            if (grupo == 0) {
                continue;
            }

            if (!resultado.isEmpty()) {
                resultado.append(" ");
            }

            if (i == 0) {
                resultado.append(numeroMenorMilALetras(grupo));
                continue;
            }

            if (i == 1) {
                if (grupo == 1) {
                    resultado.append("mil");
                } else {
                    resultado.append(ajustarUnoAntesDeSustantivo(numeroMenorMilALetras(grupo))).append(" mil");
                }
                continue;
            }

            if ((i % 2) == 1) {
                String escalaParPlural = escalaPlural(i - 1);
                if (grupo == 1) {
                    resultado.append("mil ").append(escalaParPlural);
                } else {
                    resultado.append(ajustarUnoAntesDeSustantivo(numeroMenorMilALetras(grupo)))
                            .append(" mil ")
                            .append(escalaParPlural);
                }
                continue;
            }

            if (grupo == 1) {
                resultado.append("un ").append(escalaSingular(i));
            } else {
                resultado.append(ajustarUnoAntesDeSustantivo(numeroMenorMilALetras(grupo)))
                        .append(" ")
                        .append(escalaPlural(i));
            }
        }

        return resultado.toString();
    }

    private String numeroMenorMilALetras(int numero) {
        if (numero <= 0 || numero >= 1000) {
            throw new IllegalArgumentException("El numero debe estar entre 1 y 999.");
        }

        if (numero <= 29) {
            return switch (numero) {
                case 1 -> "uno";
                case 2 -> "dos";
                case 3 -> "tres";
                case 4 -> "cuatro";
                case 5 -> "cinco";
                case 6 -> "seis";
                case 7 -> "siete";
                case 8 -> "ocho";
                case 9 -> "nueve";
                case 10 -> "diez";
                case 11 -> "once";
                case 12 -> "doce";
                case 13 -> "trece";
                case 14 -> "catorce";
                case 15 -> "quince";
                case 16 -> "dieciseis";
                case 17 -> "diecisiete";
                case 18 -> "dieciocho";
                case 19 -> "diecinueve";
                case 20 -> "veinte";
                case 21 -> "veintiuno";
                case 22 -> "veintidos";
                case 23 -> "veintitres";
                case 24 -> "veinticuatro";
                case 25 -> "veinticinco";
                case 26 -> "veintiseis";
                case 27 -> "veintisiete";
                case 28 -> "veintiocho";
                case 29 -> "veintinueve";
                default -> "";
            };
        }

        if (numero < 100) {
            int decena = numero / 10;
            int unidad = numero % 10;
            String textoDecena = switch (decena) {
                case 3 -> "treinta";
                case 4 -> "cuarenta";
                case 5 -> "cincuenta";
                case 6 -> "sesenta";
                case 7 -> "setenta";
                case 8 -> "ochenta";
                case 9 -> "noventa";
                default -> "";
            };
            return unidad == 0 ? textoDecena : textoDecena + " y " + numeroMenorMilALetras(unidad);
        }

        if (numero == 100) {
            return "cien";
        }

        int centena = numero / 100;
        int resto = numero % 100;
            String textoCentena = switch ((int) centena) {
                case 1 -> "ciento";
                case 2 -> "doscientos";
                case 3 -> "trescientos";
                case 4 -> "cuatrocientos";
                case 5 -> "quinientos";
                case 6 -> "seiscientos";
                case 7 -> "setecientos";
                case 8 -> "ochocientos";
                case 9 -> "novecientos";
                default -> "";
            };
        return resto == 0 ? textoCentena : textoCentena + " " + numeroMenorMilALetras(resto);
    }

    private String escalaSingular(int indiceGrupo) {
        return switch (indiceGrupo) {
            case 2 -> "millon";
            case 4 -> "billon";
            case 6 -> "trillon";
            case 8 -> "cuatrillon";
            case 10 -> "quintillon";
            case 12 -> "sextillon";
            case 14 -> "septillon";
            case 16 -> "octillon";
            case 18 -> "nonillon";
            case 20 -> "decillon";
            default -> "10^" + (indiceGrupo * 3);
        };
    }

    private String escalaPlural(int indiceGrupo) {
        return switch (indiceGrupo) {
            case 2 -> "millones";
            case 4 -> "billones";
            case 6 -> "trillones";
            case 8 -> "cuatrillones";
            case 10 -> "quintillones";
            case 12 -> "sextillones";
            case 14 -> "septillones";
            case 16 -> "octillones";
            case 18 -> "nonillones";
            case 20 -> "decillones";
            default -> "10^" + (indiceGrupo * 3);
        };
    }

    private String resolverRutaPlantillaCertificado() {
        Path escritorio = Paths.get(System.getProperty("user.home"), "Desktop");

        Path[] candidatas = new Path[] {
                Paths.get(NOMBRE_PLANTILLA_1),
                Paths.get(NOMBRE_PLANTILLA_2),
                escritorio.resolve(NOMBRE_PLANTILLA_1),
                escritorio.resolve(NOMBRE_PLANTILLA_2),
                Paths.get("src", "main", "resources", "plantillas", NOMBRE_PLANTILLA_1),
                Paths.get("src", "main", "resources", "plantillas", NOMBRE_PLANTILLA_2),
                Paths.get("src", "main", "resources", "com", "practicasalma", "proyectoalma", "assets", NOMBRE_PLANTILLA_1),
                Paths.get("src", "main", "resources", "com", "practicasalma", "proyectoalma", "assets", NOMBRE_PLANTILLA_2)
        };

        for (Path candidata : candidatas) {
            Path absoluta = candidata.toAbsolutePath();
            if (Files.exists(absoluta) && Files.isRegularFile(absoluta)) {
                return absoluta.toString();
            }
        }

        String[] recursosClasspath = new String[] {
                "/com/practicasalma/proyectoalma/assets/" + NOMBRE_PLANTILLA_1,
                "/com/practicasalma/proyectoalma/assets/" + NOMBRE_PLANTILLA_2
        };

        for (String recurso : recursosClasspath) {
            try (InputStream input = GeneradorPdfService.class.getResourceAsStream(recurso)) {
                if (input == null) {
                    continue;
                }

                Path temporal = Files.createTempFile("plantilla_certificado_", ".pdf");
                Files.copy(input, temporal, StandardCopyOption.REPLACE_EXISTING);
                temporal.toFile().deleteOnExit();
                return temporal.toAbsolutePath().toString();
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo cargar la plantilla desde resources.", e);
            }
        }

        throw new IllegalStateException(
                "No se encontró la plantilla del certificado. Coloca '" + NOMBRE_PLANTILLA_1 +
                        "' en assets, escritorio o raíz del proyecto."
        );
    }
}
