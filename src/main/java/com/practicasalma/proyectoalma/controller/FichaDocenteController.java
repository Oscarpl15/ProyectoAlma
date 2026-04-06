package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.service.DocenteService;
import com.practicasalma.proyectoalma.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FichaDocenteController {

    @FXML private ImageView imgFoto;
    @FXML private Button btnCambiarFoto;
    @FXML private Button btnEditar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelarEdicion;
    @FXML private Button btnCerrar;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTitulacion;
    @FXML private TextField txtNacionalidad;

    @FXML private TextField txtGeneroLectura;
    @FXML private ComboBox<String> comboGenero;

    @FXML private CheckBox chkDelitosSexuales;
    @FXML private CheckBox chkProteccionDatos;
    @FXML private CheckBox chkActivo;

    @FXML private TableView<AsignacionPersonal> tablaAsignaciones;
    @FXML private TableColumn<AsignacionPersonal, String> colAsiAnyo;
    @FXML private TableColumn<AsignacionPersonal, String> colAsiGrupo;

    private Docente docenteActual;

    private final DocenteService docenteService = new DocenteService();

    @FXML
    public void initialize() {
        comboGenero.getItems().addAll("Masculino", "Femenino", "No binario", "Prefiero no especificar");

        colAsiAnyo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getAnyoAcademico() != null ? c.getValue().getAnyoAcademico() : "—"));
        colAsiGrupo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getGrupoAsignado() != null ? c.getValue().getGrupoAsignado() : "—"));
    }

    public void setDocente(Docente docente) {
        this.docenteActual = docente;
        cargarDatosEnVista();
        cambiarModoEdicion(false);
    }

    private void cargarDatosEnVista() {
        txtNombre.setText(docenteActual.getNombre() != null ? docenteActual.getNombre() : "");
        txtApellidos.setText(docenteActual.getApellidos() != null ? docenteActual.getApellidos() : "");
        txtDni.setText(docenteActual.getDocumentoIdentidad() != null ? docenteActual.getDocumentoIdentidad() : "");
        dpFechaNacimiento.setValue(docenteActual.getFechaNacimiento());
        txtTelefono.setText(docenteActual.getTelefono() != null ? docenteActual.getTelefono() : "");
        txtCorreo.setText(docenteActual.getCorreo() != null ? docenteActual.getCorreo() : "");
        txtDireccion.setText(docenteActual.getDireccion() != null ? docenteActual.getDireccion() : "");
        txtTitulacion.setText(docenteActual.getTitulacion() != null ? docenteActual.getTitulacion() : "");
        txtNacionalidad.setText(docenteActual.getNacionalidad() != null ? docenteActual.getNacionalidad() : "");

        txtGeneroLectura.setText(docenteActual.getGenero() != null ? docenteActual.getGenero() : "");
        comboGenero.setValue(docenteActual.getGenero());

        chkDelitosSexuales.setSelected(Boolean.TRUE.equals(docenteActual.getAutoDelitosSexuales()));
        chkProteccionDatos.setSelected(Boolean.TRUE.equals(docenteActual.getAutoProteccionDatos()));
        chkActivo.setSelected(Boolean.TRUE.equals(docenteActual.getActivo()));

        if (docenteActual.getHistorialAsignaciones() != null) {
            tablaAsignaciones.setItems(FXCollections.observableArrayList(docenteActual.getHistorialAsignaciones()));
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
        dpFechaNacimiento.setDisable(!editable);
        txtTelefono.setEditable(editable);
        txtCorreo.setEditable(editable);
        txtDireccion.setEditable(editable);
        txtTitulacion.setEditable(editable);
        txtNacionalidad.setEditable(editable);
        chkDelitosSexuales.setDisable(!editable);
        chkProteccionDatos.setDisable(!editable);
        chkActivo.setDisable(!editable);

        btnCambiarFoto.setVisible(editable);
        btnCambiarFoto.setManaged(editable);

        btnEditar.setVisible(!editable);
        btnEditar.setManaged(!editable);
        btnCerrar.setVisible(!editable);
        btnCerrar.setManaged(!editable);

        btnGuardar.setVisible(editable);
        btnGuardar.setManaged(editable);
        btnCancelarEdicion.setVisible(editable);
        btnCancelarEdicion.setManaged(editable);

        txtGeneroLectura.setVisible(!editable);
        txtGeneroLectura.setManaged(!editable);
        comboGenero.setVisible(editable);
        comboGenero.setManaged(editable);
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
        docenteActual.setNombre(txtNombre.getText().trim());
        docenteActual.setApellidos(txtApellidos.getText().trim());
        docenteActual.setDocumentoIdentidad(txtDni.getText().trim());
        docenteActual.setFechaNacimiento(dpFechaNacimiento.getValue());
        docenteActual.setTelefono(txtTelefono.getText().trim());
        docenteActual.setCorreo(txtCorreo.getText().trim());
        docenteActual.setDireccion(txtDireccion.getText().trim());
        docenteActual.setTitulacion(txtTitulacion.getText().trim());
        docenteActual.setNacionalidad(txtNacionalidad.getText().trim());
        docenteActual.setGenero(comboGenero.getValue());
        docenteActual.setAutoDelitosSexuales(chkDelitosSexuales.isSelected());
        docenteActual.setAutoProteccionDatos(chkProteccionDatos.isSelected());
        docenteActual.setActivo(chkActivo.isSelected());

        try {
            docenteService.actualizarDocente(docenteActual);
            cambiarModoEdicion(false);
            cargarDatosEnVista();
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR,
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
            imgFoto.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
}
