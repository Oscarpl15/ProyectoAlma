package com.practicasalma.proyectoalma.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AgregarDocenteController {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellidos;
    @FXML
    private TextField txtDireccion; // Nuevo campo
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtDni;
    @FXML
    private TextField txtCorreo;

    // Checkboxes
    @FXML private CheckBox checkAuth1;

    @FXML
    private void guardarDocente(ActionEvent event) {
        // Recuperar los datos
        String nombre = txtNombre.getText();
        String apellidos = txtApellidos.getText();
        String direccion = txtDireccion.getText();
        String telefono = txtTelefono.getText();
        String dni = txtDni.getText();
        String correo = txtCorreo.getText();


        // Ejemplo de cómo ver si un check está marcado
        boolean tieneAuth1 = checkAuth1.isSelected();

        // Validacion simple
        if (nombre.isEmpty()) {
            System.out.println("Por favor rellena al menos nombre y curso");
            return;
        }

        System.out.println("Guardando: " + nombre + " " + apellidos);
        System.out.println("Dirección: " + direccion);
        System.out.println("Telefono: " + telefono);
        System.out.println("DNI: " + dni);
        System.out.println("Correo:" + correo);

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
