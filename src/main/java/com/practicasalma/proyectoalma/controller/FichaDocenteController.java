package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.service.DocenteService;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import com.practicasalma.proyectoalma.util.doc.GestorDocumentos;
import com.practicasalma.proyectoalma.util.validacion.Validador;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.application.Platform;

import java.io.File;

/**
 * Controlador JavaFX de la ficha de detalle de un docente ({@code fichaDocente-view.fxml}).
 * <p>
 * Permite ver y editar los datos del docente, su historial de asignaciones por curso
 * académico y su documentación adjunta (certificados, LOPD). Se abre como modal desde
 * {@link DocentesController}.
 * </p>
 */
public class FichaDocenteController {

    @FXML private ImageView imgFoto;
    @FXML private Button btnCambiarFoto;
    @FXML private Button btnEditar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelarEdicion;
    @FXML private Button btnCerrar;
    @FXML private Button btnBajaAlta;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private Label lblTipoDocLectura;
    @FXML private ComboBox<String> comboTipoDocumento;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTitulacion;
    @FXML private TextField txtNacionalidad;

    @FXML private TextField txtGeneroLectura;
    @FXML private ComboBox<String> comboGenero;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCodigoPostal;

    @FXML private CheckBox chkDelitosSexuales;
    @FXML private CheckBox chkProteccionDatos;
    @FXML private Label lblEstado;

    @FXML private TableView<AsignacionPersonal> tablaAsignaciones;
    @FXML private TableColumn<AsignacionPersonal, String> colAsiAnyo;
    @FXML private TableColumn<AsignacionPersonal, String> colAsiGrupo;

    private Docente docenteActual;
    private String rutaFotoSeleccionada = null;

    private final DocenteService docenteService = new DocenteService();

