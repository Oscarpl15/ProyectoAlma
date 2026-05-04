package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.service.GestorCorreo;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador de la ventana de gestión de credenciales de correo.
 * <p>
 * Permite configurar el correo remitente y la contraseña de aplicación de Gmail.
 * Ambas credenciales se persisten en la BD (la contraseña cifrada con AES).
 * Si el campo de contraseña se deja vacío al guardar y ya existe una contraseña
 * guardada, no se sobreescribe.
 * </p>
 */
public class GestorCredencialesController {

    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtPassword;

    @FXML
    public void initialize() {
        String correoGuardado = GestorConfig.getCorreoRemitente();
        if (correoGuardado != null && !correoGuardado.isBlank()) {
            txtCorreo.setText(correoGuardado.trim());
        }
    }

    @FXML
    private void guardar() {
        String correo = txtCorreo.getText().trim();
        if (correo.isEmpty()) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Campo obligatorio", "El correo no puede estar vacío.");
            return;
        }

        String password = txtPassword.getText();
        if (password.isBlank()) {
            // Si no se introduce contraseña, solo se actualiza el correo si ya hay una guardada
            String passwordExistente = GestorConfig.getCorreoPasswordApp();
            if (passwordExistente == null || passwordExistente.isBlank()) {
                FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Campo obligatorio", "La contraseña no puede estar vacía.");
                return;
            }
            GestorConfig.setCorreoRemitente(correo);
        } else {
            GestorCorreo.configurarCredenciales(correo, password);
        }

        FxUtils.mostrarAlerta(Alert.AlertType.INFORMATION, "Guardado", "Las credenciales se han guardado correctamente.");
        cerrar();
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) txtCorreo.getScene().getWindow()).close();
    }
}
