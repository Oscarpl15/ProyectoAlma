package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.Launcher;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Controlador JavaFX del asistente de configuración inicial ({@code configuracionInicial-view.fxml}).
 * <p>
 * Se muestra la primera vez que se abre la aplicación (o cuando no hay rutas configuradas)
 * para guiar al usuario a seleccionar la ruta de la base de datos y el directorio de documentos.
 * Una vez configurado, guarda las rutas en {@link com.practicasalma.proyectoalma.util.config.GestorConfig}
 * e inicializa Hibernate con la base de datos seleccionada.
 * </p>
 */
public class ConfiguracionInicialController {

    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private TextField txtDirectorioBBDD;
    @FXML private RadioButton rbImportar;
    @FXML private RadioButton rbNueva;
    @FXML private HBox panelImportar;
    @FXML private TextField txtArchivoImportar;
    @FXML private HBox panelNueva;
    @FXML private TextField txtNombreBBDD;
    @FXML private VBox seccionDocumentos;
    @FXML private TextField txtDirectorioDocs;
    @FXML private Button btnConfirmar;

    private Stage stage;
    private boolean modoCambio = false;
    private File directorioSeleccionado;
    private File archivoImportarSeleccionado;

    @FXML
    public void initialize() {
        ToggleGroup grupo = new ToggleGroup();
        rbImportar.setToggleGroup(grupo);
        rbNueva.setToggleGroup(grupo);

        ocultarPanel(panelImportar);
        ocultarPanel(panelNueva);

        grupo.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            ocultarPanel(panelImportar);
            ocultarPanel(panelNueva);
            if (nuevo == rbImportar) mostrarPanel(panelImportar);
            else if (nuevo == rbNueva) mostrarPanel(panelNueva);
            actualizarBotonConfirmar();
        });

        txtNombreBBDD.textProperty().addListener((obs, ant, nv) -> actualizarBotonConfirmar());
        txtDirectorioDocs.textProperty().addListener((obs, ant, nv) -> actualizarBotonConfirmar());
    }

    void prepararModoInicial() {
        lblTitulo.setText("Configuración inicial");
        lblSubtitulo.setText("Configura los directorios de trabajo antes de continuar.");
    }

    void prepararModoCambioBBDD() {
        this.modoCambio = true;
        lblTitulo.setText("Cambiar base de datos");
        lblSubtitulo.setText("Selecciona el directorio y elige entre importar o crear una nueva base de datos.");
        seccionDocumentos.setVisible(false);
        seccionDocumentos.setManaged(false);
        actualizarBotonConfirmar();
    }

    @FXML
    private void seleccionarDirectorioBBDD() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar directorio de base de datos");
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            directorioSeleccionado = dir;
            txtDirectorioBBDD.setText(dir.getAbsolutePath());
            actualizarBotonConfirmar();
        }
    }

    @FXML
    private void seleccionarArchivoImportar() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar base de datos existente");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Base de datos SQLite", "*.db"));
        File archivo = chooser.showOpenDialog(stage);
        if (archivo != null) {
            archivoImportarSeleccionado = archivo;
            txtArchivoImportar.setText(archivo.getAbsolutePath());
            actualizarBotonConfirmar();
        }
    }

    @FXML
    private void seleccionarDirectorioDocs() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar directorio de documentos");
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            txtDirectorioDocs.setText(dir.getAbsolutePath());
            actualizarBotonConfirmar();
        }
    }

    @FXML
    private void confirmar() {
        String rutaBBDD = resolverRutaBBDD();
        if (rutaBBDD == null) return;

        GestorConfig.setRutaBBDD(rutaBBDD);
        if (!modoCambio) {
            GestorConfig.setRutaDocumentos(txtDirectorioDocs.getText().trim());
        }
        stage.close();
    }

    private String resolverRutaBBDD() {
        if (directorioSeleccionado == null) return null;

        if (rbImportar.isSelected()) {
            if (archivoImportarSeleccionado == null) return null;
            File destino = new File(directorioSeleccionado, archivoImportarSeleccionado.getName());
            if (!archivoImportarSeleccionado.getAbsolutePath().equals(destino.getAbsolutePath())) {
                try {
                    Files.copy(archivoImportarSeleccionado.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    mostrarError("No se pudo copiar la base de datos: " + e.getMessage());
                    return null;
                }
            }
            return destino.getAbsolutePath();
        }

        if (rbNueva.isSelected()) {
            String nombre = txtNombreBBDD.getText().trim();
            if (nombre.isBlank()) return null;
            if (!nombre.endsWith(".db")) nombre += ".db";
            return new File(directorioSeleccionado, nombre).getAbsolutePath();
        }

        return null;
    }

    private void actualizarBotonConfirmar() {
        boolean dirOk = directorioSeleccionado != null;
        boolean opcionOk = (rbImportar.isSelected() && archivoImportarSeleccionado != null)
                || (rbNueva.isSelected() && !txtNombreBBDD.getText().trim().isBlank());
        boolean docsOk = modoCambio || !txtDirectorioDocs.getText().trim().isBlank();
        btnConfirmar.setDisable(!(dirOk && opcionOk && docsOk));
    }

    private void mostrarPanel(HBox panel) {
        panel.setVisible(true);
        panel.setManaged(true);
    }

    private void ocultarPanel(HBox panel) {
        panel.setVisible(false);
        panel.setManaged(false);
    }

    private void mostrarError(String mensaje) {
        FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", mensaje);
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    public static void mostrar() {
        abrirDialogo(false);
    }

    public static void mostrarCambioBBDD() {
        abrirDialogo(true);
    }

    private static void abrirDialogo(boolean modoCambio) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Launcher.class.getResource("view/configuracion-inicial-view.fxml"));
            Parent root = loader.load();
            ConfiguracionInicialController ctrl = loader.getController();

            Stage stage = new Stage();
            FxUtils.aplicarIcono(stage);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.setMinWidth(600);
            stage.setMinHeight(420);
            stage.centerOnScreen();

            ctrl.setStage(stage);
            if (modoCambio) {
                ctrl.prepararModoCambioBBDD();
                stage.setTitle("Cambiar base de datos");
            } else {
                ctrl.prepararModoInicial();
                stage.setTitle("Configuracion inicial");
            }

            stage.showAndWait();

        } catch (IOException e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error de configuración", "No se pudo abrir la pantalla de configuración inicial: " + e.getMessage());
        }
    }
}