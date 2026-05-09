package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

public class AjustesBackupController {

    @FXML private Spinner<Integer> spinnerDias;

    @FXML
    public void initialize() {
        spinnerDias.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 365, 0));
        spinnerDias.getValueFactory().setValue(GestorConfig.getBackupFrecuencia());
    }

    @FXML
    private void guardar() {
        GestorConfig.setBackupFrecuencia(spinnerDias.getValue());
        FxUtils.mostrarAlerta(Alert.AlertType.INFORMATION, "Guardado",
                "Configuración de copia de seguridad guardada correctamente.");
        cerrar();
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) spinnerDias.getScene().getWindow()).close();
    }
}
