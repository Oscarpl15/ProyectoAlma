package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.service.DocenteService;
import com.practicasalma.proyectoalma.util.UtilFecha;
import com.practicasalma.proyectoalma.util.filtro.DocenteFiltro;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Controlador JavaFX de la pestaña de docentes ({@code docentes-view.fxml}).
 * <p>
 * Gestiona la tabla de docentes con filtrado, acceso a la ficha de detalle
 * y las operaciones de alta/baja.
 * </p>
 */
public class DocentesController {

    @FXML private TableView<Docente> tablaDocentes;
    @FXML private TableColumn<Docente, String> colNombre;
    @FXML private TableColumn<Docente, String> colApellidos;
    @FXML private TableColumn<Docente, String> colTelefono;
    @FXML private TableColumn<Docente, String> colDni;
    @FXML private TableColumn<Docente, String> colCorreo;
    @FXML private TableColumn<Docente, Boolean> colDelitos;
    @FXML private TableColumn<Docente, Boolean> colProtDatos;
    @FXML private TableColumn<Docente, String> colAB;

    @FXML private ComboBox<String> comboCursoFiltro;
    @FXML private ComboBox<String> comboEstadoFiltro;
    @FXML private TextField txtBuscar;

    private final DocenteService docenteService = new DocenteService();
    private final ObservableList<Docente> listaMaestra = FXCollections.observableArrayList();
    private FilteredList<Docente> listaFiltrada;

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellidos.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellidos()));
        colTelefono.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTelefono()));
        colDni.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumentoIdentidad()));
        colCorreo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCorreo()));

        colDelitos.setCellValueFactory(cellData -> new SimpleBooleanProperty(
                cellData.getValue().getAutoDelitosSexuales() != null ? cellData.getValue().getAutoDelitosSexuales() : false));
        colDelitos.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("celda-booleana-true", "celda-booleana-false");
                if (empty || item == null) { setText(null); }
                else { setText(item ? "✓" : "✗"); getStyleClass().add(item ? "celda-booleana-true" : "celda-booleana-false"); }
            }
        });

        colProtDatos.setCellValueFactory(cellData -> new SimpleBooleanProperty(
                cellData.getValue().getAutoProteccionDatos() != null ? cellData.getValue().getAutoProteccionDatos() : false));
        colProtDatos.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("celda-booleana-true", "celda-booleana-false");
                if (empty || item == null) { setText(null); }
                else { setText(item ? "✓" : "✗"); getStyleClass().add(item ? "celda-booleana-true" : "celda-booleana-false"); }
            }
        });

        colAB.setCellValueFactory(cellData ->
                new SimpleStringProperty(Boolean.TRUE.equals(cellData.getValue().getActivo()) ? "Activo" : "Inactivo"));
        colAB.setCellFactory(FxUtils.celdaEstado());

        poblarCursoEscolar();
        poblarEstado();

        listaFiltrada = new FilteredList<>(listaMaestra, d -> true);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        comboCursoFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        comboEstadoFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        SortedList<Docente> listaSortable = new SortedList<>(listaFiltrada);
        listaSortable.comparatorProperty().bind(tablaDocentes.comparatorProperty());
        tablaDocentes.setItems(listaSortable);

        tablaDocentes.setRowFactory(tv -> {
            TableRow<Docente> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirFichaDocente(row.getItem());
                }
            });
            return row;
        });

        cargarDocentesEnTabla();
    }

    private void poblarCursoEscolar() {
        int anyoInicio = 2022;
        String cursoActual = UtilFecha.calcularCursoAcademico();
        int anyoActual = Integer.parseInt(cursoActual.split("/")[0]);

        ObservableList<String> cursos = FXCollections.observableArrayList();
        cursos.add("Todos");
        for (int anyo = anyoInicio; anyo <= anyoActual; anyo++) {
            cursos.add(anyo + "/" + (anyo + 1));
        }
        comboCursoFiltro.setItems(cursos);
        comboCursoFiltro.setValue(cursoActual);
    }

    private void poblarEstado() {
        comboEstadoFiltro.setItems(FXCollections.observableArrayList("Todos", "Activo", "Inactivo"));
        comboEstadoFiltro.setValue("Todos");
    }

    private void aplicarFiltros() {
        listaFiltrada.setPredicate(DocenteFiltro.construir(
                txtBuscar.getText(),
                comboCursoFiltro.getValue(),
                comboEstadoFiltro.getValue()
        ));
    }

    @FXML
    private void nuevoDocente() {
        try {
            FxUtils.abrirModal("agregarDocente-view.fxml", "Agregar docente");
            cargarDocentesEnTabla();
        } catch (IOException e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    public void cargarDocentesEnTabla() {
        listaMaestra.setAll(docenteService.obtenerTodos());
    }

    private void abrirFichaDocente(Docente docenteTabla) {
        try {
            Docente docente = docenteService.obtenerCompleto(docenteTabla.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/practicasalma/proyectoalma/view/ficha-docente.fxml"));
            javafx.scene.Parent root = loader.load();

            FichaDocenteController controller = loader.getController();
            controller.setDocente(docente);

            Stage stage = new Stage();
            stage.setTitle("Ficha del Docente: " + docente.getNombre());
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tablaDocentes.getScene().getWindow());
            stage.showAndWait();

            cargarDocentesEnTabla();
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ficha: " + e.getMessage());
        }
    }
}
