package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Voluntario;
import com.practicasalma.proyectoalma.service.VoluntarioService;
import com.practicasalma.proyectoalma.util.FxUtils;
import com.practicasalma.proyectoalma.util.GestorDocumentos;
import com.practicasalma.proyectoalma.util.Validador;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.application.Platform;

import java.io.File;

public class FichaVoluntarioController {

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
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDireccion;
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

    private Voluntario voluntarioActual;
    private String rutaFotoSeleccionada = null;

    private final VoluntarioService voluntarioService = new VoluntarioService();

    @FXML
    public void initialize() {
        comboGenero.getItems().addAll("Masculino", "Femenino", "No binario", "Prefiero no especificar");

        colAsiAnyo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getAnyoAcademico() != null ? c.getValue().getAnyoAcademico() : "—"));
        colAsiGrupo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getGrupoAsignado() != null ? c.getValue().getGrupoAsignado() : "—"));
    }

    public void setVoluntario(Voluntario voluntario) {
        this.voluntarioActual = voluntario;
        cargarDatosEnVista();
        cambiarModoEdicion(false);
        Platform.runLater(() -> btnEditar.requestFocus());
    }

    private void cargarDatosEnVista() {
        txtNombre.setText(voluntarioActual.getNombre() != null ? voluntarioActual.getNombre() : "");
        txtApellidos.setText(voluntarioActual.getApellidos() != null ? voluntarioActual.getApellidos() : "");
        txtDni.setText(voluntarioActual.getDocumentoIdentidad() != null ? voluntarioActual.getDocumentoIdentidad() : "");
        dpFechaNacimiento.setValue(voluntarioActual.getFechaNacimiento());
        txtTelefono.setText(voluntarioActual.getTelefono() != null ? voluntarioActual.getTelefono() : "");
        txtCorreo.setText(voluntarioActual.getCorreo() != null ? voluntarioActual.getCorreo() : "");
        txtDireccion.setText(voluntarioActual.getDireccion() != null ? voluntarioActual.getDireccion() : "");
        txtNacionalidad.setText(voluntarioActual.getNacionalidad() != null ? voluntarioActual.getNacionalidad() : "");

        txtGeneroLectura.setText(voluntarioActual.getGenero() != null ? voluntarioActual.getGenero() : "");
        comboGenero.setValue(voluntarioActual.getGenero());
        txtCiudad.setText(voluntarioActual.getCiudad() != null ? voluntarioActual.getCiudad() : "");
        txtCodigoPostal.setText(voluntarioActual.getCodigoPostal() != null ? voluntarioActual.getCodigoPostal() : "");

        chkDelitosSexuales.setSelected(Boolean.TRUE.equals(voluntarioActual.getAutoDelitosSexuales()));
        chkProteccionDatos.setSelected(Boolean.TRUE.equals(voluntarioActual.getAutoProteccionDatos()));
        actualizarLabelEstado(Boolean.TRUE.equals(voluntarioActual.getActivo()));

        if (voluntarioActual.getHistorialAsignaciones() != null) {
            tablaAsignaciones.setItems(FXCollections.observableArrayList(voluntarioActual.getHistorialAsignaciones()));
        }

        cargarFoto();
        actualizarBotonBajaAlta();
    }

    private void cargarFoto() {
        String ruta = voluntarioActual.getRutaFotoPerfil();
        if (ruta != null && !ruta.isBlank() && new File(ruta).exists()) {
            imgFoto.setImage(new Image(new File(ruta).toURI().toString()));
        } else {
            try {
                imgFoto.setImage(new Image(getClass().getResource(
                        "/com/practicasalma/proyectoalma/assets/default.png").toExternalForm()));
            } catch (Exception e) {
                System.out.println("No se encontró imagen por defecto.");
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
        voluntarioActual.setNombre(txtNombre.getText().trim());
        voluntarioActual.setApellidos(txtApellidos.getText().trim());
        voluntarioActual.setDocumentoIdentidad(txtDni.getText().trim());
        voluntarioActual.setFechaNacimiento(dpFechaNacimiento.getValue());
        voluntarioActual.setTelefono(txtTelefono.getText().trim());
        voluntarioActual.setCorreo(txtCorreo.getText().trim());
        voluntarioActual.setDireccion(txtDireccion.getText().trim());
        voluntarioActual.setNacionalidad(txtNacionalidad.getText().trim());
        voluntarioActual.setGenero(comboGenero.getValue());
        voluntarioActual.setCiudad(txtCiudad.getText().isBlank() ? null : txtCiudad.getText().trim());
        String cp = txtCodigoPostal.getText().trim();
        if (!cp.isBlank() && !Validador.esCodigoPostalValido(cp)) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Código postal inválido", "El código postal debe tener exactamente 5 dígitos.");
            return;
        }
        voluntarioActual.setCodigoPostal(cp.isBlank() ? null : cp);
        voluntarioActual.setAutoDelitosSexuales(chkDelitosSexuales.isSelected());
        voluntarioActual.setAutoProteccionDatos(chkProteccionDatos.isSelected());
        if (rutaFotoSeleccionada != null) {
            voluntarioActual.setRutaFotoPerfil(rutaFotoSeleccionada);
            rutaFotoSeleccionada = null;
        }

        try {
            voluntarioService.actualizarVoluntario(voluntarioActual);
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
            File dir = GestorDocumentos.getDirectorio("Voluntarios", txtNombre.getText(), txtApellidos.getText());
            String ext = obtenerExtension(file.getName());
            if (dir != null) {
                File destino = new File(dir, "foto" + ext);
                try {
                    java.nio.file.Files.copy(file.toPath(), destino.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    rutaFotoSeleccionada = destino.getAbsolutePath();
                } catch (java.io.IOException e) {
                    rutaFotoSeleccionada = file.getAbsolutePath();
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
        boolean activo = Boolean.TRUE.equals(voluntarioActual.getActivo());
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
        boolean activo = Boolean.TRUE.equals(voluntarioActual.getActivo());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(activo ? "Dar de baja" : "Dar de alta");
        confirm.setHeaderText((activo ? "¿Dar de baja a " : "¿Dar de alta a ") + voluntarioActual.getNombre() + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    if (activo) {
                        voluntarioService.darDeBaja(voluntarioActual.getId());
                    } else {
                        voluntarioService.darDeAlta(voluntarioActual.getId());
                    }
                    voluntarioActual = voluntarioService.obtenerCompleto(voluntarioActual.getId());
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
        GestorDocumentos.copiarDocumento(archivo, "Voluntarios", txtNombre.getText(), txtApellidos.getText());
    }

    @FXML
    private void verDocumentos() {
        GestorDocumentos.abrirDirectorio("Voluntarios", txtNombre.getText(), txtApellidos.getText());
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
}
