package com.practicasalma.proyectoalma.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
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

    @FXML
    private void initialize() {
        try {
            String rutaDefault = getClass().getResource(RUTA_DEFECTO_IMAGEN).toExternalForm();
            Image imagenDefault = new Image(rutaDefault);
            fotoAlumno.setImage(imagenDefault);
        } catch (NullPointerException e) {
            System.out.println("ERROR: No se encontró la imagen por defecto.");
        }
    }

    @FXML
    private void guardarAlumno(ActionEvent event) {
        // Recuperar los datos principales
        String nombre = txtNombre.getText();
        String apellidos = txtApellidos.getText();
        String curso = comboCurso.getValue();
        LocalDate fechaNac = dpFechaNacimiento.getValue();

        // Validacion simple
        if (nombre == null || nombre.trim().isEmpty() || curso == null) {
            System.out.println("Por favor rellena al menos nombre y curso");
            return;
        }

        System.out.println("--- GUARDANDO NUEVO ALUMNO ---");
        System.out.println("Nombre: " + nombre + " " + apellidos);
        System.out.println("DNI: " + txtDni.getText());
        System.out.println("Curso: " + curso + " | Colegio: " + txtColegio.getText());

        // Comprobar Autorizaciones
        System.out.println("Auto. Irse Solo: " + chkAutoIrseSolo.isSelected());
        System.out.println("Seguimiento SS: " + chkSS.isSelected() + " | Derivado por: " + txtDerivado.getText());

        // Aquí más adelante llamaremos a BBDD para hacer el INSERT real.

        cerrarVentana(event);
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

            Image image = new Image(archivoSeleccionado.toURI().toString());
            fotoAlumno.setImage(image);

            System.out.println("Ruta de la foto: " + rutaFotoSeleccionada);
        }
    }

    private void cerrarVentana(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
