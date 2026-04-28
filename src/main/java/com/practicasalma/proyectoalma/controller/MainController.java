package com.practicasalma.proyectoalma.controller;


import com.practicasalma.proyectoalma.service.GestorAsignaciones;
import com.practicasalma.proyectoalma.service.GestorCorreo;
import com.practicasalma.proyectoalma.service.GestorDonaciones;
import com.practicasalma.proyectoalma.service.GestorMatriculas;
import com.practicasalma.proyectoalma.service.RecordatorioCorreoService;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.doc.GestorDocumentos;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import javafx.event.ActionEvent;
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

/**
 * Controlador principal de la aplicación ({@code main-view.fxml}).
 * <p>
 * Gestiona la navegación por pestañas entre Alumnos, Docentes, Socios y Voluntarios,
 * y coordina los diálogos automáticos de inicio de curso (renovación de matrículas
 * y asignaciones de personal). También expone el menú de Ajustes para cambiar
 * el directorio de documentos y configurar credenciales de correo.
 * </p>
 */
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

        try {
            RecordatorioCorreoService recordatorioService = new RecordatorioCorreoService();
            recordatorioService.enviarSiCorresponde();
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING,
                    "Recordatorio correo",
                    "No se pudo enviar el recordatorio: " + e.getMessage());
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
    protected void mostrarGraficas() {
        tabPrincipal.getSelectionModel().select(4);
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
        String rutaActual = GestorConfig.getRutaDocumentos();
        File dirActual = (rutaActual != null && !rutaActual.isBlank()) ? new File(rutaActual) : null;

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar directorio de documentos");
        if (dirActual != null && dirActual.exists()) chooser.setInitialDirectory(dirActual);

        File dirNuevo = chooser.showDialog(btnAjustes.getScene().getWindow());
        if (dirNuevo == null) return;

        if (dirActual != null && GestorDocumentos.tieneContenido(dirActual)
                && !dirActual.getAbsolutePath().equals(dirNuevo.getAbsolutePath())) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Mover contenido");
            confirmacion.setHeaderText("Se encontró contenido en el directorio actual.");
            confirmacion.setContentText("¿Deseas mover todos los archivos al nuevo directorio?");
            confirmacion.getButtonTypes().setAll(
                    javafx.scene.control.ButtonType.YES,
                    javafx.scene.control.ButtonType.NO);
            confirmacion.showAndWait().ifPresent(respuesta -> {
                if (respuesta == javafx.scene.control.ButtonType.YES) {
                    GestorDocumentos.moverContenido(dirActual, dirNuevo);
                }
            });
        }

        GestorConfig.setRutaDocumentos(dirNuevo.getAbsolutePath());
        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setTitle("Directorio actualizado");
        ok.setHeaderText(null);
        ok.setContentText("Directorio de documentos actualizado correctamente.");
        ok.showAndWait();
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

    @FXML
    private void configurarRecordatorioCorreo(ActionEvent event) {
        TextInputDialog fechaDialog = new TextInputDialog(obtenerFechaRecordatorio());
        fechaDialog.setTitle("Recordatorio correo");
        fechaDialog.setHeaderText("Fecha del recordatorio (dd/MM)");
        fechaDialog.setContentText("Fecha:");

        Optional<String> fechaResultado = fechaDialog.showAndWait();
        if (fechaResultado.isEmpty()) {
            return;
        }

        String fecha = fechaResultado.get().trim();
        if (!fecha.isEmpty()) {
            GestorConfig.setRecordatorioFecha(fecha);
        }

        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setTitle("Recordatorio guardado");
        ok.setHeaderText(null);
        ok.setContentText("Fecha actualizada. Se enviara en la fecha configurada al abrir la app.");
        ok.showAndWait();
    }

    private String obtenerFechaRecordatorio() {
        String configurada = GestorConfig.getRecordatorioFecha();
        return (configurada == null || configurada.isBlank()) ? "20/01" : configurada.trim();
    }
}
