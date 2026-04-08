package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.service.SocioService;
import com.practicasalma.proyectoalma.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FichaSocioController {

    @FXML private ImageView imgFoto;
    @FXML private Button btnCambiarFoto;
    @FXML private Button btnEditar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelarEdicion;
    @FXML private Button btnCerrar;
    @FXML private Button btnEnviarInforme;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtNacionalidad;
    @FXML private TextField txtCuota;

    @FXML private TextField txtGeneroLectura;
    @FXML private ComboBox<String> comboGenero;

    @FXML private TextField txtTipoEntidadLectura;
    @FXML private ComboBox<String> comboTipoEntidad;

    @FXML private TextField txtPeriodicidadLectura;
    @FXML private ComboBox<String> comboPeriodicidad;

    @FXML private CheckBox chkActivo;

    @FXML private TextField txtCuentaBancaria;
    @FXML private TextField txtCodigoBic;
    @FXML private TextField txtTipoBanco;

    @FXML private TableView<Donacion> tablaDonaciones;
    @FXML private TableColumn<Donacion, String> colDonFecha;
    @FXML private TableColumn<Donacion, String> colDonImporte;
    @FXML private TableColumn<Donacion, String> colDonDescripcion;

    private Socio socioActual;
    private String rutaFotoSeleccionada = null;

    private final SocioService socioService = new SocioService();

    @FXML
    public void initialize() {
        comboGenero.getItems().addAll("Masculino", "Femenino", "No binario", "Prefiero no especificar");
        comboTipoEntidad.getItems().addAll("Física", "Empresa", "Asociación");
        comboPeriodicidad.getItems().addAll("Mensual", "Trimestral", "Anual", "Puntual");

        colDonFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFecha() != null ? c.getValue().getFecha().toString() : "—"));
        colDonImporte.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getImporte() != null ? c.getValue().getImporte().toPlainString() + " €" : "—"));
        colDonDescripcion.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFormaDonacion() != null ? c.getValue().getFormaDonacion() : ""));
    }

    public void setSocio(Socio socio) {
        this.socioActual = socio;
        cargarDatosEnVista();
        cambiarModoEdicion(false);
    }

    private void cargarDatosEnVista() {
        txtNombre.setText(socioActual.getNombre() != null ? socioActual.getNombre() : "");
        txtApellidos.setText(socioActual.getApellidos() != null ? socioActual.getApellidos() : "");
        txtDni.setText(socioActual.getDocumentoIdentidad() != null ? socioActual.getDocumentoIdentidad() : "");
        txtTelefono.setText(socioActual.getTelefono() != null ? socioActual.getTelefono() : "");
        txtCorreo.setText(socioActual.getCorreo() != null ? socioActual.getCorreo() : "");
        txtDireccion.setText(socioActual.getDireccion() != null ? socioActual.getDireccion() : "");
        txtNacionalidad.setText(socioActual.getNacionalidad() != null ? socioActual.getNacionalidad() : "");
        txtCuota.setText(socioActual.getCuota() != null ? String.valueOf(socioActual.getCuota()) : "");

        txtGeneroLectura.setText(socioActual.getGenero() != null ? socioActual.getGenero() : "");
        comboGenero.setValue(socioActual.getGenero());

        txtTipoEntidadLectura.setText(socioActual.getTipoEntidad() != null ? socioActual.getTipoEntidad() : "");
        comboTipoEntidad.setValue(socioActual.getTipoEntidad());

        txtPeriodicidadLectura.setText(socioActual.getPeriodicidad() != null ? socioActual.getPeriodicidad() : "");
        comboPeriodicidad.setValue(socioActual.getPeriodicidad());

        chkActivo.setSelected(Boolean.TRUE.equals(socioActual.getActivo()));

        txtCuentaBancaria.setText(socioActual.getCuentaBancaria() != null ? socioActual.getCuentaBancaria() : "");
        txtCodigoBic.setText(socioActual.getCodigoBic() != null ? socioActual.getCodigoBic() : "");
        txtTipoBanco.setText(socioActual.getTipoBanco() != null ? socioActual.getTipoBanco() : "");

        if (socioActual.getDonaciones() != null) {
            tablaDonaciones.setItems(FXCollections.observableArrayList(socioActual.getDonaciones()));
        }

        cargarFoto();
    }

    private void cargarFoto() {
        try {
            imgFoto.setImage(new Image(getClass().getResource(
                    "/com/practicasalma/proyectoalma/assets/default.png").toExternalForm()));
        } catch (Exception e) {
            System.out.println("No se encontró imagen por defecto.");
        }
    }

    private void cambiarModoEdicion(boolean editable) {
        txtNombre.setEditable(editable);
        txtApellidos.setEditable(editable);
        txtDni.setEditable(editable);
        txtTelefono.setEditable(editable);
        txtCorreo.setEditable(editable);
        txtDireccion.setEditable(editable);
        txtNacionalidad.setEditable(editable);
        txtCuota.setEditable(editable);
        txtCuentaBancaria.setEditable(editable);
        txtCodigoBic.setEditable(editable);
        txtTipoBanco.setEditable(editable);
        chkActivo.setDisable(!editable);

        btnCambiarFoto.setVisible(editable);
        btnCambiarFoto.setManaged(editable);

        btnEditar.setVisible(!editable);
        btnEditar.setManaged(!editable);
        btnCerrar.setVisible(!editable);
        btnCerrar.setManaged(!editable);
        btnEnviarInforme.setVisible(!editable);
        btnEnviarInforme.setManaged(!editable);

        btnGuardar.setVisible(editable);
        btnGuardar.setManaged(editable);
        btnCancelarEdicion.setVisible(editable);
        btnCancelarEdicion.setManaged(editable);

        // Género: intercambiar label/combo
        txtGeneroLectura.setVisible(!editable);
        txtGeneroLectura.setManaged(!editable);
        comboGenero.setVisible(editable);
        comboGenero.setManaged(editable);

        // Tipo Entidad: intercambiar label/combo
        txtTipoEntidadLectura.setVisible(!editable);
        txtTipoEntidadLectura.setManaged(!editable);
        comboTipoEntidad.setVisible(editable);
        comboTipoEntidad.setManaged(editable);

        // Periodicidad: intercambiar label/combo
        txtPeriodicidadLectura.setVisible(!editable);
        txtPeriodicidadLectura.setManaged(!editable);
        comboPeriodicidad.setVisible(editable);
        comboPeriodicidad.setManaged(editable);
    }

    @FXML
    private void activarEdicion() {
        cambiarModoEdicion(true);
    }

    @FXML
    private void cancelarEdicion() {
        cargarDatosEnVista();
        cambiarModoEdicion(false);
    }

    @FXML
    private void guardarDatos() {
        socioActual.setNombre(txtNombre.getText().trim());
        socioActual.setApellidos(txtApellidos.getText().trim());
        socioActual.setDocumentoIdentidad(txtDni.getText().trim());
        socioActual.setTelefono(txtTelefono.getText().trim());
        socioActual.setCorreo(txtCorreo.getText().trim());
        socioActual.setDireccion(txtDireccion.getText().trim());
        socioActual.setNacionalidad(txtNacionalidad.getText().trim());
        socioActual.setGenero(comboGenero.getValue());
        socioActual.setTipoEntidad(comboTipoEntidad.getValue());
        socioActual.setPeriodicidad(comboPeriodicidad.getValue());
        socioActual.setActivo(chkActivo.isSelected());
        socioActual.setCuentaBancaria(txtCuentaBancaria.getText().trim());
        socioActual.setCodigoBic(txtCodigoBic.getText().trim());
        socioActual.setTipoBanco(txtTipoBanco.getText().trim());

        String cuotaTexto = txtCuota.getText().trim();
        if (!cuotaTexto.isEmpty()) {
            try {
                socioActual.setCuota(Double.parseDouble(cuotaTexto));
            } catch (NumberFormatException e) {
                FxUtils.mostrarAlerta(javafx.scene.control.Alert.AlertType.WARNING,
                        "Cuota inválida", "Introduce un número válido para la cuota.");
                return;
            }
        }

        try {
            socioService.actualizarSocio(socioActual);
            cambiarModoEdicion(false);
            cargarDatosEnVista();
        } catch (Exception e) {
            FxUtils.mostrarAlerta(javafx.scene.control.Alert.AlertType.ERROR,
                    "Error al guardar", "No se pudieron guardar los cambios: " + e.getMessage());
        }
    }

    @FXML
    private void seleccionarFoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar foto");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(imgFoto.getScene().getWindow());
        if (file != null) {
            rutaFotoSeleccionada = file.getAbsolutePath();
            imgFoto.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void enviarInforme() {
        FxUtils.mostrarAlerta(Alert.AlertType.INFORMATION,
                "Enviar informe",
                "Botón disponible. La funcionalidad de envío se implementará a continuación.");
    }
}
