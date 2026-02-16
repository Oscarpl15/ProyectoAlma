package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Alumno;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

public class AlumnosController {

    @FXML private TableView<Alumno> tablaAlumnos;
    @FXML private TableColumn<Alumno, String> colNombre;
    @FXML private TableColumn<Alumno, String> colApellidos;

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
        // Aquí abrirías la ventana vacía
        mostrarMensaje("Abrir Formulario", "Aquí se abrirá la ventana para crear un alumno nuevo.");
    }

    private void abrirFichaAlumno(Alumno alumno) {
        // Aquí abrirías la ventana con los datos cargados
        mostrarMensaje("Ficha de Alumno", "Has hecho doble clic en: " + alumno.nombreProperty().get());
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