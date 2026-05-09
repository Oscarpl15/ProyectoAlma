package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.util.CifradoUtil;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Servicio para el envío de correos electrónicos mediante SMTP de Gmail.
 * <p>
 * Las credenciales (correo + contraseña de aplicación de Gmail) se configuran
 * en tiempo de ejecución mediante {@link #configurarCredenciales(String, String)}.
 * Hasta que no se configure, cualquier intento de envío lanzará {@link IllegalStateException}.
 * </p>
 * <p>
 * <b>Importante:</b> se requiere una "contraseña de aplicación" de Gmail (no la contraseña normal),
 * que se genera en la cuenta de Google con la verificación en dos pasos activada.
 * </p>
 */
public class GestorCorreo {

    private static String correoRemitente = "";
    private static String contrasenaRemitente = "";

    /**
     * Establece las credenciales SMTP a usar en los envíos.
     *
     * @param correo     dirección de correo del remitente
     * @param contrasena contraseña de aplicación de Gmail
     */
    public static void configurarCredenciales(String correo, String contrasena) {
        correoRemitente = correo != null ? correo.trim() : "";
        contrasenaRemitente = contrasena != null ? contrasena : "";
        GestorConfig.setCorreoRemitente(correoRemitente);
        if (!contrasenaRemitente.isBlank()) {
            GestorConfig.setCorreoPasswordApp(CifradoUtil.cifrar(contrasenaRemitente));
        }
    }

    public static boolean estaConfigurado() {
        cargarCredencionalesDesdeConfigSiHaceFalta();
        return !correoRemitente.isBlank() && !contrasenaRemitente.isBlank();
    }

    private static void cargarCredencionalesDesdeConfigSiHaceFalta() {
        if (correoRemitente.isBlank()) {
            String correoGuardado = GestorConfig.getCorreoRemitente();
            if (correoGuardado != null && !correoGuardado.isBlank()) {
                correoRemitente = correoGuardado.trim();
            }
        }
        if (contrasenaRemitente.isBlank()) {
            String passwordCifrada = GestorConfig.getCorreoPasswordApp();
            if (passwordCifrada != null && !passwordCifrada.isBlank()) {
                contrasenaRemitente = CifradoUtil.descifrar(passwordCifrada);
            }
        }
    }

    private static Session crearSesion() {
        if (!estaConfigurado()) {
            throw new IllegalStateException("Debes configurar correo y contraseña antes de enviar.");
        }

        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.host", "smtp.gmail.com");
        propiedades.put("mail.smtp.port", "587");

        return Session.getInstance(propiedades, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(correoRemitente, contrasenaRemitente);
            }
        });
    }

    /**
     * Envía un correo HTML con imagen inline y fichero adjunto.
     *
     * @param destinatario  dirección de destino
     * @param asunto        asunto del mensaje
     * @param cuerpoHtml    cuerpo del mensaje en HTML
     * @param rutaAdjunto   ruta absoluta al fichero a adjuntar
     * @param recursoImagen ruta de recurso classpath a la imagen inline (puede ser {@code null})
     * @param contentId     Content-ID para referenciar la imagen desde el HTML
     * @throws com.practicasalma.proyectoalma.exception.AlmaException si el envío falla
     */
    public static void mandarEmailHtmlConAdjuntoImagen(
            String destinatario,
            String asunto,
            String cuerpoHtml,
            String rutaAdjunto,
            String recursoImagen,
            String contentId
    ) {
        Session sesion = crearSesion();

        try {
            MimeMessage mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(correoRemitente));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto, StandardCharsets.UTF_8.name());

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(cuerpoHtml, "text/html; charset=UTF-8");

            MimeMultipart related = new MimeMultipart("related");
            related.addBodyPart(htmlPart);

            if (recursoImagen != null && !recursoImagen.isBlank()) {
                MimeBodyPart imagenPart = new MimeBodyPart();
                byte[] imagenBytes = cargarRecursoBytes(recursoImagen);
                if (imagenBytes != null) {
                    imagenPart.setDataHandler(new DataHandler(new ByteArrayDataSource(imagenBytes, "image/png")));
                    imagenPart.setFileName("logo.png");
                    imagenPart.setHeader("Content-ID", "<" + contentId + ">");
                    imagenPart.setDisposition(MimeBodyPart.INLINE);
                    related.addBodyPart(imagenPart);
                }
            }

            MimeBodyPart relatedWrapper = new MimeBodyPart();
            relatedWrapper.setContent(related);

            MimeBodyPart adjuntoPart = new MimeBodyPart();
            adjuntoPart.attachFile(new File(rutaAdjunto));

            MimeMultipart mixed = new MimeMultipart("mixed");
            mixed.addBodyPart(relatedWrapper);
            mixed.addBodyPart(adjuntoPart);

            mensaje.setContent(mixed);

            Transport.send(mensaje);
        } catch (jakarta.mail.AuthenticationFailedException e) {
            throw new com.practicasalma.proyectoalma.exception.AlmaException(
                    "Correo o contraseña de aplicación incorrectos. Verifica las credenciales en Ajustes.", e);
        } catch (Exception e) {
            String mensaje = e.getMessage();
            if (mensaje != null && (mensaje.contains("535") || mensaje.contains("Authentication") || mensaje.contains("credentials"))) {
                throw new com.practicasalma.proyectoalma.exception.AlmaException(
                        "Correo o contraseña de aplicación incorrectos. Verifica las credenciales en Ajustes.", e);
            }
            throw new com.practicasalma.proyectoalma.exception.AlmaException(
                    "Error al enviar el correo: " + mensaje, e);
        }
    }

    private static byte[] cargarRecursoBytes(String recurso) {
        try (InputStream input = GestorCorreo.class.getResourceAsStream(recurso)) {
            if (input == null) {
                return null;
            }
            return input.readAllBytes();
        } catch (Exception e) {
            return null;
        }
    }
}
