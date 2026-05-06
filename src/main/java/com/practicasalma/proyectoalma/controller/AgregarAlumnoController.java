package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.service.AlumnoService;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.doc.GestorDocumentos;
import com.practicasalma.proyectoalma.util.validacion.Validador;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;

/**
 * Controlador JavaFX del formulario de alta de un nuevo alumno ({@code agregarAlumno-view.fxml}).
 * <p>
 * Recoge los datos del formulario y los pasa a {@link com.practicasalma.proyectoalma.service.AlumnoService}
 * para persistirlos. También permite adjuntar una foto de perfil al guardar.
 * Se abre como modal desde {@link AlumnosController}.
 * </p>
 */
public class AgregarAlumnoController {

    // Datos Personales
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;

    @FXML private ComboBox<String> comboTipoDocumento;

    // Datos Escolares / Sociales
    @FXML private TextField txtColegio;
    @FXML private ComboBox<String> comboCurso;
    @FXML private ComboBox<String> comboGrupo;

    // Opcionales
    @FXML private TextField txtNacionalidad;
    @FXML private ComboBox<String> comboGenero;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCodigoPostal;

    // Seguimiento
    @FXML private CheckBox chkSeguimientoSS;
    @FXML private CheckBox chkSeguimientoSAF;

    // Derivación
    @FXML private CheckBox chkDerivacionSS;
    @FXML private CheckBox chkDerivacionSAF;
    @FXML private CheckBox chkDerivacionEOEP;
    @FXML private CheckBox chkDerivacionColegio;
    @FXML private CheckBox chkDerivacionOtro;
    @FXML private TextField txtDerivado;

    // Autorizaciones Legales
    @FXML private CheckBox chkAutoDatos;
    @FXML private CheckBox chkAutoImagen;
    @FXML private CheckBox chkAutoActividades;
    @FXML private CheckBox chkAutoComunicaciones;
    @FXML private CheckBox chkAutoIrseSolo;

    @FXML private ImageView fotoAlumno;
    private final String RUTA_DEFECTO_IMAGEN = "/com/practicasalma/proyectoalma/assets/default.png";
    private String rutaFotoSeleccionada = null;
    private File fotoArchivoSeleccionado = null;

    private final AlumnoService alumnoService = new AlumnoService();

    @FXML
    private void initialize() {
        comboTipoDocumento.getItems().addAll("DNI", "NIE", "Pasaporte");
        comboTipoDocumento.setValue("DNI");
        comboGrupo.getItems().addAll(GestorConfig.getGrupos());
        try {
            String rutaDefault = getClass().getResource(RUTA_DEFECTO_IMAGEN).toExternalForm();
            fotoAlumno.setImage(new Image(rutaDefault));
        } catch (NullPointerException ignored) {
        }
    }

    @FXML
    private void guardarAlumno(ActionEvent event) {
        String nombre = limpiarTexto(txtNombre.getText());
        String apellidos = limpiarTexto(txtApellidos.getText());
        String curso = comboCurso.getValue();
        String grupo = comboGrupo.getValue();
        String cursoNormalizado = limpiarTexto(curso);
        LocalDate fechaNac = dpFechaNacimiento.getValue();
        String dni = limpiarTexto(txtDni.getText());
        String telefono = limpiarTexto(txtTelefono.getText());

        if (nombre == null || apellidos == null || fechaNac == null || cursoNormalizado == null) {
            mostrarMensaje("Datos incompletos", "Nombre, apellidos, fecha de nacimiento y curso son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        Alumno alumno = new Alumno(nombre, apellidos, limpiarTexto(txtDireccion.getText()), fechaNac);
        alumno.setTelefono(telefono);
        alumno.setColegio(limpiarTexto(txtColegio.getText()));
        alumno.setDerivadoPor(limpiarTexto(txtDerivado.getText()));
        if (fotoArchivoSeleccionado != null) {
            File dir = GestorDocumentos.getDirectorio("Alumnos", nombre, apellidos);
            if (dir != null) {
                String ext = obtenerExtension(fotoArchivoSeleccionado.getName());
                File destino = new File(dir, "foto" + ext);
                try {
                    java.nio.file.Files.copy(fotoArchivoSeleccionado.toPath(), destino.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    rutaFotoSeleccionada = destino.getAbsolutePath();
                } catch (java.io.IOException e) {
                    rutaFotoSeleccionada = fotoArchivoSeleccionado.getAbsolutePath();
                    mostrarMensaje("Foto no copiada", "No se pudo copiar la foto al directorio de documentos. Se usará la ubicación original.", Alert.AlertType.WARNING);
                }
            }
        }
        alumno.setRutaFotoPerfil(rutaFotoSeleccionada);

        if (dni != null) alumno.setDocumentoIdentidad(dni);
        alumno.setTipoDocumento(comboTipoDocumento.getValue());

        alumno.setAutorizaUsoDatos(chkAutoDatos.isSelected());
        alumno.setAutorizaImagen(chkAutoImagen.isSelected());
        alumno.setAutorizaActividades(chkAutoActividades.isSelected());
        alumno.setAutorizaComunicaciones(chkAutoComunicaciones.isSelected());
        alumno.setAutorizaIrseSolo(chkAutoIrseSolo.isSelected());
        alumno.setSeguimientoServiciosSociales(chkSeguimientoSS.isSelected());
        alumno.setSeguimientoSaf(chkSeguimientoSAF.isSelected());
        alumno.setDerivacionSS(chkDerivacionSS.isSelected());
        alumno.setDerivacionSaf(chkDerivacionSAF.isSelected());
        alumno.setDerivacionEoep(chkDerivacionEOEP.isSelected());
        alumno.setDerivacionColegio(chkDerivacionColegio.isSelected());
        alumno.setDerivacionOtro(chkDerivacionOtro.isSelected());

        String nacionalidad = limpiarTexto(txtNacionalidad.getText());
        if (nacionalidad != null) alumno.setNacionalidad(nacionalidad);
        String genero = comboGenero.getValue();
        if (genero != null) alumno.setGenero(genero);

        String ciudad = limpiarTexto(txtCiudad.getText());
        if (ciudad != null) alumno.setCiudad(ciudad);

        String cp = limpiarTexto(txtCodigoPostal.getText());
        if (cp != null) alumno.setCodigoPostal(cp);

        try {
            alumnoService.matricularNuevoAlumno(alumno, cursoNormalizado, grupo);

            mostrarMensaje("Éxito", "Alumno guardado correctamente.", Alert.AlertType.INFORMATION);
            cerrarVentana( event);

        } catch (Exception e) {
            // Si el servicio o el DAO fallan, capturamos el error aquí
            mostrarMensaje("Error", "No se pudo guardar el alumno: " + (e.getMessage() != null ? e.getMessage() : "Error inesperado."), Alert.AlertType.ERROR);
        }
    }

    private String limpiarTexto(String valor) {
        if (valor == null) {
            return null;
        }

        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    @FXML
    private void cancelar(ActionEvent event) {
        cerrarVentana(event);
    }

    @FXML
    private void onSeleccionarFotoClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona foto del alumno");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado != null) {
            fotoArchivoSeleccionado = archivoSeleccionado;
            rutaFotoSeleccionada = archivoSeleccionado.getAbsolutePath();
            fotoAlumno.setImage(new Image(archivoSeleccionado.toURI().toString()));
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        int punto = nombreArchivo.lastIndexOf('.');
        return punto >= 0 ? nombreArchivo.substring(punto) : "";
    }

    private void cerrarVentana(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    private void mostrarMensaje(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
