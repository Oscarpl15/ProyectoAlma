package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.service.GeneradorPdfService;
import com.practicasalma.proyectoalma.service.GestorCorreo;
import com.practicasalma.proyectoalma.service.SocioService;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import com.practicasalma.proyectoalma.util.doc.GestorDocumentos;
import com.practicasalma.proyectoalma.util.validacion.Validador;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Objects;

import javafx.application.Platform;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class FichaSocioController {

    @FXML private ImageView imgFoto;
    @FXML private Button btnCambiarFoto;
    @FXML private Button btnEditar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelarEdicion;
    @FXML private Button btnCerrar;
    @FXML private Button btnEnviarInforme;
    @FXML private Button btnBajaAlta;
    @FXML private Button btnAnadirDonativo;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private Label lblTipoDocLectura;
    @FXML private ComboBox<String> comboTipoDocumento;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtNacionalidad;
    @FXML private TextField txtCuota;
    @FXML private TextField txtCuentaBancaria;
    @FXML private TextField txtCodigoBic;
    @FXML private TextField txtTipoBanco;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCodigoPostal;

    @FXML private TextField txtGeneroLectura;
    @FXML private ComboBox<String> comboGenero;

    @FXML private TextField txtTipoEntidadLectura;
    @FXML private ComboBox<String> comboTipoEntidad;

    @FXML private TextField txtPeriodicidadLectura;
    @FXML private ComboBox<String> comboPeriodicidad;

    @FXML private TextField txtFormaPagoLectura;
    @FXML private ComboBox<String> comboFormaPago;

    @FXML private Label lblEstado;

    @FXML private TableView<Donacion> tablaDonaciones;
    @FXML private TableColumn<Donacion, String> colDonFecha;
    @FXML private TableColumn<Donacion, String> colDonImporte;
    @FXML private TableColumn<Donacion, String> colDonPeriodicidad;
    @FXML private TableColumn<Donacion, String> colDonDescripcion;

    @FXML private Label lblFechaCambioPeriodicidad;
    @FXML private DatePicker dpFechaCambioPeriodicidad;

    private Socio socioActual;
    private String rutaFotoSeleccionada = null;
    private String periodicidadOriginal;

    private final SocioService socioService = new SocioService();
    private final GeneradorPdfService generadorPdfService = new GeneradorPdfService();

    @FXML
    public void initialize() {
        comboGenero.getItems().addAll("Masculino", "Femenino", "No binario", "Prefiero no especificar");
        comboTipoDocumento.getItems().addAll("DNI", "NIE", "Pasaporte");
        comboTipoEntidad.getItems().addAll("Física", "Empresa", "Asociación");
        comboPeriodicidad.getItems().addAll("Mensual", "Trimestral", "Anual", "Puntual");
        comboFormaPago.getItems().addAll("Domiciliación", "Transferencia", "Efectivo");

        colDonFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFecha() != null ? c.getValue().getFecha().toString() : "—"));
        colDonImporte.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getImporte() != null ? c.getValue().getImporte().toPlainString() + " €" : "—"));
        colDonPeriodicidad.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPeriodicidad() != null ? c.getValue().getPeriodicidad() : "—"));
        colDonDescripcion.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFormaDonacion() != null ? c.getValue().getFormaDonacion() : ""));
    }

    public void setSocio(Socio socio) {
        this.socioActual = socio;
        this.periodicidadOriginal = socio.getPeriodicidad();
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
        String tipoDoc = socioActual.getTipoDocumento();
        String tipoMostrar = (tipoDoc != null && !tipoDoc.isBlank()) ? tipoDoc : "DNI";
        comboTipoDocumento.setValue(tipoMostrar);
        lblTipoDocLectura.setText(tipoMostrar + ":");
        txtCiudad.setText(socioActual.getCiudad() != null ? socioActual.getCiudad() : "");
        txtCodigoPostal.setText(socioActual.getCodigoPostal() != null ? socioActual.getCodigoPostal() : "");

        txtTipoEntidadLectura.setText(socioActual.getTipoEntidad() != null ? socioActual.getTipoEntidad() : "");
        comboTipoEntidad.setValue(socioActual.getTipoEntidad());

        txtPeriodicidadLectura.setText(socioActual.getPeriodicidad() != null ? socioActual.getPeriodicidad() : "");
        comboPeriodicidad.setValue(socioActual.getPeriodicidad());

        txtFormaPagoLectura.setText(socioActual.getFormaPago() != null ? socioActual.getFormaPago() : "");
        comboFormaPago.setValue(socioActual.getFormaPago());

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
        txtCuentaBancaria.setEditable(editable);
        txtCodigoBic.setEditable(editable);
        txtTipoBanco.setEditable(editable);
        txtCiudad.setEditable(editable);
        txtCodigoPostal.setEditable(editable);

        // Cuota: editable solo si no es Puntual
        boolean esPuntual = "Puntual".equals(comboPeriodicidad.getValue());
        txtCuota.setEditable(editable && !esPuntual);

        if (editable) {
            comboPeriodicidad.setOnAction(e -> {
                String nuevaPeriodicidad = comboPeriodicidad.getValue();
                boolean puntual = "Puntual".equals(nuevaPeriodicidad);
                txtCuota.setEditable(!puntual);
                if (puntual) txtCuota.setText("0.0");

                boolean cambio = !Objects.equals(nuevaPeriodicidad, periodicidadOriginal);
                boolean mostrarFecha = cambio && !puntual;
                dpFechaCambioPeriodicidad.setVisible(mostrarFecha);
                dpFechaCambioPeriodicidad.setManaged(mostrarFecha);
                lblFechaCambioPeriodicidad.setVisible(mostrarFecha);
                lblFechaCambioPeriodicidad.setManaged(mostrarFecha);
                if (!mostrarFecha) dpFechaCambioPeriodicidad.setValue(null);
            });
        } else {
            comboPeriodicidad.setOnAction(null);
            dpFechaCambioPeriodicidad.setVisible(false);
            dpFechaCambioPeriodicidad.setManaged(false);
            lblFechaCambioPeriodicidad.setVisible(false);
            lblFechaCambioPeriodicidad.setManaged(false);
            dpFechaCambioPeriodicidad.setValue(null);
        }

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

        // Género
        txtGeneroLectura.setVisible(!editable);
        txtGeneroLectura.setManaged(!editable);
        comboGenero.setVisible(editable);
        comboGenero.setManaged(editable);

        // Tipo Documento
        lblTipoDocLectura.setVisible(!editable);
        lblTipoDocLectura.setManaged(!editable);
        comboTipoDocumento.setVisible(editable);
        comboTipoDocumento.setManaged(editable);
        if (!editable) {
            lblTipoDocLectura.setText((comboTipoDocumento.getValue() != null ? comboTipoDocumento.getValue() : "DNI") + ":");
        }

        // Tipo Entidad
        txtTipoEntidadLectura.setVisible(!editable);
        txtTipoEntidadLectura.setManaged(!editable);
        comboTipoEntidad.setVisible(editable);
        comboTipoEntidad.setManaged(editable);

        // Periodicidad
        txtPeriodicidadLectura.setVisible(!editable);
        txtPeriodicidadLectura.setManaged(!editable);
        comboPeriodicidad.setVisible(editable);
        comboPeriodicidad.setManaged(editable);

        // Forma de Pago
        txtFormaPagoLectura.setVisible(!editable);
        txtFormaPagoLectura.setManaged(!editable);
        comboFormaPago.setVisible(editable);
        comboFormaPago.setManaged(editable);
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
        String oldPeriodicidad = socioActual.getPeriodicidad();

        socioActual.setNombre(txtNombre.getText().trim());
        socioActual.setApellidos(txtApellidos.getText().trim());
        socioActual.setDocumentoIdentidad(txtDni.getText().trim());
        socioActual.setTipoDocumento(comboTipoDocumento.getValue());
        socioActual.setTelefono(txtTelefono.getText().trim());
        socioActual.setCorreo(txtCorreo.getText().trim());
        socioActual.setDireccion(txtDireccion.getText().trim());
        socioActual.setNacionalidad(txtNacionalidad.getText().trim());
        socioActual.setGenero(comboGenero.getValue());
        socioActual.setCiudad(txtCiudad.getText().isBlank() ? null : txtCiudad.getText().trim());

        String cp = txtCodigoPostal.getText().trim();
        if (!cp.isBlank() && !Validador.esCodigoPostalValido(cp)) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Código postal inválido", "El código postal debe tener exactamente 5 dígitos.");
            return;
        }
        socioActual.setCodigoPostal(cp.isBlank() ? null : cp);
        socioActual.setTipoEntidad(comboTipoEntidad.getValue());
        socioActual.setFormaPago(comboFormaPago.getValue());
        socioActual.setCuentaBancaria(txtCuentaBancaria.getText().trim());
        socioActual.setCodigoBic(txtCodigoBic.getText().trim());
        socioActual.setTipoBanco(txtTipoBanco.getText().trim());

        if (rutaFotoSeleccionada != null) {
            socioActual.setRutaFotoPerfil(rutaFotoSeleccionada);
            rutaFotoSeleccionada = null;
        }

        String newPeriodicidad = comboPeriodicidad.getValue();
        String cuotaTexto = txtCuota.getText().trim();
        double cuota = 0.0;

        if (!"Puntual".equals(newPeriodicidad) && !cuotaTexto.isEmpty()) {
            try {
                cuota = Double.parseDouble(cuotaTexto);
            } catch (NumberFormatException e) {
                FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Cuota inválida", "Introduce un número válido para la cuota.");
                return;
            }
        }

        boolean periodicidadCambiada = !Objects.equals(newPeriodicidad, periodicidadOriginal);

        // Si cambia a una periodicidad regular, fecha e importe son obligatorios
        if (periodicidadCambiada && !"Puntual".equals(newPeriodicidad)) {
            if (dpFechaCambioPeriodicidad.getValue() == null) {
                FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Fecha obligatoria", "Debes indicar la fecha de inicio de la nueva periodicidad.");
                return;
            }
            if (cuota <= 0) {
                FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Cuota obligatoria", "Debes indicar una cuota mayor que 0 para la periodicidad seleccionada.");
                return;
            }
        }

        // Manejar transiciones de periodicidad
        if ("Puntual".equals(newPeriodicidad) && !"Puntual".equals(oldPeriodicidad)) {
            // Regular → Puntual: desactivar
            socioActual.setActivo(false);
            socioActual.setPeriodicidad("Puntual");
            socioActual.setCuota(0.0);
        } else if (periodicidadCambiada && !"Puntual".equals(newPeriodicidad)) {
            // Cualquier cambio a periodicidad regular: reactivar y generar primera donación
            socioActual.setActivo(true);
            socioActual.setPeriodicidad(newPeriodicidad);
            socioActual.setCuota(cuota);
            LocalDate fechaInicio = dpFechaCambioPeriodicidad.getValue();
            Donacion primera = new Donacion(fechaInicio, BigDecimal.valueOf(cuota), newPeriodicidad, socioActual);
            primera.setFormaDonacion(comboFormaPago.getValue());
            socioActual.addDonacion(primera);
        } else {
            socioActual.setPeriodicidad(newPeriodicidad);
            socioActual.setCuota(cuota);
        }

        try {
            socioService.actualizarSocio(socioActual);
            socioActual = socioService.obtenerCompleto(socioActual.getId());
            periodicidadOriginal = socioActual.getPeriodicidad();
            cambiarModoEdicion(false);
            cargarDatosEnVista();
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar", "No se pudieron guardar los cambios: " + e.getMessage());
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
        btnBajaAlta.setVisible(activo);
        btnBajaAlta.setManaged(activo);
    }

    @FXML
    private void toggleBajaAlta() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Dar de baja");
        confirm.setHeaderText("¿Dar de baja a " + socioActual.getNombre() + "?");
        confirm.setContentText("El socio pasará a inactivo, su periodicidad será Puntual y la cuota quedará a 0.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    socioService.darDeBaja(socioActual.getId());
                    socioActual = socioService.obtenerCompleto(socioActual.getId());
                    cargarDatosEnVista();
                } catch (Exception e) {
                    FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void anadirDonativo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/practicasalma/proyectoalma/view/agregarDonativo-view.fxml"));
            Parent root = loader.load();
            AgregarDonativoController controller = loader.getController();
            controller.setSocio(socioActual);
            Stage stage = new Stage();
            stage.setTitle("Añadir Donativo");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnCerrar.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            socioActual = socioService.obtenerCompleto(socioActual.getId());
            cargarDatosEnVista();
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
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
            String rutaDocumentos = GestorConfig.getRutaDocumentos();
            if (rutaDocumentos == null || rutaDocumentos.isBlank()) {
                FxUtils.mostrarAlerta(Alert.AlertType.WARNING,
                        "Directorio no configurado",
                        "Configura el directorio de documentos en Ajustes antes de generar PDFs.");
                return;
            }

            Path rutaInforme = Paths.get(rutaDocumentos,
                    "Informes", "socios",
                    "informe_socio_" + (socioActual.getId() != null ? socioActual.getId() : "sin_id") + ".pdf");

            generadorPdfService.generarInformeSocioConPlantilla(rutaInforme.toString(), socioActual);

            GestorCorreo.mandarEmailConAdjunto(correoSocio, "Prueba", "Prueba", rutaInforme.toString());

            FxUtils.mostrarAlerta(Alert.AlertType.INFORMATION, "Informe enviado",
                    "Se ha generado y enviado el informe a " + correoSocio + ".");
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error al enviar informe",
                    "No se pudo generar o enviar el informe: " + e.getMessage());
        }
    }
}
