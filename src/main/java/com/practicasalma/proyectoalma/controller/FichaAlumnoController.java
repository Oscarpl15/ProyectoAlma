package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Alumno;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FichaAlumnoController {

    @FXML private ImageView imgFoto;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtColegio;
    @FXML private TextField txtDerivado;

    @FXML private CheckBox chkAutoDatos;
    @FXML private CheckBox chkAutoImagen;
    @FXML private CheckBox chkAutoActividades;
    @FXML private CheckBox chkAutoComunicaciones;
    @FXML private CheckBox chkAutoIrseSolo;

    @FXML private Button btnGuardar;

    private Alumno alumnoActual;
    private String rutaFotoSeleccionada = null;

    @FXML
    public void initialize() {
        // Inicialización básica
    }

    public void setAlumno(Alumno alumno) {
        this.alumnoActual = alumno;
        if (alumno != null) {
            txtNombre.setText(alumno.getNombre() != null ? alumno.getNombre() : "");
            txtApellidos.setText(alumno.getApellidos() != null ? alumno.getApellidos() : "");
            txtDni.setText(alumno.getDocumentoIdentidad() != null ? alumno.getDocumentoIdentidad() : "");
            txtDireccion.setText(alumno.getDireccion() != null ? alumno.getDireccion() : "");
            txtTelefono.setText(alumno.getTelefono() != null ? alumno.getTelefono() : "");
            txtColegio.setText(alumno.getColegio() != null ? alumno.getColegio() : "");
            txtDerivado.setText(alumno.getDerivadoPor() != null ? alumno.getDerivadoPor() : "");
            dpFechaNacimiento.setValue(alumno.getFechaNacimiento());

            chkAutoDatos.setSelected(Boolean.TRUE.equals(alumno.getAutorizaUsoDatos()));
            chkAutoImagen.setSelected(Boolean.TRUE.equals(alumno.getAutorizaImagen()));
            chkAutoActividades.setSelected(Boolean.TRUE.equals(alumno.getAutorizaActividades()));
            chkAutoComunicaciones.setSelected(Boolean.TRUE.equals(alumno.getAutorizaComunicaciones()));
            chkAutoIrseSolo.setSelected(Boolean.TRUE.equals(alumno.getAutorizaIrseSolo()));

            String rutaAlumno = alumno.getRutaFotoPerfil();
            if (rutaAlumno != null && !rutaAlumno.trim().isEmpty()) {
                try {
                    if (rutaAlumno.startsWith("/")) {
                        imgFoto.setImage(new Image(getClass().getResource(rutaAlumno).toExternalForm()));
                    } else if (rutaAlumno.startsWith("file:") || rutaAlumno.startsWith("http")) {
                        imgFoto.setImage(new Image(rutaAlumno));
                    } else {
                        imgFoto.setImage(new Image(new File(rutaAlumno).toURI().toString()));
                    }
                } catch (Exception e) {
                    System.out.println("Error al cargar la imagen: " + rutaAlumno);
                }
            } else {
                try {
                    imgFoto.setImage(new Image(getClass().getResource("/com/practicasalma/proyectoalma/assets/default.png").toExternalForm()));
                } catch (Exception e) {
                    System.out.println("No se encontró imagen por defecto.");
                }
            }
        }
    }

    @FXML
    private void seleccionarFoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona foto del alumno");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado != null) {
            rutaFotoSeleccionada = archivoSeleccionado.getAbsolutePath();
            imgFoto.setImage(new Image(archivoSeleccionado.toURI().toString()));
        }
    }

    @FXML
    private void guardarDatos() {
        System.out.println("Guardando datos del formulario...");
        cerrarVentana();
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }
}
