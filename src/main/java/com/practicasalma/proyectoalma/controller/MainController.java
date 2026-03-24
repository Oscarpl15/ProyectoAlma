package com.practicasalma.proyectoalma.controller;


import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private TabPane tabPrincipal; // Se conecta con el fx:id del FXML

    @FXML private StackPane rootPane;
    @FXML private ImageView imgFondo;

    @FXML
    public void initialize() {
        // Hace que la imagen persiga dinámicamente el ancho y alto de la ventana
        imgFondo.fitWidthProperty().bind(rootPane.widthProperty());
        imgFondo.fitHeightProperty().bind(rootPane.heightProperty());
        // Opcional: Asegurarnos de que arranca en la pestaña 0
        if (tabPrincipal != null) {
            tabPrincipal.getSelectionModel().select(0);
        }
    }

    // --- MÉTODOS DE LOS BOTONES DEL MENÚ ---

    @FXML
    protected void mostrarAlumnos() {
        // Selecciona la primera pestaña (Índice 0)
        tabPrincipal.getSelectionModel().select(0);
        System.out.println("Navegando a Alumnos...");
    }

    @FXML
    protected void mostrarDocentes() {
        // Selecciona la segunda pestaña (Índice 1)
        tabPrincipal.getSelectionModel().select(1);
        System.out.println("Navegando a Docentes...");
    }

    @FXML
    protected void mostrarVoluntarios() {
        // Selecciona la tercera pestaña (Índice 2)
        tabPrincipal.getSelectionModel().select(2);
        System.out.println("Navegando a Voluntarios...");
    }

    @FXML
    protected void mostrarSocios() {
        // Selecciona la tercera pestaña (Índice 2)
        tabPrincipal.getSelectionModel().select(3);
        System.out.println("Navegando a Socios...");
    }


}
