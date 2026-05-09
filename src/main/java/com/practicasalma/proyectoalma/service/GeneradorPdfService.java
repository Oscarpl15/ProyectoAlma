package com.practicasalma.proyectoalma.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfStamper;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.model.PersonaAutorizada;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Servicio de generación de documentos PDF para la fundación.
 * <p>
 * Genera tres tipos de documentos usando la librería OpenPDF:
 * <ol>
 *   <li><b>Certificado de donaciones</b> — rellena la plantilla PDF oficial (con AcroForm o
 *       reemplazo de texto plano como fallback) con los datos del socio y el importe del año indicado.</li>
 *   <li><b>Listado de alumnos por grupos</b> — PDF con la distribución de alumnos por grupo en un curso escolar.</li>
 *   <li><b>Informe de gráficas</b> — captura de una gráfica con su resumen en PDF.</li>
 * </ol>
 * La plantilla del certificado se busca automáticamente en varias ubicaciones: raíz del proyecto,
 * escritorio del usuario, carpeta de recursos y classpath (en ese orden).
 * </p>
 * <p>
 * El importe se convierte a letras en español siguiendo las reglas morfológicas del castellano
 * (apócope de "uno" ante sustantivo, etc.).
 * </p>
 */
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

    public void generarPdfGruposAlumnos(String rutaSalida, List<Alumno> alumnos, String titulo, String cursoEscolar) {
        if (rutaSalida == null || rutaSalida.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta de salida del PDF es obligatoria.");
        }
        if (alumnos == null) {
            throw new IllegalArgumentException("La lista de alumnos es obligatoria.");
        }

        Path ruta = Paths.get(rutaSalida.trim());
        try {
            Path carpetaPadre = ruta.getParent();
            if (carpetaPadre != null) {
                Files.createDirectories(carpetaPadre);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de salida para el PDF de grupos.", e);
        }

        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(ruta.toFile());
            PdfWriter.getInstance(document, outputStream);
            document.open();

            String tituloSeguro = (titulo == null || titulo.trim().isEmpty()) ? "Listado de alumnos por grupos" : titulo.trim();
            document.add(new Paragraph(tituloSeguro));
            document.add(new Paragraph("Fecha: " + LocalDate.now()));
            document.add(new Paragraph("Total alumnos: " + alumnos.size()));
            if (cursoEscolar != null && !cursoEscolar.trim().isEmpty()) {
                document.add(new Paragraph("Curso escolar: " + cursoEscolar.trim()));
            }
            document.add(new Paragraph(" "));

            Map<String, List<Alumno>> alumnosPorGrupo = new LinkedHashMap<>();
            for (Alumno alumno : alumnos) {
                if (alumno == null) {
                    continue;
                }

                Matricula matricula = obtenerMatriculaParaCurso(alumno, cursoEscolar);
                String grupo = (matricula != null && matricula.getGrupoAsignado() != null
                        && !matricula.getGrupoAsignado().trim().isEmpty())
                        ? matricula.getGrupoAsignado().trim()
                        : "Sin grupo";

                alumnosPorGrupo
                        .computeIfAbsent(grupo, k -> new ArrayList<>())
                        .add(alumno);
            }

            List<String> grupos = new ArrayList<>(alumnosPorGrupo.keySet());
            grupos.sort((a, b) -> {
                if ("Sin grupo".equalsIgnoreCase(a) && !"Sin grupo".equalsIgnoreCase(b)) return 1;
                if (!"Sin grupo".equalsIgnoreCase(a) && "Sin grupo".equalsIgnoreCase(b)) return -1;
                return a.compareToIgnoreCase(b);
            });

            if (grupos.isEmpty()) {
                document.add(new Paragraph("No hay alumnos para generar el listado."));
            } else {
                for (String grupo : grupos) {
                    List<Alumno> alumnosGrupo = alumnosPorGrupo.get(grupo);
                    if (alumnosGrupo == null) {
                        continue;
                    }

                    alumnosGrupo.sort(Comparator
                            .comparing((Alumno a) -> valorSeguro(a.getApellidos()), String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(a -> valorSeguro(a.getNombre()), String.CASE_INSENSITIVE_ORDER));

                    document.add(new Paragraph("GRUPO: " + grupo + " (" + alumnosGrupo.size() + ")"));
                    document.add(new Paragraph("------------------------------------------------------------"));

                    for (Alumno alumno : alumnosGrupo) {
                        String nombreCompleto = construirNombreCompleto(alumno);
                        Matricula matricula = obtenerMatriculaParaCurso(alumno, cursoEscolar);
                        String curso = obtenerCursoTexto(matricula);

                        document.add(new Paragraph("Alumno: " + nombreCompleto));
                        document.add(new Paragraph("Curso: " + curso));
                        document.add(new Paragraph("Autorizados a recoger: " + obtenerAutorizadosTexto(alumno)));
                        document.add(new Paragraph(" "));
                    }
                    document.add(new Paragraph(" "));
                }
            }
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Error al generar el PDF de grupos de alumnos.", e);
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

    private Matricula obtenerUltimaMatricula(Alumno alumno) {
        if (alumno.getMatriculas() == null || alumno.getMatriculas().isEmpty()) {
            return null;
        }
        return alumno.getMatriculas().get(alumno.getMatriculas().size() - 1);
    }

    private String obtenerAutorizadosTexto(Alumno alumno) {
        if (alumno == null || alumno.getAutorizadaRecoger() == null || alumno.getAutorizadaRecoger().isEmpty()) {
            return "Sin autorizados";
        }

        List<String> autorizados = new ArrayList<>();
        for (PersonaAutorizada persona : alumno.getAutorizadaRecoger()) {
            if (persona == null) {
                continue;
            }
            String nombre = (valorSeguro(persona.getNombre()) + " " + valorSeguro(persona.getApellidos())).trim();
            String relacion = valorSeguro(persona.getRelacion());
            if (!"-".equals(nombre)) {
                if (!"-".equals(relacion)) {
                    autorizados.add(nombre + " (" + relacion + ")");
                } else {
                    autorizados.add(nombre);
                }
            }
        }

        if (autorizados.isEmpty()) {
            return "Sin autorizados";
        }

        Collections.sort(autorizados, String.CASE_INSENSITIVE_ORDER);
        return String.join(", ", autorizados);
    }

    private String construirNombreCompleto(Alumno alumno) {
        String nombre = valorSeguro(alumno.getNombre());
        String apellidos = valorSeguro(alumno.getApellidos());
        String completo = (nombre + " " + apellidos).trim();
        return completo.isBlank() ? "-" : completo;
    }

    private String obtenerCursoTexto(Matricula matricula) {
        if (matricula == null || matricula.getCurso() == null || matricula.getCurso().trim().isEmpty()) {
            return "Sin curso";
        }
        return matricula.getCurso().trim();
    }

    private Matricula obtenerMatriculaParaCurso(Alumno alumno, String cursoEscolar) {
        if (alumno == null || alumno.getMatriculas() == null || alumno.getMatriculas().isEmpty()) {
            return null;
        }
        if (cursoEscolar == null || cursoEscolar.trim().isEmpty()) {
            return obtenerUltimaMatricula(alumno);
        }
        String normalizado = cursoEscolar.trim();
        String alternativo = normalizado.replace("/", "-");
        for (Matricula matricula : alumno.getMatriculas()) {
            if (matricula == null) {
                continue;
            }
            String ano = matricula.getAnyoAcademico();
            if (ano == null) {
                continue;
            }
            if (normalizado.equals(ano) || alternativo.equals(ano)) {
                return matricula;
            }
        }
        return null;
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

    /**
     * Genera el certificado de donaciones filtrando solo las del año indicado.
     *
     * @param rutaSalida ruta del fichero PDF de salida
     * @param socio      socio para el que se emite el certificado (con donaciones cargadas)
     * @param ano        año de las donaciones a certificar (p. ej. año actual para envío manual)
     */
    public void generarInformeSocioConPlantilla(String rutaSalida, Socio socio, int ano) {
        if (socio == null) {
            throw new IllegalArgumentException("El socio es obligatorio para generar el informe.");
        }

        List<Donacion> donaciones = socio.getDonaciones();
        if (donaciones != null && ano > 0) {
            donaciones = donaciones.stream()
                    .filter(d -> d.getFecha() != null && d.getFecha().getYear() == ano)
                    .collect(java.util.stream.Collectors.toList());
        }

        BigDecimal totalDonado = BigDecimal.ZERO;
        int anoInicial = (ano > 0) ? ano : LocalDate.now().getYear();

        if (donaciones != null && !donaciones.isEmpty()) {
            for (Donacion donacion : donaciones) {
                if (donacion.getImporte() != null) {
                    totalDonado = totalDonado.add(donacion.getImporte());
                }
            }
            if (ano <= 0) {
                Donacion masReciente = donaciones.stream()
                        .filter(d -> d.getFecha() != null)
                        .max(Comparator.comparing(Donacion::getFecha))
                        .orElse(null);
                if (masReciente != null) {
                    anoInicial = masReciente.getFecha().getYear();
                }
            }
        }

        LocalDate hoy = LocalDate.now();
        String nombreCompleto = (valorSeguro(socio.getNombre()) + " " + valorSeguro(socio.getApellidos())).trim();
        String domicilio = construirDomicilioObligatorio(socio);
        BigDecimal cantidadInforme = obtenerCantidadInforme(totalDonado, socio.getCuota());
        String cantidadNumero = formatearCantidad(cantidadInforme);
        String cantidadLetra = convertirImporteALetras(cantidadInforme);

        generarCertificadoDesdeZero(
                rutaSalida,
                String.valueOf(anoInicial),
                nombreCompleto,
                valorSeguro(socio.getTipoDocumento()),
                valorSeguro(socio.getDocumentoIdentidad()),
                domicilio,
                cantidadLetra,
                cantidadNumero,
                String.valueOf(hoy.getDayOfMonth()),
                hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toUpperCase(Locale.ROOT),
                String.valueOf(hoy.getYear())
        );
    }

    private void generarCertificadoDesdeZero(
            String rutaSalida,
            String anoInicial,
            String nombre,
            String tipoDocumento,
            String dni,
            String domicilio,
            String cantidadLetra,
            String cantidadNumero,
            String dia,
            String mes,
            String anoFinal) {

        Path rutaDestino = Paths.get(rutaSalida.trim());
        try {
            Path carpetaPadre = rutaDestino.getParent();
            if (carpetaPadre != null) Files.createDirectories(carpetaPadre);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de salida para el certificado.", e);
        }

        byte[] logoBytes = cargarRecursoBytes("/com/practicasalma/proyectoalma/assets/LogoCertificado.png");
        byte[] selloBytes = cargarRecursoBytes("/com/practicasalma/proyectoalma/assets/SelloCertificado.png");
        if (logoBytes == null) {
            throw new com.practicasalma.proyectoalma.exception.AlmaException("No se encontró LogoCertificado.png en assets.", null);
        }
        if (selloBytes == null) {
            throw new com.practicasalma.proyectoalma.exception.AlmaException("No se encontró SelloCertificado.png en assets.", null);
        }

        float margenLateral = 60f;
        float margenSuperior = 100f;
        float margenInferior = 100f;

        Document document = new Document(PageSize.A4, margenLateral, margenLateral, margenSuperior, margenInferior);

        try (FileOutputStream fos = new FileOutputStream(rutaDestino.toFile())) {
            PdfWriter writer = PdfWriter.getInstance(document, fos);

            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            BaseFont bfNegrita = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);

            Font fNormal = new Font(bf, 12, Font.NORMAL);
            Font fNegrita = new Font(bfNegrita, 12, Font.NORMAL);
            Font fTitulo = new Font(bfNegrita, 14, Font.NORMAL);

            Image logo = Image.getInstance(logoBytes);
            Image sello = Image.getInstance(selloBytes);

            writer.setPageEvent(new EventoPaginaCertificado(logo, bf, margenLateral, margenInferior));

            document.open();

            // ── PÁGINA 1 ──────────────────────────────────────────────
            Paragraph titulo = new Paragraph("CERTIFICADO DE DONACIÓN", fTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(22f);
            document.add(titulo);

            Paragraph intro = new Paragraph(
                    "D. Ángel Cuevas López, con D.N.I 09043551-C, como Presidente de FUNDACIÓN " +
                    "ALMA 2022, con N.I.F. G-67919654, con domicilio social en Calle Perú 39, 28806 de " +
                    "Alacalá de Henares (Madrid), constituida ante el Notario de Madrid, D. Carlos de " +
                    "Prada Guaita con fecha 14 de febrero de 2022 mediante escritura pública con protocolo " +
                    "número 249, e inscrita el día 14 de junio de 2022 en el Registro de Fundaciones " +
                    "de la Comunidad de Madrid con número 00249/2022, al Tomo CCXCVII, Folio 201-238, " +
                    "inscripción 1ª, e incluida entre las reguladas en el Artículo 16 de la " +
                    "Ley 49/2002, de 23 de diciembre, de régimen fiscal de las entidades sin fines " +
                    "lucrativos y de los incentivos fiscales al mecenazgo,",
                    fNormal);
            intro.setAlignment(Element.ALIGN_JUSTIFIED);
            intro.setSpacingAfter(22f);
            document.add(intro);

            Paragraph certifica = new Paragraph("CERTIFICA", fNegrita);
            certifica.setAlignment(Element.ALIGN_CENTER);
            certifica.setSpacingAfter(22f);
            document.add(certifica);

            Paragraph primero = new Paragraph();
            primero.setAlignment(Element.ALIGN_JUSTIFIED);
            primero.setSpacingAfter(18f);
            primero.add(new Chunk("Primero.-", fNegrita));
            primero.add(new Chunk(
                    " Que FUNDACIÓN ALMA 2022 tiene como objetivo general fomentar y contribuir " +
                    "a la educación y a la cultura, a través de la realización de actividades " +
                    "tendentes a promover servicios de apoyo y atención educativa y social a menores " +
                    "que se encuentren en riesgo por cuestiones económicas, familiares o sociales.",
                    fNormal));
            document.add(primero);

            Paragraph segundo = new Paragraph();
            segundo.setAlignment(Element.ALIGN_JUSTIFIED);
            segundo.setSpacingAfter(18f);
            segundo.add(new Chunk("Segundo.-", fNegrita));
            segundo.add(new Chunk(" Que durante el año ", fNormal));
            segundo.add(new Chunk(anoInicial, fNormal));
            segundo.add(new Chunk(", ", fNormal));
            segundo.add(new Chunk(nombre, fNormal));
            segundo.add(new Chunk(" con " + formatearTipoDocumento(tipoDocumento) + " ", fNormal));
            segundo.add(new Chunk(formatearDocumentoIdentidad(dni), fNormal));
            segundo.add(new Chunk(" y domicilio en ", fNormal));
            segundo.add(new Chunk(domicilio, fNormal));
            segundo.add(new Chunk(", realizó una donación de ", fNormal));
            segundo.add(new Chunk(cantidadLetra, fNormal));
            segundo.add(new Chunk(" (" + cantidadNumero + "€) en concepto de donación dineraria.", fNormal));
            document.add(segundo);

            Paragraph tercero = new Paragraph();
            tercero.setAlignment(Element.ALIGN_JUSTIFIED);
            tercero.add(new Chunk("Tercero.-", fNegrita));
            tercero.add(new Chunk(
                    " Que los bienes/la cantidad donados se entregan con el carácter de donación " +
                    "pura, simple e irrevocable, y se reciben con el fin de destinarlos a las actividades " +
                    "que constituyen el objeto fundacional.",
                    fNormal));
            document.add(tercero);

            // ── PÁGINA 2 ──────────────────────────────────────────────
            document.newPage();

            Paragraph cuarto = new Paragraph();
            cuarto.setAlignment(Element.ALIGN_JUSTIFIED);
            cuarto.setSpacingAfter(24f);
            cuarto.add(new Chunk("Cuarto.-", fNegrita));
            cuarto.add(new Chunk(
                    " Que la FUNDACIÓN ALMA 2022 cumple con todos los requisitos exigidos por " +
                    "el artículo 3 de la Ley 49/2002, de 23 de diciembre, de régimen fiscal de " +
                    "las entidades sin fines lucrativos y de los incentivos fiscales al mecenazgo.",
                    fNormal));
            document.add(cuarto);

            Paragraph constancia = new Paragraph(
                    "Y para que así conste, se expide el presente certificado a los efectos de la " +
                    "Ley 49/2002, de 23 de diciembre, de régimen fiscal de las entidades sin fines " +
                    "lucrativos y de los incentivos fiscales al mecenazgo.",
                    fNormal);
            constancia.setAlignment(Element.ALIGN_JUSTIFIED);
            constancia.setSpacingAfter(36f);
            document.add(constancia);

            Paragraph fecha = new Paragraph(
                    "En Alcalá de Henares, " + dia + " de " + mes + " de " + anoFinal + ".",
                    fNormal);
            fecha.setAlignment(Element.ALIGN_CENTER);
            fecha.setSpacingAfter(20f);
            document.add(fecha);

            sello.scaleToFit(120f, 120f);
            sello.setAlignment(Image.ALIGN_RIGHT);
            sello.setSpacingAfter(8f);
            document.add(sello);

            Paragraph firmaNombre = new Paragraph("Ángel Cuevas López", fNormal);
            firmaNombre.setAlignment(Element.ALIGN_RIGHT);
            document.add(firmaNombre);

            Paragraph firmaCargo = new Paragraph("Presidente", fNormal);
            firmaCargo.setAlignment(Element.ALIGN_RIGHT);
            document.add(firmaCargo);

            document.close();
        } catch (Exception e) {
            throw new com.practicasalma.proyectoalma.exception.AlmaException(
                    "Error al generar el certificado de donaciones.", e);
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

    private static final Set<String> CAMPOS_CENTRADOS = Set.of(
            CAMPO_DIA, CAMPO_MES, CAMPO_ANO_INICIAL, CAMPO_ANO_FINAL
    );

    private boolean reemplazarPorAcroForm(PdfStamper stamper, Map<String, String> valores)
            throws IOException, DocumentException {
        AcroFields form = stamper.getAcroFields();
        if (form == null || form.getFields().isEmpty()) {
            return false;
        }

        BaseFont fuente = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        form.setGenerateAppearances(true);
        boolean huboReemplazo = false;

        for (Map.Entry<String, String> entrada : valores.entrySet()) {
            String campo = entrada.getKey();
            AcroFields.Item item = form.getFieldItem(campo);
            if (item != null) {
                // Eliminar apariencias previas para que OpenPDF regenere con nuestra fuente
                for (int i = 0; i < item.size(); i++) {
                    item.getWidget(i).remove(PdfName.AP);
                }
                form.setFieldProperty(campo, "textfont", fuente, null);
                form.setFieldProperty(campo, "textsize", 12f, null);
                int alineacion = CAMPOS_CENTRADOS.contains(campo) ? 1 : 0;
                form.setFieldProperty(campo, "alignment", alineacion, null);
                form.setField(campo, entrada.getValue());
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
        String provincia = socio.getProvincia() != null ? socio.getProvincia().trim() : "";

        StringBuilder sb = new StringBuilder();
        if (!direccion.isEmpty()) {
            sb.append(direccion);
        }
        if (!cp.isEmpty()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(cp);
        }
        if (!ciudad.isEmpty()) {
            if (!sb.isEmpty()) sb.append(" de ");
            sb.append(ciudad);
        }
        if (!provincia.isEmpty()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append("(").append(provincia).append(")");
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

    private String formatearTipoDocumento(String tipo) {
        if (tipo == null || tipo.isBlank() || "-".equals(tipo)) return tipo;
        return switch (tipo.trim().toUpperCase()) {
            case "DNI" -> "D.N.I";
            case "NIE" -> "N.I.E";
            default -> tipo.trim();
        };
    }

    private String formatearDocumentoIdentidad(String documento) {
        if (documento == null || documento.isBlank() || "-".equals(documento)) return documento;
        String doc = documento.trim();
        if (doc.length() > 1
                && Character.isLetter(doc.charAt(doc.length() - 1))
                && Character.isDigit(doc.charAt(doc.length() - 2))) {
            return doc.substring(0, doc.length() - 1) + "-" + doc.charAt(doc.length() - 1);
        }
        return doc;
    }

    private static byte[] cargarRecursoBytes(String recurso) {
        try (InputStream input = GeneradorPdfService.class.getResourceAsStream(recurso)) {
            if (input == null) return null;
            return input.readAllBytes();
        } catch (Exception e) {
            return null;
        }
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

    private static class EventoPaginaCertificado extends PdfPageEventHelper {
        private final Image logo;
        private final BaseFont bf;
        private final float margenLateral;
        private final float margenInferior;

        EventoPaginaCertificado(Image logo, BaseFont bf, float margenLateral, float margenInferior) {
            this.logo = logo;
            this.bf = bf;
            this.margenLateral = margenLateral;
            this.margenInferior = margenInferior;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();
            Rectangle pageSize = document.getPageSize();

            float logoAlto = 55f;
            float logoAncho = logo.getWidth() / logo.getHeight() * logoAlto;
            float xLogo = pageSize.getWidth() - margenLateral - logoAncho;
            float yLogo = pageSize.getHeight() - 35f - logoAlto;
            try {
                canvas.addImage(logo, logoAncho, 0, 0, logoAlto, xLogo, yLogo);
            } catch (DocumentException e) {
                // no interrumpir la generación por el logo
            }

            String[] lineasFooter = {
                "FUNDACIÓN ALMA 2022",
                "Calle Perú 39, 28806 de Alcalá de Henares (Madrid)",
                "Tlfno: 620432574",
                "info@fundacionalma2022.org",
                "www.fundacionalma2022.org"
            };
            float yBase = margenInferior - 3f;
            float alturaLinea = 13f;

            canvas.beginText();
            canvas.setFontAndSize(bf, 11f);
            for (int i = 0; i < lineasFooter.length; i++) {
                canvas.showTextAligned(Element.ALIGN_LEFT, lineasFooter[i],
                        margenLateral, yBase - i * alturaLinea, 0);
            }
            canvas.endText();
        }
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
