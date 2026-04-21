package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.service.SocioService;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import com.practicasalma.proyectoalma.util.filtro.SocioFiltro;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador JavaFX de la pestaña de socios ({@code socios-view.fxml}).
 * <p>
 * Gestiona la tabla de socios con filtrado, acceso a la ficha de detalle
 * y las operaciones de alta/baja.
 * </p>
 */
public class SocioController {

    @FXML private TableView<Socio> tablaSocios;
    @FXML private TableColumn<Socio, String> colNombre;
    @FXML private TableColumn<Socio, String> colApellidos;
    @FXML private TableColumn<Socio, String> colDni;
    @FXML private TableColumn<Socio, String> colTipoEntidad;
    @FXML private TableColumn<Socio, String> colPeriodicidad;
    @FXML private TableColumn<Socio, Double> colCuota;
    @FXML private TableColumn<Socio, String> colEstado;

    @FXML private ComboBox<String> comboTipoEntidadFiltro;
    @FXML private ComboBox<String> comboPeriodicidadFiltro;
    @FXML private ComboBox<String> comboEstadoFiltro;
    @FXML private TextField txtBuscar;

    private final SocioService socioService = new SocioService();
    private final ObservableList<Socio> listaMaestra = FXCollections.observableArrayList();
    private FilteredList<Socio> listaFiltrada;

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellidos.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellidos()));
        colDni.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumentoIdentidad()));
        colTipoEntidad.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipoEntidad()));
        colPeriodicidad.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPeriodicidad()));
        colCuota.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getCuota()).asObject());

        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(Boolean.TRUE.equals(cellData.getValue().getActivo()) ? "Activo" : "Inactivo"));
        colEstado.setCellFactory(FxUtils.celdaEstado());

        poblarTipoEntidad();
        poblarPeriodicidad();
        poblarEstado();

        listaFiltrada = new FilteredList<>(listaMaestra, s -> true);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        comboTipoEntidadFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        comboPeriodicidadFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        comboEstadoFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        SortedList<Socio> listaSortable = new SortedList<>(listaFiltrada);
        listaSortable.comparatorProperty().bind(tablaSocios.comparatorProperty());
        tablaSocios.setItems(listaSortable);

        tablaSocios.setRowFactory(tv -> {
            TableRow<Socio> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirFichaSocio(row.getItem());
                }
            });
            return row;
        });

        cargarSociosEnTabla();
    }

    private void poblarTipoEntidad() {
        comboTipoEntidadFiltro.setItems(FXCollections.observableArrayList(
                "Todos", "Física", "Empresa", "Asociación"
        ));
        comboTipoEntidadFiltro.setValue("Todos");
    }

    private void poblarPeriodicidad() {
        comboPeriodicidadFiltro.setItems(FXCollections.observableArrayList(
                "Todos", "Mensual", "Trimestral", "Anual", "Puntual"
        ));
        comboPeriodicidadFiltro.setValue("Todos");
    }

    private void poblarEstado() {
        comboEstadoFiltro.setItems(FXCollections.observableArrayList("Todos", "Activo", "Inactivo"));
        comboEstadoFiltro.setValue("Todos");
    }

    private void aplicarFiltros() {
        listaFiltrada.setPredicate(SocioFiltro.construir(
                txtBuscar.getText(),
                comboTipoEntidadFiltro.getValue(),
                comboPeriodicidadFiltro.getValue(),
                comboEstadoFiltro.getValue()
        ));
    }

    public void nuevoSocio(ActionEvent actionEvent) {
        try {
            FxUtils.abrirModal("agregarSocio-view.fxml", "Agregar nuevo socio");
            cargarSociosEnTabla();
        } catch (IOException e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    public void cargarSociosEnTabla() {
        listaMaestra.setAll(socioService.obtenerTodos());
    }

    private void abrirFichaSocio(Socio socioTabla) {
        try {
            Socio socio = socioService.obtenerCompleto(socioTabla.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/practicasalma/proyectoalma/view/ficha-socio.fxml"));
            Parent root = loader.load();

            FichaSocioController controller = loader.getController();
            controller.setSocio(socio);

            Stage stage = new Stage();
            stage.setTitle("Ficha del Socio: " + socio.getNombre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tablaSocios.getScene().getWindow());
            stage.showAndWait();

            cargarSociosEnTabla();
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ficha: " + e.getMessage());
        }
    }
}