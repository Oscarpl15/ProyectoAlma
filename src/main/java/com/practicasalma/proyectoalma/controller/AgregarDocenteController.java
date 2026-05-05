package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.service.DocenteService;
import com.practicasalma.proyectoalma.util.validacion.Validador;
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

/**
 * Controlador JavaFX del formulario de alta de un nuevo docente ({@code agregarDocente-view.fxml}).
 * <p>
 * Recoge los datos del formulario y los pasa a {@link com.practicasalma.proyectoalma.service.DocenteService}.
 * Se abre como modal desde {@link DocentesController}.
 * </p>
 */
public class AgregarDocenteController {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellidos;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtDni;
    @FXML
    private TextField txtCorreo;

    @FXML private TextField txtTitulacion;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private ComboBox<String> comboTipoDocumento;
    @FXML private TextField txtNacionalidad;
    @FXML private ComboBox<String> comboGenero;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCodigoPostal;

    @FXML private CheckBox checkAuth1;
    @FXML private CheckBox checkProtDatos;

    private final DocenteService docenteService = new DocenteService();

    @FXML
    private void initialize() {
        comboTipoDocumento.getItems().addAll("DNI", "NIE", "Pasaporte");
        comboTipoDocumento.setValue("DNI");
    }

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
            docente.setTipoDocumento(comboTipoDocumento.getValue());
            docente.setAutoDelitosSexuales(checkAuth1.isSelected());
            docente.setAutoProteccionDatos(checkProtDatos.isSelected());
            String titulacion = txtTitulacion.getText().trim();
            if (!titulacion.isEmpty()) docente.setTitulacion(titulacion);
            String nacionalidad = txtNacionalidad.getText().trim();
            if (!nacionalidad.isEmpty()) docente.setNacionalidad(nacionalidad);
            String genero = comboGenero.getValue();
            if (genero != null) docente.setGenero(genero);
            String ciudad = txtCiudad.getText().trim();
            if (!ciudad.isEmpty()) docente.setCiudad(ciudad);
            String cp = txtCodigoPostal.getText().trim();
            if (!cp.isEmpty()) {
                if (!Validador.esCodigoPostalValido(cp)) {
                    mostrarError("El código postal debe tener exactamente 5 dígitos.");
                    return;
                }
                docente.setCodigoPostal(cp);
            }
            docenteService.guardarDocente(docente);
            cerrarVentana(event);
        } catch (Exception e) {
            mostrarError("No se pudo guardar el docente: " + (e.getMessage() != null ? e.getMessage() : "Error inesperado."));
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
