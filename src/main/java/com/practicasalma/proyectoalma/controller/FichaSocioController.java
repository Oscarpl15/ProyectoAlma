package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.service.GeneradorPdfService;
import com.practicasalma.proyectoalma.service.GestorCorreo;
import com.practicasalma.proyectoalma.service.SocioService;
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
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controlador JavaFX de la ficha de detalle de un socio ({@code fichaSocio-view.fxml}).
 * <p>
 * Permite ver y editar los datos del socio, registrar donaciones y generar el certificado
 * de donaciones (PDF) usando la plantilla oficial de la fundación. Se abre como modal
 * desde {@link SocioController}.
 * </p>
 */
public class FichaSocioController {

    @FXML private ImageView imgFoto;
    @FXML private Button btnCambiarFoto;
    @FXML private Button btnEditar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelarEdicion;
    @FXML private Button btnCerrar;
    @FXML private Button btnEnviarInforme;
    @FXML private Button btnBajaAlta;

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
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCodigoPostal;

    @FXML private TextField txtTipoEntidadLectura;
    @FXML private ComboBox<String> comboTipoEntidad;

    @FXML private TextField txtPeriodicidadLectura;
    @FXML private ComboBox<String> comboPeriodicidad;

    @FXML private Label lblEstado;

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
    private final GeneradorPdfService generadorPdfService = new GeneradorPdfService();

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
        Platform.runLater(() -> btnEditar.requestFocus());
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
        txtCiudad.setText(socioActual.getCiudad() != null ? socioActual.getCiudad() : "");
        txtCodigoPostal.setText(socioActual.getCodigoPostal() != null ? socioActual.getCodigoPostal() : "");

        txtTipoEntidadLectura.setText(socioActual.getTipoEntidad() != null ? socioActual.getTipoEntidad() : "");
        comboTipoEntidad.setValue(socioActual.getTipoEntidad());

        txtPeriodicidadLectura.setText(socioActual.getPeriodicidad() != null ? socioActual.getPeriodicidad() : "");
        comboPeriodicidad.setValue(socioActual.getPeriodicidad());

        actualizarLabelEstado(Boolean.TRUE.equals(socioActual.getActivo()));

        txtCuentaBancaria.setText(socioActual.getCuentaBancaria() != null ? socioActual.getCuentaBancaria() : "");
        txtCodigoBic.setText(socioActual.getCodigoBic() != null ? socioActual.getCodigoBic() : "");
        txtTipoBanco.setText(socioActual.getTipoBanco() != null ? socioActual.getTipoBanco() : "");

        if (socioActual.getDonaciones() != null) {
            tablaDonaciones.setItems(FXCollections.observableArrayList(socioActual.getDonaciones()));
        }

        cargarFoto();
        actualizarBotonBajaAlta();
    }

    private void cargarFoto() {
        String ruta = socioActual.getRutaFotoPerfil();
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
        txtTelefono.setEditable(editable);
        txtCorreo.setEditable(editable);
        txtDireccion.setEditable(editable);
        txtNacionalidad.setEditable(editable);
        txtCuota.setEditable(editable);
        txtCuentaBancaria.setEditable(editable);
        txtCodigoBic.setEditable(editable);
        txtTipoBanco.setEditable(editable);
        txtCiudad.setEditable(editable);
        txtCodigoPostal.setEditable(editable);

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
        socioActual.setNombre(txtNombre.getText().trim());
        socioActual.setApellidos(txtApellidos.getText().trim());
        socioActual.setDocumentoIdentidad(txtDni.getText().trim());
        socioActual.setTelefono(txtTelefono.getText().trim());
        socioActual.setCorreo(txtCorreo.getText().trim());
        socioActual.setDireccion(txtDireccion.getText().trim());
        socioActual.setNacionalidad(txtNacionalidad.getText().trim());
        socioActual.setGenero(comboGenero.getValue());
        socioActual.setCiudad(txtCiudad.getText().isBlank() ? null : txtCiudad.getText().trim());
        String cp = txtCodigoPostal.getText().trim();
        if (!cp.isBlank() && !Validador.esCodigoPostalValido(cp)) {
            FxUtils.mostrarAlerta(javafx.scene.control.Alert.AlertType.WARNING, "Código postal inválido", "El código postal debe tener exactamente 5 dígitos.");
            return;
        }
        socioActual.setCodigoPostal(cp.isBlank() ? null : cp);
        socioActual.setTipoEntidad(comboTipoEntidad.getValue());
        socioActual.setPeriodicidad(comboPeriodicidad.getValue());
        socioActual.setCuentaBancaria(txtCuentaBancaria.getText().trim());
        socioActual.setCodigoBic(txtCodigoBic.getText().trim());
        socioActual.setTipoBanco(txtTipoBanco.getText().trim());
        if (rutaFotoSeleccionada != null) {
            socioActual.setRutaFotoPerfil(rutaFotoSeleccionada);
            rutaFotoSeleccionada = null;
        }

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
            File dir = GestorDocumentos.getDirectorio("Socios", txtNombre.getText(), txtApellidos.getText());
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
        boolean activo = Boolean.TRUE.equals(socioActual.getActivo());
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
        boolean activo = Boolean.TRUE.equals(socioActual.getActivo());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(activo ? "Dar de baja" : "Dar de alta");
        confirm.setHeaderText((activo ? "¿Dar de baja a " : "¿Dar de alta a ") + socioActual.getNombre() + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    if (activo) {
                        socioService.darDeBaja(socioActual.getId());
                    } else {
                        socioService.darDeAlta(socioActual.getId());
                    }
                    socioActual = socioService.obtenerCompleto(socioActual.getId());
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
        GestorDocumentos.copiarDocumento(archivo, "Socios", txtNombre.getText(), txtApellidos.getText());
    }

    @FXML
    private void verDocumentos() {
        GestorDocumentos.abrirDirectorio("Socios", txtNombre.getText(), txtApellidos.getText());
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void enviarInforme() {
        if (socioActual == null) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Sin socio", "No hay socio cargado para enviar el informe.");
            return;
        }

        String correoSocio = socioActual.getCorreo() != null ? socioActual.getCorreo().trim() : "";
        if (correoSocio.isEmpty()) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Correo no disponible", "El socio no tiene correo configurado.");
            return;
        }

        if (!GestorCorreo.estaConfigurado()) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING,
                    "Configurar correo",
                    "Antes de enviar, configura el correo desde el botón 'Correo' en la pantalla principal.");
            return;
        }

        try {
            Path rutaInforme = Paths.get(System.getProperty("user.home"), "Desktop",
                    "informe_socio_" + (socioActual.getId() != null ? socioActual.getId() : "sin_id") + ".pdf");

            generadorPdfService.generarInformeSocioConPlantilla(rutaInforme.toString(), socioActual);

            GestorCorreo.mandarEmailConAdjunto(
                    correoSocio,
                    "Prueba",
                    "Prueba",
                    rutaInforme.toString()
            );

            FxUtils.mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Informe enviado",
                    "Se ha generado y enviado el informe a " + correoSocio + ".");
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR,
                    "Error al enviar informe",
                    "No se pudo generar o enviar el informe: " + e.getMessage());
        }
    }
}
