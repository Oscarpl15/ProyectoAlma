package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.service.SocioService;
import com.practicasalma.proyectoalma.util.validacion.Validador;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AgregarSocioController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCuota;

    @FXML private ComboBox<String> comboTipoDocumento;
    @FXML private ComboBox<String> comboTipoEntidad;
    @FXML private ComboBox<String> comboPeriodicidad;
    @FXML private ComboBox<String> comboFormaPago;
    @FXML private TextField txtNacionalidad;
    @FXML private ComboBox<String> comboGenero;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCodigoPostal;
    @FXML private TextField txtProvincia;
    @FXML private DatePicker dpFechaDonacion;

    private final SocioService socioService = new SocioService();

    @FXML
    public void initialize() {
        comboTipoDocumento.getItems().addAll("DNI", "NIE", "Pasaporte");
        comboTipoDocumento.setValue("DNI");
    }

    @FXML
    private void guardarSocio(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String dni = txtDni.getText().trim();
        String correo = txtCorreo.getText().trim();
        String ciudad = txtCiudad.getText().trim();
        String cp = txtCodigoPostal.getText().trim();
        String provincia = txtProvincia.getText().trim();
        String tipoEntidad = comboTipoEntidad.getValue();
        String periodicidad = comboPeriodicidad.getValue();
        String cuotaTexto = txtCuota.getText().trim();

        if (nombre.isEmpty()) { mostrarError("El nombre es obligatorio."); return; }
        if (apellidos.isEmpty()) { mostrarError("Los apellidos son obligatorios."); return; }
        if (dni.isEmpty()) { mostrarError("El documento de identidad es obligatorio."); return; }
        if (tipoEntidad == null) { mostrarError("El tipo de entidad es obligatorio."); return; }
        if (correo.isEmpty()) { mostrarError("El correo electrónico es obligatorio."); return; }
        if (!Validador.esEmailValido(correo)) { mostrarError("El correo electrónico no tiene un formato válido."); return; }
        if (direccion.isEmpty()) { mostrarError("La dirección es obligatoria."); return; }
        if (cp.isEmpty()) { mostrarError("El código postal es obligatorio."); return; }
        if (!Validador.esCodigoPostalValido(cp)) { mostrarError("El código postal debe tener exactamente 5 dígitos."); return; }
        if (ciudad.isEmpty()) { mostrarError("La ciudad es obligatoria."); return; }
        if (provincia.isEmpty()) { mostrarError("La provincia es obligatoria."); return; }

        boolean esPuntual = "Puntual".equals(periodicidad) || periodicidad == null;
        double importe = 0.0;
        if (!cuotaTexto.isEmpty()) {
            try {
                importe = Double.parseDouble(cuotaTexto.replace(",", "."));
            } catch (NumberFormatException e) {
                mostrarError("La cuota debe ser un número válido.");
                return;
            }
        }

        // Si se selecciona una periodicidad, fecha e importe son obligatorios
        if (periodicidad != null && !periodicidad.isBlank()) {
            if (dpFechaDonacion.getValue() == null) {
                mostrarError("Debes indicar la fecha de la donación.");
                return;
            }
            if (importe <= 0) {
                mostrarError("Debes indicar una cuota o importe mayor que 0.");
                return;
            }
        }

        LocalDate fechaDonacion = dpFechaDonacion.getValue();

        try {
            // Para puntual: la cuota recurrente del socio es 0; el importe va a la donación
            Socio socio = new Socio(nombre, apellidos, direccion, dni, tipoEntidad,
                    esPuntual ? 0.0 : importe, periodicidad != null ? periodicidad : "Puntual");
            String telefono = txtTelefono.getText().trim();
            if (!telefono.isEmpty()) socio.setTelefono(telefono);
            socio.setCorreo(correo);
            String nacionalidad = txtNacionalidad.getText().trim();
            if (!nacionalidad.isEmpty()) socio.setNacionalidad(nacionalidad);
            String genero = comboGenero.getValue();
            if (genero != null) socio.setGenero(genero);
            socio.setCiudad(ciudad);
            socio.setCodigoPostal(cp);
            socio.setProvincia(provincia);
            socio.setTipoDocumento(comboTipoDocumento.getValue());
            String formaPago = comboFormaPago.getValue();
            if (formaPago != null) socio.setFormaPago(formaPago);

            if (esPuntual) {
                socio.setActivo(false);
                Donacion puntual = new Donacion(fechaDonacion, BigDecimal.valueOf(importe), "Puntual", socio);
                puntual.setFormaDonacion(formaPago);
                socio.addDonacion(puntual);
            } else {
                Donacion primera = new Donacion(fechaDonacion, BigDecimal.valueOf(importe), periodicidad, socio);
                primera.setFormaDonacion(formaPago);
                socio.addDonacion(primera);
            }

            socioService.guardarSocio(socio);
            cerrarVentana(event);
        } catch (Exception e) {
            mostrarError("No se pudo guardar el socio: " + (e.getMessage() != null ? e.getMessage() : "Error inesperado."));
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
