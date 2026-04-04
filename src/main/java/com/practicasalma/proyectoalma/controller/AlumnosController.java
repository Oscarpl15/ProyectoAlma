package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.Launcher;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.service.AlumnoService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AlumnosController {

    @FXML private TableView<Alumno> tablaAlumnos;
    @FXML private TableColumn<Alumno, String> colNombre;
    @FXML private TableColumn<Alumno, String> colApellidos;
    @FXML private TableColumn<Alumno, String> colTelefono;
    @FXML private TableColumn<Alumno, String> colGrupo;

    @FXML private TableColumn<Alumno, Boolean> colAutoDatos;
    @FXML private TableColumn<Alumno, Boolean> colAutoActividades;
    @FXML private TableColumn<Alumno, Boolean> colAutoComunicaciones;
    @FXML private TableColumn<Alumno, Boolean> colAutoImagen;
    @FXML private TableColumn<Alumno, Boolean> colAutoIrseSolo;

    @FXML private ComboBox<String> comboCursoFiltro;
    @FXML private ComboBox<String> comboGrupoFiltro;
    @FXML private TextField txtBuscar;

    private final AlumnoService alumnoService = new AlumnoService();
    private final ObservableList<Alumno> listaMaestra = FXCollections.observableArrayList();
    private FilteredList<Alumno> listaFiltrada;

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellidos.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellidos()));
        colTelefono.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTelefono() != null ? cellData.getValue().getTelefono() : ""));
        colGrupo.setCellValueFactory(cellData -> {
            Alumno alumno = cellData.getValue();
            // Si el alumno tiene matrículas, cogemos el curso de la primera (la actual)
            if (alumno.getMatriculas() != null && !alumno.getMatriculas().isEmpty()) {
                String curso = alumno.getMatriculas().get(0).getGrupoAsignado();
                return new SimpleStringProperty(curso != null ? curso : "Sin curso");
            }
            return new SimpleStringProperty("Sin matricular");
        });

        configurarColumnaBooleana(colAutoDatos, alumno -> alumno.getAutorizaUsoDatos());
        configurarColumnaBooleana(colAutoActividades, alumno -> alumno.getAutorizaActividades());
        configurarColumnaBooleana(colAutoComunicaciones, alumno -> alumno.getAutorizaComunicaciones());
        configurarColumnaBooleana(colAutoImagen, alumno -> alumno.getAutorizaImagen());
        configurarColumnaBooleana(colAutoIrseSolo, alumno -> alumno.getAutorizaIrseSolo());

        comboCursoFiltro.getSelectionModel().select(0);

        listaFiltrada = new FilteredList<>(listaMaestra, a -> true);
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            listaFiltrada.setPredicate(alumno -> {
                if (newVal == null || newVal.isBlank()) return true;
                String filtro = newVal.toLowerCase();
                return alumno.getNombre().toLowerCase().contains(filtro)
                        || alumno.getApellidos().toLowerCase().contains(filtro);
            });
        });
        SortedList<Alumno> listaSortable = new SortedList<>(listaFiltrada);
        listaSortable.comparatorProperty().bind(tablaAlumnos.comparatorProperty());
        tablaAlumnos.setItems(listaSortable);

        tablaAlumnos.setRowFactory(tv -> {
            TableRow<Alumno> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Alumno alumnoSeleccionado = row.getItem();
                    abrirFichaAlumno(alumnoSeleccionado);
                }
            });
            return row;
        });

        cargarAlumnosEnTabla();
    }

    private void configurarColumnaBooleana(TableColumn<Alumno, Boolean> columna, java.util.function.Function<Alumno, Boolean> extractor) {
        columna.setCellValueFactory(cellData -> {
            Boolean valor = extractor.apply(cellData.getValue());
            return new SimpleObjectProperty<>(valor != null ? valor : false);
        });

        columna.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                // Limpiamos las clases previas para evitar conflictos al reciclar celdas
                getStyleClass().removeAll("celda-booleana-true", "celda-booleana-false");

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "✓" : "✗");
                    // Aplicamos la clase CSS correspondiente
                    getStyleClass().add(item ? "celda-booleana-true" : "celda-booleana-false");
                }
            }
        });
    }

    public void cargarAlumnosEnTabla() {
        listaMaestra.setAll(alumnoService.obtenerTodos());
    }

    @FXML
    private void nuevoAlumno() {
        try {
            FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("view/agregarAlumno-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Agregar alumno");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            String rutaIcono = "/com/practicasalma/proyectoalma/assets/logo.png";
            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream(rutaIcono)));
            } catch (Exception ignored) {}

            stage.showAndWait();

            // Refresco automático al cerrar la ventana de añadir
            cargarAlumnosEnTabla();

        } catch (IOException e) {
            System.err.println("Error al abrir el pop-up: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirFichaAlumno(Alumno alumnoTabla) {
        try {
            // 1. Pedimos a la BBDD el alumno con TODA su información "despierta"
            Alumno alumnoCompleto = alumnoService.obtenerCompleto(alumnoTabla.getId());

            if (alumnoCompleto == null) {
                System.err.println("No se ha podido cargar la información completa del alumno.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/practicasalma/proyectoalma/view/ficha-alumno.fxml"));
            Parent root = loader.load();

            FichaAlumnoController controller = loader.getController();

            // 2. Le pasamos a la ficha el alumno COMPLETO, no el de la tabla
            controller.setAlumno(alumnoCompleto);

            Stage stage = new Stage();
            stage.setTitle("Ficha del Alumno: " + alumnoCompleto.getNombre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tablaAlumnos.getScene().getWindow());
            stage.showAndWait();

            // Refrescamos la tabla al cerrar por si hubo cambios
            cargarAlumnosEnTabla();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al abrir la ficha: " + e.getMessage());
        }
    }
}