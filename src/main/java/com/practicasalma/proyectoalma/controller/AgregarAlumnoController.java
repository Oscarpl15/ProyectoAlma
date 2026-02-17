package com.practicasalma.proyectoalma.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AgregarAlumnoController {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellidos;
    @FXML
    private TextField txtDireccion; // Nuevo campo

    @FXML
    private ComboBox<String> comboCurso; // Nuevo campo

    // Checkboxes
    @FXML private CheckBox checkAuth1;
    @FXML private CheckBox checkAuth2;
    @FXML private CheckBox checkAuth3;
    @FXML private CheckBox checkAuth4;

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

    private void cerrarVentana(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
