package com.practicasalma.proyectoalma.controller;


import com.practicasalma.proyectoalma.service.GestorCorreo;
import com.practicasalma.proyectoalma.service.GestorMatriculas;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.Optional;

public class MainController {

    @FXML
    private TabPane tabPrincipal; // Se conecta con el fx:id del FXML

    @FXML private StackPane rootPane;
    @FXML private ImageView imgFondo;
    @FXML private Button btnAjustes;

    private ContextMenu menuAjustes;

    @FXML
    public void initialize() {
        // Hace que la imagen persiga dinámicamente el ancho y alto de la ventana
        imgFondo.fitWidthProperty().bind(rootPane.widthProperty());
        imgFondo.fitHeightProperty().bind(rootPane.heightProperty());
        // Opcional: Asegurarnos de que arranca en la pestaña 0
        if (tabPrincipal != null) {
            tabPrincipal.getSelectionModel().select(0);
        }
        GestorMatriculas.ejecutar();
        menuAjustes = new ContextMenu();
    }

    @FXML
    private void abrirMenuAjustes() {
        menuAjustes.show(btnAjustes, Side.BOTTOM, 0, 4);
    }

    // --- MÉTODOS DE LOS BOTONES DEL MENÚ ---

    @FXML
    protected void mostrarAlumnos() {
        // Selecciona la primera pestaña (Índice 0)
        tabPrincipal.getSelectionModel().select(0);
    }

    @FXML
    protected void mostrarDocentes() {
        // Selecciona la segunda pestaña (Índice 1)
        tabPrincipal.getSelectionModel().select(1);
    }

    @FXML
    protected void mostrarVoluntarios() {
        // Selecciona la tercera pestaña (Índice 2)
        tabPrincipal.getSelectionModel().select(2);
    }

    @FXML
    protected void mostrarSocios() {
        // Selecciona la tercera pestaña (Índice 2)
        tabPrincipal.getSelectionModel().select(3);
    }

    @FXML
    protected void configurarCorreo() {
        TextInputDialog correoDialog = new TextInputDialog();
        correoDialog.setTitle("Configurar correo");
        correoDialog.setHeaderText("Introduce el correo de envío");
        correoDialog.setContentText("Correo:");

        Optional<String> correoResultado = correoDialog.showAndWait();
        if (correoResultado.isEmpty() || correoResultado.get().trim().isEmpty()) {
            return;
        }

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña de aplicación");

        Alert passwordAlert = new Alert(Alert.AlertType.CONFIRMATION);
        passwordAlert.setTitle("Configurar correo");
        passwordAlert.setHeaderText("Introduce la contraseña del correo");
        passwordAlert.getDialogPane().setContent(passwordField);

        Optional<javafx.scene.control.ButtonType> passwordResultado = passwordAlert.showAndWait();
        if (passwordResultado.isEmpty() || passwordResultado.get() != javafx.scene.control.ButtonType.OK) {
            return;
        }

        String contrasena = passwordField.getText();
        if (contrasena == null || contrasena.isBlank()) {
            return;
        }

        GestorCorreo.configurarCredenciales(correoResultado.get().trim(), contrasena);

        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setTitle("Correo configurado");
        ok.setHeaderText(null);
        ok.setContentText("Correo de envío configurado correctamente.");
        ok.showAndWait();
    }


}
