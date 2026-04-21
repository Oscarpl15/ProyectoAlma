package com.practicasalma.proyectoalma.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
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
    }

    public static boolean estaConfigurado() {
        return !correoRemitente.isBlank() && !contrasenaRemitente.isBlank();
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
     * Envía un correo de texto plano.
     *
     * @param destinatario dirección de destino
     * @param asunto       asunto del mensaje
     * @param cuerpo       cuerpo del mensaje en texto plano
     * @throws com.practicasalma.proyectoalma.exception.AlmaException si el envío falla
     */
    public static void mandarEmail(String destinatario, String asunto, String cuerpo) {
        Session sesion = crearSesion();

        try {
            Message mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(correoRemitente));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);

            Transport.send(mensaje);

        } catch (MessagingException e) {
            throw new com.practicasalma.proyectoalma.exception.AlmaException("Error al enviar el correo: " + e.getMessage(), e);
        }
    }

    /**
     * Envía un correo con un fichero adjunto (multipart MIME).
     *
     * @param destinatario dirección de destino
     * @param asunto       asunto del mensaje
     * @param cuerpo       cuerpo del mensaje en texto plano
     * @param rutaAdjunto  ruta absoluta al fichero a adjuntar
     * @throws com.practicasalma.proyectoalma.exception.AlmaException si el envío falla
     */
    public static void mandarEmailConAdjunto(String destinatario, String asunto, String cuerpo, String rutaAdjunto) {
        Session sesion = crearSesion();

        try {
            Message mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(correoRemitente));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);

            MimeBodyPart cuerpoParte = new MimeBodyPart();
            cuerpoParte.setText(cuerpo);

            MimeBodyPart adjuntoParte = new MimeBodyPart();
            File archivo = new File(rutaAdjunto);
            adjuntoParte.attachFile(archivo);

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(cuerpoParte);
            multipart.addBodyPart(adjuntoParte);

            mensaje.setContent(multipart);

            Transport.send(mensaje);
        } catch (Exception e) {
            throw new com.practicasalma.proyectoalma.exception.AlmaException("Error al enviar el correo con adjunto: " + e.getMessage(), e);
        }
    }
}
