package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controlador del modal de configuración de fechas automáticas.
 * <p>
 * Permite cambiar el día y mes de inicio del período de automatriculación
 * y el día y mes de envío automático de certificados anuales.
 * </p>
 */
public class AjustesFechasController {

    private static final List<String> MESES = List.of(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );

    @FXML private ComboBox<String> comboMesMatricula;
    @FXML private Spinner<Integer> spinnerDiaMatricula;
    @FXML private ComboBox<String> comboMesCertificados;
    @FXML private Spinner<Integer> spinnerDiaCertificados;

    @FXML
    public void initialize() {
        comboMesMatricula.getItems().addAll(MESES);
        comboMesCertificados.getItems().addAll(MESES);

        spinnerDiaMatricula.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 1));
        spinnerDiaMatricula.setEditable(true);
        spinnerDiaCertificados.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 1));
        spinnerDiaCertificados.setEditable(true);

        comboMesMatricula.getSelectionModel().select(GestorConfig.getMatriculasMesInicio() - 1);
        spinnerDiaMatricula.getValueFactory().setValue(GestorConfig.getMatriculasDiaInicio());
        comboMesCertificados.getSelectionModel().select(GestorConfig.getCertificadosMesEnvio() - 1);
        spinnerDiaCertificados.getValueFactory().setValue(GestorConfig.getCertificadosDiaEnvio());
    }

    @FXML
    private void guardar() {
        if (comboMesMatricula.getValue() == null || comboMesCertificados.getValue() == null) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Campo obligatorio", "Debes seleccionar un mes para cada fecha.");
            return;
        }

        int mesMatricula = comboMesMatricula.getSelectionModel().getSelectedIndex() + 1;
        int diaMatricula = spinnerDiaMatricula.getValue();
        int mesCertificados = comboMesCertificados.getSelectionModel().getSelectedIndex() + 1;
        int diaCertificados = spinnerDiaCertificados.getValue();

        GestorConfig.setMatriculasMesInicio(mesMatricula);
        GestorConfig.setMatriculasDiaInicio(diaMatricula);
        GestorConfig.setCertificadosMesEnvio(mesCertificados);
        GestorConfig.setCertificadosDiaEnvio(diaCertificados);

        FxUtils.mostrarAlerta(Alert.AlertType.INFORMATION, "Guardado", "Las fechas se han guardado correctamente.");
        cerrar();
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) comboMesMatricula.getScene().getWindow()).close();
    }
}
