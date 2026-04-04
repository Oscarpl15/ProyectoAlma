package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.service.DocenteService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AgregarDocenteController {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellidos;
    @FXML
    private TextField txtDireccion; // Nuevo campo
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtDni;
    @FXML
    private TextField txtCorreo;

    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtNacionalidad;
    @FXML private ComboBox<String> comboGenero;

    // Checkboxes
    @FXML private CheckBox checkAuth1;

    private final DocenteService docenteService = new DocenteService();

    @FXML
    private void guardarDocente(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String dni = txtDni.getText().trim();
        String correo = txtCorreo.getText().trim();
        LocalDate fechaNacimiento = dpFechaNacimiento.getValue();

        if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || fechaNacimiento == null) {
            mostrarError("Nombre, apellidos, DNI y fecha de nacimiento son obligatorios.");
            return;
        }

        try {
            Docente docente = new Docente(nombre, apellidos, direccion, dni, telefono, correo, fechaNacimiento);
            docente.setAutoDelitosSexuales(checkAuth1.isSelected());
            String nacionalidad = txtNacionalidad.getText().trim();
            if (!nacionalidad.isEmpty()) docente.setNacionalidad(nacionalidad);
            String genero = comboGenero.getValue();
            if (genero != null) docente.setGenero(genero);
            docenteService.guardarDocente(docente);
            cerrarVentana(event);
        } catch (Exception e) {
            mostrarError("No se pudo guardar el docente: " + e.getMessage());
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
