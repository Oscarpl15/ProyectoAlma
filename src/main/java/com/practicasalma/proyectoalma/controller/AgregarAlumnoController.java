package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.service.AlumnoService;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import com.practicasalma.proyectoalma.util.Validador;
import jakarta.persistence.EntityManager;
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

public class AgregarAlumnoController {

    // Datos Personales
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;

    // Datos Escolares / Sociales
    @FXML private TextField txtColegio;
    @FXML private ComboBox<String> comboCurso;
    @FXML private TextField txtDerivado;
    @FXML private CheckBox chkSS;
    @FXML private CheckBox chkSAF;

    // Autorizaciones Legales
    @FXML private CheckBox chkAutoDatos;
    @FXML private CheckBox chkAutoImagen;
    @FXML private CheckBox chkAutoActividades;
    @FXML private CheckBox chkAutoComunicaciones;
    @FXML private CheckBox chkAutoIrseSolo;

    // Foto (Tu lógica original)
    @FXML private ImageView fotoAlumno;
    private final String RUTA_DEFECTO_IMAGEN = "/com/practicasalma/proyectoalma/assets/default.png";
    private String rutaFotoSeleccionada = null;

    // Instanciamos el servicio
    private final AlumnoService alumnoService = new AlumnoService();

    @FXML
    private void initialize() {
        try {
            String rutaDefault = getClass().getResource(RUTA_DEFECTO_IMAGEN).toExternalForm();
            fotoAlumno.setImage(new Image(rutaDefault));
        } catch (NullPointerException e) {
            System.out.println("ERROR: No se encontró la imagen por defecto.");
        }
    }

    @FXML
    private void guardarAlumno(ActionEvent event) {
        // Recoger datos UI
        String nombre = limpiarTexto(txtNombre.getText());
        String apellidos = limpiarTexto(txtApellidos.getText());
        String curso = comboCurso.getValue();
        LocalDate fechaNac = dpFechaNacimiento.getValue();
        String dni = limpiarTexto(txtDni.getText());

        // Validación Rápida de Interfaz
        if (nombre == null || apellidos == null || fechaNac == null || curso == null || curso.trim().isEmpty()) {
            mostrarMensaje("Datos incompletos", "Nombre, apellidos, fecha de nacimiento y curso son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        if (dni != null) {
            if (!Validador.esDni(dni) && !Validador.esNie(dni)) {
                mostrarMensaje("Formato Incorrecto", "El DNI/NIE introducido no es válido. Revisa las letras y números.", Alert.AlertType.WARNING);
                return; // Cortamos la ejecución, no seguimos guardando
            }
        }

        // Empaquetar los datos en el Modelo
        Alumno alumno = new Alumno(nombre, apellidos, limpiarTexto(txtDireccion.getText()), fechaNac);
        alumno.setTelefono(limpiarTexto(txtTelefono.getText()));
        alumno.setColegio(limpiarTexto(txtColegio.getText()));
        alumno.setDerivadoPor(limpiarTexto(txtDerivado.getText()));
        alumno.setRutaFotoPerfil(rutaFotoSeleccionada);

        if (dni != null) alumno.setDocumentoIdentidad(dni);

        alumno.setAutorizaUsoDatos(chkAutoDatos.isSelected());
        alumno.setAutorizaImagen(chkAutoImagen.isSelected());
        alumno.setAutorizaActividades(chkAutoActividades.isSelected());
        alumno.setAutorizaComunicaciones(chkAutoComunicaciones.isSelected());
        alumno.setAutorizaIrseSolo(chkAutoIrseSolo.isSelected());
        alumno.setSeguimientoServiciosSociales(chkSS.isSelected());
        alumno.setSeguimientoSaf(chkSAF.isSelected());

        // Delegar la responsabilidad al Servicio
        try {
            alumnoService.matricularNuevoAlumno(alumno, curso);

            // Si llegamos aquí, todo salió bien
            mostrarMensaje("Éxito", "Alumno guardado correctamente.", Alert.AlertType.INFORMATION);
            cerrarVentana(event);

        } catch (Exception e) {
            // Si el servicio o el DAO fallan, capturamos el error aquí
            System.err.println("Error en el proceso de guardado: " + e.getMessage());
            mostrarMensaje("Error", "No se pudo guardar el alumno: " + e.getMessage(), Alert.AlertType.ERROR);
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
        // Es para que te salga la ventana de seleccionar archivos
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona foto del alumno");

        // Indicas que extensiones son validos para poder selccionar y te filtre
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg"));

        // Sirve para que solo se pueda darle click a la ventana "padre" hasta que seleciones una foto o canceles
        // y aparte guarda el archivo que hayas seleccionado
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado != null) {
            rutaFotoSeleccionada = archivoSeleccionado.getAbsolutePath();
            fotoAlumno.setImage(new Image(archivoSeleccionado.toURI().toString()));

            System.out.println("Ruta de la foto: " + rutaFotoSeleccionada);
        }
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
