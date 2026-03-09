package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Socio;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FichaSocioController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCuota;

    @FXML private ComboBox<String> cmbTipoEntidad;
    @FXML private ComboBox<String> cmbPeriodicidad;

    @FXML private Button btnGuardar;

    private Socio socioActual;

    @FXML
    public void initialize(){
        cmbTipoEntidad.getItems().addAll("Físico", "Empresa", "Asociacion");
        cmbPeriodicidad.getItems().addAll("Mensual", "Trimestral", "Anual", "Puntual");
    }

    public void setSocio(Socio socio){
        this.socioActual = socio;
        if (socio != null){

            txtNombre.setText(socio.getNombre());
            txtApellidos.setText(socio.getApellidos());
            txtDni.setText(socio.getDocumentoIdentidad());
            txtDireccion.setText(socio.getDireccion());
            txtCuota.setText(String.valueOf(socio.getCuota()));

            btnGuardar.setText("Actualizar Datos");
        }else{
            txtNombre.setText("");
            txtApellidos.setText("");
            txtDni.setText("");
            txtDireccion.setText("");
            txtCuota.setText("");

            btnGuardar.setText("Actualizar Datos");
        }
    }

    @FXML
    private void guardar(){
        cancelar();
    }

    @FXML
    private void cancelar(){
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }
}
