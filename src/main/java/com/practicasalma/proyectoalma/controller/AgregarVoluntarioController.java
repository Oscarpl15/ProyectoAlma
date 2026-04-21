package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Voluntario;
import com.practicasalma.proyectoalma.service.VoluntarioService;
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
 * Controlador JavaFX del formulario de alta de un nuevo voluntario ({@code agregarVoluntario-view.fxml}).
 * <p>
 * Recoge los datos del formulario y los pasa a {@link com.practicasalma.proyectoalma.service.VoluntarioService}.
 * Se abre como modal desde {@link VoluntariosController}.
 * </p>
 */
public class AgregarVoluntarioController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDni;
    @FXML private TextField txtCorreo;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtNacionalidad;
    @FXML private ComboBox<String> comboGenero;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCodigoPostal;
    @FXML private CheckBox checkDelitos;
    @FXML private CheckBox checkProtDatos;

    private final VoluntarioService voluntarioService = new VoluntarioService();

    @FXML
    private void guardarVoluntario(ActionEvent event) {
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
            Voluntario voluntario = new Voluntario(nombre, apellidos, direccion, dni, telefono, correo, fechaNacimiento);
            voluntario.setAutoDelitosSexuales(checkDelitos.isSelected());
            voluntario.setAutoProteccionDatos(checkProtDatos.isSelected());
            String nacionalidad = txtNacionalidad.getText().trim();
            if (!nacionalidad.isEmpty()) voluntario.setNacionalidad(nacionalidad);
            String genero = comboGenero.getValue();
            if (genero != null) voluntario.setGenero(genero);
            String ciudad = txtCiudad.getText().trim();
            if (!ciudad.isEmpty()) voluntario.setCiudad(ciudad);
            String cp = txtCodigoPostal.getText().trim();
            if (!cp.isEmpty()) {
                if (!Validador.esCodigoPostalValido(cp)) {
                    mostrarError("El código postal debe tener exactamente 5 dígitos.");
                    return;
                }
                voluntario.setCodigoPostal(cp);
            }
            voluntarioService.guardarVoluntario(voluntario);
            cerrarVentana(event);
        } catch (Exception e) {
            mostrarError("No se pudo guardar el voluntario: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar(ActionEvent event) {
        cerrarVentana(event);
    }

    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
