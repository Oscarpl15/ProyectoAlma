package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.util.config.GestorConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que gestiona el envío automático de certificados de donaciones anuales.
 * <p>
 * El envío automático se activa el 20 de enero (o posterior si la app arranca tarde),
 * y cubre las donaciones del año anterior. No debe instanciarse.
 * </p>
 */
public class GestorCertificadosAnuales {

    private GestorCertificadosAnuales() {}

    /**
     * Indica si la fecha actual corresponde al periodo de envío anual.
     * El día y mes de inicio se leen de configuración (por defecto: 20 de enero).
     */
    public static boolean esPeriodoEnvio() {
        LocalDate hoy = LocalDate.now();
        int mes = GestorConfig.getCertificadosMesEnvio();
        int dia = GestorConfig.getCertificadosDiaEnvio();
        return hoy.getMonthValue() > mes
                || (hoy.getMonthValue() == mes && hoy.getDayOfMonth() >= dia);
    }

    /**
     * Indica si los certificados ya se han enviado el año en curso.
     */
    public static boolean yaEnviadoEsteAno() {
        String ultimoAno = GestorConfig.getCertificadosUltimoAno();
        return String.valueOf(LocalDate.now().getYear()).equals(ultimoAno);
    }

    /** Persiste en BD que los certificados han sido enviados este año. */
    public static void marcarEnviados() {
        GestorConfig.setCertificadosUltimoAno(String.valueOf(LocalDate.now().getYear()));
    }

    /**
     * Devuelve los socios que tienen al menos una donación registrada en el año indicado.
     *
     * @param ano año a filtrar
     * @return lista de socios con donaciones en ese año
     */
    public static List<Socio> obtenerSociosConDonacionesEnAno(int ano) {
        SocioService socioService = new SocioService();
        List<Socio> todos = socioService.obtenerTodosConDonaciones();
        return filtrarConDonacionesEnAno(todos, ano);
    }

    static List<Socio> filtrarConDonacionesEnAno(List<Socio> todos, int ano) {
        List<Socio> resultado = new ArrayList<>();
        for (Socio socio : todos) {
            if (socio == null || socio.getDonaciones() == null) continue;
            boolean tieneDonaciones = socio.getDonaciones().stream()
                    .anyMatch(d -> d.getFecha() != null && d.getFecha().getYear() == ano);
            if (tieneDonaciones) resultado.add(socio);
        }
        return resultado;
    }

    /**
     * Construye las líneas de texto para el diálogo de confirmación,
     * mostrando el total de donaciones de cada socio en el año indicado.
     *
     * @return lista de cadenas con formato "Nombre Apellidos  —  XX.XX €"
     */
    public static List<String> construirLineasConfirmacion(List<Socio> socios, int ano) {
        List<String> lineas = new ArrayList<>();
        for (Socio socio : socios) {
            if (socio == null) continue;
            String nombre = (seguro(socio.getNombre()) + " " + seguro(socio.getApellidos())).trim();
            BigDecimal total = BigDecimal.ZERO;
            if (socio.getDonaciones() != null) {
                for (Donacion d : socio.getDonaciones()) {
                    if (d.getImporte() != null && d.getFecha() != null && d.getFecha().getYear() == ano) {
                        total = total.add(d.getImporte());
                    }
                }
            }
            lineas.add(nombre + "  —  " + total.setScale(2, RoundingMode.HALF_UP).toPlainString() + " €");
        }
        return lineas;
    }

    /**
     * Genera el certificado PDF de cada socio y lo envía por correo.
     * Los socios sin correo configurado se ignoran sin error.
     *
     * @param socios lista de socios a los que enviar el certificado
     * @param ano    año del que se certifican las donaciones
     * @throws IllegalStateException si el directorio de documentos no está configurado
     */
    public static void enviarCertificados(List<Socio> socios, int ano) {
        String rutaDocumentos = GestorConfig.getRutaDocumentos();
        if (rutaDocumentos == null || rutaDocumentos.isBlank()) {
            throw new IllegalStateException("El directorio de documentos no está configurado.");
        }
        GeneradorPdfService pdfService = new GeneradorPdfService();
        for (Socio socio : socios) {
            String correoSocio = socio.getCorreo();
            if (correoSocio == null || correoSocio.isBlank()) continue;
            String nombreCarpeta = (socio.getNombre() + " " + socio.getApellidos()).replaceAll("[/\\\\:*?\"<>|]", "").trim();
            String nombreArchivoCert = nombreCarpeta.replaceAll("\\s", "");
            Path rutaInforme = Paths.get(rutaDocumentos, "Socios", nombreCarpeta,
                    "certificado_" + ano + "_" + nombreArchivoCert + ".pdf");
            pdfService.generarInformeSocioConPlantilla(rutaInforme.toString(), socio, ano);
            GestorCorreo.mandarEmailHtmlConAdjuntoImagen(
                    correoSocio,
                    "Certificado de donaciones " + ano,
                    construirCorreoHtml(socio.getNombre(), ano),
                    rutaInforme.toString(),
                    "/com/practicasalma/proyectoalma/assets/logo.png",
                    "logo"
            );
        }
    }

    private static String construirCorreoHtml(String nombre, int ano) {
        String n = (nombre == null || nombre.isBlank()) ? "" : nombre.trim();
        return "<html><body style='font-family: Arial, sans-serif; font-size: 12pt; color: #1a1a1a;'>"
                + "<p>Hola " + n + ",</p>"
                + "<p>Te informamos que ya está listo el certificado de tus donaciones (" + ano + "). "
                + "Adjunto a este documento puedes descargártelo.</p>"
                + "<p>Con este certificado podrás obtener desgravaciones fiscales en la declaración de la renta que presentes este año.</p>"
                + "<p>Conoce todos los detalles sobre desgravaciones fiscales en nuestra web.</p>"
                + "<p>Un abrazo,</p>"
                + "<p>Equipo de Fundación Alma.</p>"
                + "<p><img src='cid:logo' alt='Fundación Alma' style='width:160px; height:auto;'/></p>"
                + "<p>Calle Torrelaguna 21, Alcalá de Henares (Madrid)<br/>"
                + "Tlfno: 685761563<br/>"
                + "info@fundacionalma2022.org<br/>"
                + "www.fundacionalma2022.org</p>"
                + "<p>NOTA LEGAL: Este mensaje y cualquier archivo adjunto está destinado únicamente a quien se dirige y es confidencial. "
                + "Si usted ha recibido este mensaje por error, comuníqueselo al remitente y bórrelo inmediatamente.</p>"
                + "<p>PROTECCIÓN DE DATOS – Responsable: FUNDACION ALMA 2022. Finalidad: envío de información. "
                + "Legitimación: interés legítimo, contrato o consentimiento. Derechos: acceso, rectificación, supresión, "
                + "limitación, oposición y portabilidad dirigiéndose a direccion@fundacionalma2022.org.</p>"
                + "</body></html>";
    }

    private static String seguro(String v) {
        return v != null ? v.trim() : "";
    }
}
