package com.practicasalma.proyectoalma.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AgregarSocioController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtDni;
    @FXML private TextField txtCuota;

    @FXML private ComboBox<String> comboTipoEntidad;
    @FXML private ComboBox<String> comboPeriodicidad;

    @FXML
    private void guardarSocio(ActionEvent event){

        cerrarVentana(event);
    }

    @FXML
    private void cancelar(ActionEvent event) {
        cerrarVentana(event);
    }

    private void cerrarVentana(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
