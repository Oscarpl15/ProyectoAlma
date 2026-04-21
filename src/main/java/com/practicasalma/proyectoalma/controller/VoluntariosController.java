package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Voluntario;
import com.practicasalma.proyectoalma.service.VoluntarioService;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import com.practicasalma.proyectoalma.util.filtro.VoluntarioFiltro;
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

public class VoluntariosController {

    @FXML private TableView<Voluntario> tablaVoluntarios;
    @FXML private TableColumn<Voluntario, String> colNombre;
    @FXML private TableColumn<Voluntario, String> colApellidos;
    @FXML private TableColumn<Voluntario, String> colTelefono;
    @FXML private TableColumn<Voluntario, String> colDni;
    @FXML private TableColumn<Voluntario, String> colCorreo;
    @FXML private TableColumn<Voluntario, Boolean> colDelitos;
    @FXML private TableColumn<Voluntario, Boolean> colProtDatos;
    @FXML private TableColumn<Voluntario, String> colAB;

    @FXML private ComboBox<String> comboCursoFiltro;
    @FXML private ComboBox<String> comboEstadoFiltro;
    @FXML private TextField txtBuscar;

    private final VoluntarioService voluntarioService = new VoluntarioService();
    private final ObservableList<Voluntario> listaMaestra = FXCollections.observableArrayList();
    private FilteredList<Voluntario> listaFiltrada;

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

        listaFiltrada = new FilteredList<>(listaMaestra, v -> true);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        comboCursoFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        comboEstadoFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        SortedList<Voluntario> listaSortable = new SortedList<>(listaFiltrada);
        listaSortable.comparatorProperty().bind(tablaVoluntarios.comparatorProperty());
        tablaVoluntarios.setItems(listaSortable);

        tablaVoluntarios.setRowFactory(tv -> {
            TableRow<Voluntario> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirFichaVoluntario(row.getItem());
                }
            });
            return row;
        });

        cargarVoluntariosEnTabla();
    }

    private void poblarCursoEscolar() {
        LocalDate hoy = LocalDate.now();
        int anyoInicio = 2022;
        int anyoActual = (hoy.getMonthValue() >= 9) ? hoy.getYear() : hoy.getYear() - 1;

        ObservableList<String> cursos = FXCollections.observableArrayList();
        cursos.add("Todos");
        for (int anyo = anyoInicio; anyo <= anyoActual; anyo++) {
            cursos.add(anyo + "/" + (anyo + 1));
        }
        comboCursoFiltro.setItems(cursos);
        comboCursoFiltro.setValue(anyoActual + "/" + (anyoActual + 1));
    }

    private void poblarEstado() {
        comboEstadoFiltro.setItems(FXCollections.observableArrayList("Todos", "Activo", "Inactivo"));
        comboEstadoFiltro.setValue("Todos");
    }

    private void aplicarFiltros() {
        listaFiltrada.setPredicate(VoluntarioFiltro.construir(
                txtBuscar.getText(),
                comboCursoFiltro.getValue(),
                comboEstadoFiltro.getValue()
        ));
    }

    public void cargarVoluntariosEnTabla() {
        listaMaestra.setAll(voluntarioService.obtenerTodos());
    }

    private void abrirFichaVoluntario(Voluntario voluntarioTabla) {
        try {
            Voluntario voluntario = voluntarioService.obtenerCompleto(voluntarioTabla.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/practicasalma/proyectoalma/view/ficha-voluntario.fxml"));
            javafx.scene.Parent root = loader.load();

            FichaVoluntarioController controller = loader.getController();
            controller.setVoluntario(voluntario);

            Stage stage = new Stage();
            stage.setTitle("Ficha del Voluntario: " + voluntario.getNombre());
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tablaVoluntarios.getScene().getWindow());
            stage.showAndWait();

            cargarVoluntariosEnTabla();
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ficha: " + e.getMessage());
        }
    }

    @FXML
    private void nuevoVoluntario() {
        try {
            FxUtils.abrirModal("agregarVoluntario-view.fxml", "Agregar voluntario");
            cargarVoluntariosEnTabla();
        } catch (IOException e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }
}