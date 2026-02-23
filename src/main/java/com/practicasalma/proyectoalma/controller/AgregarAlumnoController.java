package com.practicasalma.proyectoalma.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class AgregarAlumnoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDireccion;

    @FXML private ComboBox<String> comboCurso;

    // Checkboxes
    @FXML private CheckBox checkAuth1;
    @FXML private CheckBox checkAuth2;
    @FXML private CheckBox checkAuth3;
    @FXML private CheckBox checkAuth4;

    @FXML private ImageView fotoAlumno;
    private final String RUTA_DEFECTO_IMAGEN = "/com/practicasalma/proyectoalma/assets/default.png"; // Foto para que sea la por defecto al cargar una imagen
    private String rutaFotoSeleccionada = null; // Foto seleccionada por el usuario

    @FXML
    private void initialize() {
        try {

            String rutaDefault = getClass().getResource(RUTA_DEFECTO_IMAGEN).toExternalForm(); // .toExternalForm transforma automaticamente al formato que necesita iamge
            Image imagenDefault = new Image(rutaDefault);

            fotoAlumno.setImage(imagenDefault);

        } catch (NullPointerException e) {
            System.out.println("ERROR: No se encontró la imagen por defecto.");
        }
    }

    @FXML
    private void guardarAlumno(ActionEvent event) {
        // Recuperar los datos
        String nombre = txtNombre.getText();
        String apellidos = txtApellidos.getText();
        String direccion = txtDireccion.getText();
        String curso = comboCurso.getValue();

        // Ejemplo de cómo ver si un check está marcado
        boolean tieneAuth1 = checkAuth1.isSelected();

        // Validacion simple
        if (nombre.isEmpty() || curso == null) {
            System.out.println("Por favor rellena al menos nombre y curso");
            return;
        }

        System.out.println("Guardando: " + nombre + " " + apellidos + " - Curso: " + curso);
        System.out.println("Dirección: " + direccion);

        // Cierra la ventana
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