    @FXML
    public void initialize() {
        comboGenero.getItems().addAll("Masculino", "Femenino", "No binario", "Prefiero no especificar");
        comboTipoDocumento.getItems().addAll("DNI", "NIE", "Pasaporte");

        colAsiAnyo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getAnyoAcademico() != null ? c.getValue().getAnyoAcademico() : "—"));
        colAsiGrupo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getGrupoAsignado() != null ? c.getValue().getGrupoAsignado() : "—"));
    }

    public void setDocente(Docente docente) {
        this.docenteActual = docente;
        cargarDatosEnVista();
        cambiarModoEdicion(false);
        Platform.runLater(() -> btnEditar.requestFocus());
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
        String tipoDoc = docenteActual.getTipoDocumento();
        String tipoMostrar = (tipoDoc != null && !tipoDoc.isBlank()) ? tipoDoc : "DNI";
        comboTipoDocumento.setValue(tipoMostrar);
        lblTipoDocLectura.setText(tipoMostrar + ":");
        txtCiudad.setText(docenteActual.getCiudad() != null ? docenteActual.getCiudad() : "");
        txtCodigoPostal.setText(docenteActual.getCodigoPostal() != null ? docenteActual.getCodigoPostal() : "");

        chkDelitosSexuales.setSelected(Boolean.TRUE.equals(docenteActual.getAutoDelitosSexuales()));
        chkProteccionDatos.setSelected(Boolean.TRUE.equals(docenteActual.getAutoProteccionDatos()));
        actualizarLabelEstado(Boolean.TRUE.equals(docenteActual.getActivo()));

        if (docenteActual.getHistorialAsignaciones() != null) {
            tablaAsignaciones.setItems(FXCollections.observableArrayList(docenteActual.getHistorialAsignaciones()));
        }

        cargarFoto();
        actualizarBotonBajaAlta();
    }

    private void cargarFoto() {
        String ruta = docenteActual.getRutaFotoPerfil();
        if (ruta != null && !ruta.isBlank() && new File(ruta).exists()) {
            imgFoto.setImage(new Image(new File(ruta).toURI().toString()));
        } else {
            try {
                imgFoto.setImage(new Image(getClass().getResource(
                        "/com/practicasalma/proyectoalma/assets/default.png").toExternalForm()));
            } catch (Exception ignored) {
            }
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
        txtCiudad.setEditable(editable);
        txtCodigoPostal.setEditable(editable);
        actualizarVistaCheckbox(chkDelitosSexuales, "Cert. Delitos Sexuales", editable);
        actualizarVistaCheckbox(chkProteccionDatos, "Cert. Protección Datos", editable);

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

        lblTipoDocLectura.setVisible(!editable);
        lblTipoDocLectura.setManaged(!editable);
        comboTipoDocumento.setVisible(editable);
        comboTipoDocumento.setManaged(editable);
        if (!editable) {
            lblTipoDocLectura.setText((comboTipoDocumento.getValue() != null ? comboTipoDocumento.getValue() : "DNI") + ":");
        }
    }

    private void actualizarLabelEstado(boolean activo) {
        lblEstado.getStyleClass().removeAll("celda-activo", "celda-baja");
        if (activo) {
            lblEstado.setText("✔  Activo");
            lblEstado.getStyleClass().add("celda-activo");
        } else {
            lblEstado.setText("✘  Inactivo");
            lblEstado.getStyleClass().add("celda-baja");
        }
    }

    private void actualizarVistaCheckbox(CheckBox chk, String textoBase, boolean editable) {
        chk.setDisable(!editable);
        chk.getStyleClass().removeAll("check-lectura-true", "check-lectura-false");
        if (!editable) {
            if (chk.isSelected()) {
                chk.setText("✔  " + textoBase);
                chk.getStyleClass().add("check-lectura-true");
            } else {
                chk.setText("✘  " + textoBase);
                chk.getStyleClass().add("check-lectura-false");
            }
        } else {
            chk.setText(textoBase);
        }
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
        docenteActual.setTipoDocumento(comboTipoDocumento.getValue());
        docenteActual.setFechaNacimiento(dpFechaNacimiento.getValue());
        docenteActual.setTelefono(txtTelefono.getText().trim());
        docenteActual.setCorreo(txtCorreo.getText().trim());
        docenteActual.setDireccion(txtDireccion.getText().trim());
        docenteActual.setTitulacion(txtTitulacion.getText().trim());
        docenteActual.setNacionalidad(txtNacionalidad.getText().trim());
        docenteActual.setGenero(comboGenero.getValue());
        docenteActual.setCiudad(txtCiudad.getText().isBlank() ? null : txtCiudad.getText().trim());
        String cp = txtCodigoPostal.getText().trim();
        if (!cp.isBlank() && !Validador.esCodigoPostalValido(cp)) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Código postal inválido", "El código postal debe tener exactamente 5 dígitos.");
            return;
        }
        docenteActual.setCodigoPostal(cp.isBlank() ? null : cp);
        docenteActual.setAutoDelitosSexuales(chkDelitosSexuales.isSelected());
        docenteActual.setAutoProteccionDatos(chkProteccionDatos.isSelected());
        if (rutaFotoSeleccionada != null) {
            docenteActual.setRutaFotoPerfil(rutaFotoSeleccionada);
            rutaFotoSeleccionada = null;
        }

        try {
            docenteService.actualizarDocente(docenteActual);
            cambiarModoEdicion(false);
            cargarDatosEnVista();
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR,
                    "Error al guardar", "No se pudieron guardar los cambios: " + (e.getMessage() != null ? e.getMessage() : "Error inesperado."));
        }
    }

    @FXML
    private void seleccionarFoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar foto");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(imgFoto.getScene().getWindow());
        if (file != null) {
            File dir = GestorDocumentos.getDirectorio("Docentes", txtNombre.getText(), txtApellidos.getText());
            String ext = obtenerExtension(file.getName());
            if (dir != null) {
                File destino = new File(dir, "foto" + ext);
                try {
                    java.nio.file.Files.copy(file.toPath(), destino.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    rutaFotoSeleccionada = destino.getAbsolutePath();
                } catch (java.io.IOException e) {
                    rutaFotoSeleccionada = file.getAbsolutePath();
                    FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Foto no copiada",
                            "No se pudo copiar la foto al directorio de documentos. Se usará la ubicación original.");
                }
            } else {
                rutaFotoSeleccionada = file.getAbsolutePath();
            }
            imgFoto.setImage(new Image(new File(rutaFotoSeleccionada).toURI().toString()));
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        int punto = nombreArchivo.lastIndexOf('.');
        return punto >= 0 ? nombreArchivo.substring(punto) : "";
    }

    private void actualizarBotonBajaAlta() {
        boolean activo = Boolean.TRUE.equals(docenteActual.getActivo());
        btnBajaAlta.getStyleClass().removeAll("boton-baja", "boton-guardar-exito");
        if (activo) {
            btnBajaAlta.setText("Dar de baja");
            btnBajaAlta.getStyleClass().add("boton-baja");
        } else {
            btnBajaAlta.setText("Dar de alta");
            btnBajaAlta.getStyleClass().add("boton-guardar-exito");
        }
    }

    @FXML
    private void toggleBajaAlta() {
        boolean activo = Boolean.TRUE.equals(docenteActual.getActivo());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(activo ? "Dar de baja" : "Dar de alta");
        confirm.setHeaderText((activo ? "¿Dar de baja a " : "¿Dar de alta a ") + docenteActual.getNombre() + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    if (activo) {
                        docenteService.darDeBaja(docenteActual.getId());
                    } else {
                        docenteService.darDeAlta(docenteActual.getId());
                    }
                    docenteActual = docenteService.obtenerCompleto(docenteActual.getId());
                    cargarDatosEnVista();
                } catch (Exception e) {
                    FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void anadirDocumento() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar documento");
        File archivo = chooser.showOpenDialog(btnCerrar.getScene().getWindow());
        if (archivo == null) return;
        GestorDocumentos.copiarDocumento(archivo, "Docentes", txtNombre.getText(), txtApellidos.getText());
    }

    @FXML
    private void verDocumentos() {
        GestorDocumentos.abrirDirectorio("Docentes", txtNombre.getText(), txtApellidos.getText());
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
}
