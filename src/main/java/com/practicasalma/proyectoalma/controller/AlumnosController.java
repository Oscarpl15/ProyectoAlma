package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.Launcher;

import com.practicasalma.proyectoalma.model.Alumno;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AlumnosController {

    @FXML private TableView<Alumno> tablaAlumnos;
    @FXML private TableColumn<Alumno, String> colNombre;
    @FXML private TableColumn<Alumno, String> colApellidos;

    @FXML
    private ComboBox<String> comboCursoFiltro;

    // Datos falsos para la presentación
    private ObservableList<Alumno> listaFalsa = FXCollections.observableArrayList(
            new Alumno("Juan", "Pérez López"),
            new Alumno("Lucía", "García Martín"),
            new Alumno("Marcos", "Alonso")
    );

    @FXML
    public void initialize() {
        // 1. Enlazar columnas con el modelo
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colApellidos.setCellValueFactory(cellData -> cellData.getValue().apellidosProperty());

        // 2. Meter datos
        tablaAlumnos.setItems(listaFalsa);

        comboCursoFiltro.getSelectionModel().select(0);

        // 3. EVENTO DOBLE CLIC (Importante)
        tablaAlumnos.setRowFactory(tv -> {
            TableRow<Alumno> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Alumno alumnoSeleccionado = row.getItem();
                    abrirFichaAlumno(alumnoSeleccionado);
                }
            });
            return row ;
        });
    }

    @FXML
    private void nuevoAlumno() {
        try {
            // 1. Cargar la vista del Pop-up
            FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("view/agregarAlumno-view.fxml"));
            Parent root = loader.load();

            // 2. Crear la escena
            Scene scene = new Scene(root);

            // 3. Crear un NUEVO escenario (Stage) para el pop-up
            Stage stage = new Stage();

            // Título de la ventanita
            stage.setTitle("Agregar alumno");

            // Asignar la escena
            stage.setScene(scene);

            // 4. Configurar la modalidad (IMPORTANTE para efecto pop-up)
            // APPLICATION_MODAL: Bloquea la interacción con la ventana principal
            // hasta que cierres este pop-up.
            stage.initModality(Modality.APPLICATION_MODAL);

            // Agregamos logo de la fundación a la ventana
            String rutaIcono = "/com/practicasalma/proyectoalma/assets/logo.png";
            stage.getIcons().add(new Image(getClass().getResourceAsStream(rutaIcono)));

            // 5. Mostrar la ventana
            // showAndWait() espera a que se cierre para continuar la ejecución del código
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error al abrir el pop-up: " + e.getMessage());
            e.printStackTrace();
        };
    }

    private void abrirFichaAlumno(Alumno alumno) {
        try {
            // 1. Cargar el FXML de la ficha
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/practicasalma/proyectoalma/view/ficha-alumno.fxml"));
            Parent root = loader.load();

            // 2. OBTENER EL CONTROLADOR DE LA FICHA (¡Importante!)
            FichaAlumnoController controller = loader.getController();

            // 3. PASARLE EL ALUMNO SELECCIONADO
            controller.setAlumno(alumno);

            // 4. Mostrar la ventana nueva
            Stage stage = new Stage();
            stage.setTitle("Ficha del Alumno: " + alumno.getNombre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL); // Bloquea la ventana de atrás hasta que cierres esta
            stage.initOwner(tablaAlumnos.getScene().getWindow()); // Dice que esta ventana pertenece a la principal
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al abrir la ficha: " + e.getMessage());
        }
    }

    private void mostrarMensaje(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}