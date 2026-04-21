package com.practicasalma.proyectoalma.controller;


import com.practicasalma.proyectoalma.service.GestorAsignaciones;
import com.practicasalma.proyectoalma.service.GestorCorreo;
import com.practicasalma.proyectoalma.service.GestorMatriculas;
import com.practicasalma.proyectoalma.util.GestorConfig;
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
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.Optional;

public class MainController {

    @FXML private TabPane tabPrincipal;
    @FXML private StackPane rootPane;
    @FXML private ImageView imgFondo;
    @FXML private Button btnAjustes;

    // Sub-controladores inyectados automáticamente por JavaFX gracias al fx:id de fx:include
    @FXML private AlumnosController alumnosController;
    @FXML private DocentesController docentesController;
    @FXML private VoluntariosController voluntariosController;

    @FXML private ContextMenu menuAjustes;

    @FXML
    public void initialize() {
        imgFondo.fitWidthProperty().bind(rootPane.widthProperty());
        imgFondo.fitHeightProperty().bind(rootPane.heightProperty());
        if (tabPrincipal != null) {
            tabPrincipal.getSelectionModel().select(0);
        }
        GestorMatriculas.DatosDialogo datosSexto = GestorMatriculas.ejecutar();
        GestorAsignaciones.DatosPersonal datosPersonal = GestorAsignaciones.prepararDatos();
        if (datosSexto != null || datosPersonal != null) {
            RenovacionController.mostrar(datosSexto, datosPersonal);
            alumnosController.cargarAlumnosEnTabla();
            docentesController.cargarDocentesEnTabla();
            voluntariosController.cargarVoluntariosEnTabla();
        }
    }

    @FXML
    private void abrirMenuAjustes() {
        menuAjustes.show(btnAjustes, Side.BOTTOM, 0, 4);
        javafx.stage.Window window = btnAjustes.getScene().getWindow();
        menuAjustes.setX(window.getX() + window.getWidth() - menuAjustes.getWidth() - 15);
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
    private void cambiarDirectorioBBDD() {
        ConfiguracionInicialController.mostrarCambioBBDD();
        String nuevaRuta = GestorConfig.getRutaBBDD();
        if (nuevaRuta != null && !nuevaRuta.isBlank()) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Cambio pendiente");
            info.setHeaderText(null);
            info.setContentText("El cambio de base de datos se aplicará al reiniciar la aplicación.");
            info.showAndWait();
        }
    }

    @FXML
    private void cambiarDirectorioDocs() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar directorio de documentos");
        String actual = GestorConfig.getRutaDocumentos();
        if (actual != null && !actual.isBlank()) {
            File dirActual = new File(actual);
            if (dirActual.exists()) chooser.setInitialDirectory(dirActual);
        }
        File dir = chooser.showDialog(btnAjustes.getScene().getWindow());
        if (dir != null) {
            GestorConfig.setRutaDocumentos(dir.getAbsolutePath());
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Directorio actualizado");
            ok.setHeaderText(null);
            ok.setContentText("Directorio de documentos actualizado correctamente.");
            ok.showAndWait();
        }
    }

    @FXML
    private void configurarCorreo() {
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
