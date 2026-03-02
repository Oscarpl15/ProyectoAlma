package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.Launcher;
import com.practicasalma.proyectoalma.model.Docente;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class DocentesController {

    @FXML private TableView<Docente> tablaDocentes;
    @FXML private TableColumn<Docente, String> colNombre;
    @FXML private TableColumn<Docente, String> colApellidos;
    @FXML private TableColumn<Docente, String> colDireccion;
    @FXML private TableColumn<Docente, String> colTelefono;
    @FXML private TableColumn<Docente, String> colDni;
    @FXML private TableColumn<Docente, String> colCorreo;

    // Datos falsos para la presentación
    private ObservableList<Docente> listaFalsa = FXCollections.observableArrayList(
            new Docente("Sofía", "Díaz Sánchez", "C/ Olivos 13 1B", "612 34 56 78", "09876543E", "sofiadiazsanchez@gmail.com"),
            new Docente("Daniel", "Gonzalez Fernández", "C/ Flores 5 4D", "698 76 54 32", "01234567G", "danielgonzalezfernandez@gmail.com"),
            new Docente("Alma", "Perez Domínguez", "C/ Olivos 13 1B", "612 34 56 78", "08642975A", "almaperezdominguez@gmail.com")
    );

    @FXML
    public void initialize() {
        // 1. Enlazar columnas con el modelo
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellidos.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellidos()));
        colDireccion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDireccion()));
        colTelefono.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTelefono()));
        colDni.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDni()));
        colCorreo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCorreo()));

        // 2. Meter datos
        tablaDocentes.setItems(listaFalsa);

        // 3. EVENTO DOBLE CLIC (Importante)
        tablaDocentes.setRowFactory(tv -> {
            TableRow<Docente> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Docente docenteSeleccionado = row.getItem();
                    abrirFichaDocente(docenteSeleccionado);
                }
            });
            return row ;
        });
    }

    @FXML
    private void nuevoDocente() {
        try {
            // 1. Cargar la vista del Pop-up
            FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("view/agregarDocente-view.fxml"));
            Parent root = loader.load();

            // 2. Crear la escena
            Scene scene = new Scene(root);

            // 3. Crear un NUEVO escenario (Stage) para el pop-up
            Stage stage = new Stage();

            // Título de la ventanita
            stage.setTitle("Agregar docente");

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

    private void abrirFichaDocente(Docente docente) {
        // Aquí abrirías la ventana con los datos cargados
        mostrarMensaje("Ficha de Docente", "Has hecho doble clic en: " + docente.getNombre());
        // PARA LA PRESENTACIÓN:
        // 1. Crear un nuevo Stage (Ventana)
        // 2. Cargar el FXML de detalle
        // 3. Pasar el objeto 'alumno' al controlador de esa nueva ventana
    }

    private void mostrarMensaje(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}