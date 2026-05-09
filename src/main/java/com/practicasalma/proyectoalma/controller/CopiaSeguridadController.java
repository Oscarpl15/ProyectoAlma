package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.service.BackupService;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class CopiaSeguridadController {

    @FXML private Label lblRutaActual;

    private String rutaSeleccionada;
    private final BackupService backupService = new BackupService();

    @FXML
    public void initialize() {
        rutaSeleccionada = GestorConfig.getBackupRuta();
        actualizarLabelRuta();
    }

    @FXML
    private void cambiarDestino() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar destino de la copia de seguridad");
        if (rutaSeleccionada != null && !rutaSeleccionada.isBlank()) {
            File actual = new File(rutaSeleccionada);
            if (actual.exists()) chooser.setInitialDirectory(actual);
        }
        File dir = chooser.showDialog(lblRutaActual.getScene().getWindow());
        if (dir != null) {
            rutaSeleccionada = dir.getAbsolutePath();
            actualizarLabelRuta();
        }
    }

    @FXML
    private void aceptar() {
        if (rutaSeleccionada == null || rutaSeleccionada.isBlank()) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Destino no configurado",
                    "Selecciona un destino para la copia de seguridad.");
            return;
        }
        File dir = new File(rutaSeleccionada);
        if (!dir.exists() || !dir.isDirectory()) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Destino inválido",
                    "El destino seleccionado no existe o no es una carpeta válida.");
            return;
        }
        try {
            backupService.realizarCopia(rutaSeleccionada);
            GestorConfig.setBackupRuta(rutaSeleccionada);
            backupService.registrarFechaHoy();
            FxUtils.mostrarAlerta(Alert.AlertType.INFORMATION, "Copia realizada",
                    "La copia de seguridad se ha completado correctamente.");
            cerrar();
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error en la copia",
                    "No se pudo completar la copia de seguridad: " + e.getMessage());
        }
    }

    @FXML
    private void posponer() {
        backupService.registrarFechaHoy();
        cerrar();
    }

    private void actualizarLabelRuta() {
        lblRutaActual.setText(rutaSeleccionada != null && !rutaSeleccionada.isBlank()
                ? rutaSeleccionada : "No configurado");
    }

    private void cerrar() {
        ((Stage) lblRutaActual.getScene().getWindow()).close();
    }
}
