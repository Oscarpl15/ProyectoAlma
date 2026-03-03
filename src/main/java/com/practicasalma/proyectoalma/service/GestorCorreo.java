package com.practicasalma.proyectoalma.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class GestorCorreo {

    public static void mandarEmail(String destinatario, String asunto, String cuerpo) {

        // 1. Datos de tu cuenta (Remitente)
        String miCorreo = "tu_correo@gmail.com";
        String miContrasena = "TU_CONTRASEÑA_DE_APLICACION";

        // 2. Configuración del servidor SMTP de Gmail
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true"); // Seguridad TLS
        propiedades.put("mail.smtp.host", "smtp.gmail.com");  // Servidor de Gmail
        propiedades.put("mail.smtp.port", "587");             // Puerto seguro

        // 3. Crear la sesión con tus credenciales
        Session sesion = Session.getInstance(propiedades, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(miCorreo, miContrasena);
            }
        });

        try {
            // 4. Crear el mensaje
            Message mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(miCorreo));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);

            // 5. Enviar el mensaje
            Transport.send(mensaje);
            System.out.println("¡Correo enviado con éxito a " + destinatario + "!");

        } catch (MessagingException e) {
            System.out.println("Error al enviar el correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
