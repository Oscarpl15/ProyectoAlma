package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.service.SocioService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
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
    @FXML private TextField txtNacionalidad;
    @FXML private ComboBox<String> comboGenero;

    private final SocioService socioService = new SocioService();

    @FXML
    private void guardarSocio(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String dni = txtDni.getText().trim();
        String tipoEntidad = comboTipoEntidad.getValue();
        String periodicidad = comboPeriodicidad.getValue();
        String cuotaTexto = txtCuota.getText().trim();

        if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || tipoEntidad == null) {
            mostrarError("Nombre, apellidos, DNI y tipo de entidad son obligatorios.");
            return;
        }

        double cuota = 0.0;
        if (!cuotaTexto.isEmpty()) {
            try {
                cuota = Double.parseDouble(cuotaTexto.replace(",", "."));
            } catch (NumberFormatException e) {
                mostrarError("La cuota debe ser un número válido.");
                return;
            }
        }

        try {
            Socio socio = new Socio(nombre, apellidos, direccion, dni, tipoEntidad,
                    cuota, periodicidad != null ? periodicidad : "Ninguna");
            String nacionalidad = txtNacionalidad.getText().trim();
            if (!nacionalidad.isEmpty()) socio.setNacionalidad(nacionalidad);
            String genero = comboGenero.getValue();
            if (genero != null) socio.setGenero(genero);
            socioService.guardarSocio(socio);
            cerrarVentana(event);
        } catch (Exception e) {
            mostrarError("No se pudo guardar el socio: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
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
