package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.Launcher;
import com.practicasalma.proyectoalma.model.Voluntario;
import com.practicasalma.proyectoalma.service.VoluntarioService;
import com.practicasalma.proyectoalma.util.FxUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class VoluntariosController {

    @FXML private TableView<Voluntario> tablaVoluntarios;
    @FXML private TableColumn<Voluntario, String> colNombre;
    @FXML private TableColumn<Voluntario, String> colApellidos;
    @FXML private TableColumn<Voluntario, String> colTelefono;
    @FXML private TableColumn<Voluntario, String> colDni;
    @FXML private TableColumn<Voluntario, String> colCorreo;

    // Documentación y Estado
    @FXML private TableColumn<Voluntario, Boolean> colDelitos;
    @FXML private TableColumn<Voluntario, Boolean> colProtDatos;
    @FXML private TableColumn<Voluntario, String> colAB;
    @FXML private TextField txtBuscar;

    private final VoluntarioService voluntarioService = new VoluntarioService();
    private final ObservableList<Voluntario> listaMaestra = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellidos.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellidos()));
        colTelefono.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTelefono()));
        colDni.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumentoIdentidad()));
        colCorreo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCorreo()));

        // Booleanos comentados temporalmente hasta asegurar que tienes los getters en el modelo Voluntario
        colDelitos.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getAutoDelitosSexuales() != null ? cellData.getValue().getAutoDelitosSexuales() : false));
        colProtDatos.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getAutoProteccionDatos() != null ? cellData.getValue().getAutoProteccionDatos() : false));

        colAB.setCellValueFactory(cellData ->
                new SimpleStringProperty(Boolean.TRUE.equals(cellData.getValue().getActivo()) ? "Activo" : "Baja"));
        colAB.setCellFactory(FxUtils.celdaEstado());

        FilteredList<Voluntario> listaFiltrada = new FilteredList<>(listaMaestra, v -> true);
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            listaFiltrada.setPredicate(voluntario -> {
                if (newVal == null || newVal.isBlank()) return true;
                String filtro = newVal.toLowerCase();
                return voluntario.getNombre().toLowerCase().contains(filtro)
                        || voluntario.getApellidos().toLowerCase().contains(filtro);
            });
        });
        SortedList<Voluntario> listaSortable = new SortedList<>(listaFiltrada);
        listaSortable.comparatorProperty().bind(tablaVoluntarios.comparatorProperty());
        tablaVoluntarios.setItems(listaSortable);

        cargarVoluntariosEnTabla();

        tablaVoluntarios.setRowFactory(tv -> {
            TableRow<Voluntario> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Voluntario voluntarioSeleccionado = row.getItem();
                    // abrirFichaVoluntario(voluntarioSeleccionado); // Lo haremos luego
                    System.out.println("Doble clic en voluntario: " + voluntarioSeleccionado.getNombre());
                }
            });
            return row ;
        });
    }

    public void cargarVoluntariosEnTabla() {
        listaMaestra.setAll(voluntarioService.obtenerTodos());
    }

    @FXML
    private void nuevoVoluntario() {
        try {
            FxUtils.abrirModal("agregarVoluntario-view.fxml", "Agregar voluntario");
            cargarVoluntariosEnTabla();
        } catch (IOException e) {
            System.err.println("Error al abrir el pop-up: " + e.getMessage());
        }
    }
}
