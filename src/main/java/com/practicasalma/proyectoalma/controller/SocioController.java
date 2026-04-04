package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.Launcher;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.service.SocioService;
import com.practicasalma.proyectoalma.util.FxUtils;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.image.Image;
import javafx.scene.control.TableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SocioController {

    @FXML private TableView<Socio> tablaSocios;
    @FXML private TableColumn<Socio, String> colNombre;
    @FXML private TableColumn<Socio, String> colApellidos;
    @FXML private TableColumn<Socio, String> colDni;
    @FXML private TableColumn<Socio, String> colTipoEntidad;
    @FXML private TableColumn<Socio, String> colPeriodicidad;
    @FXML private TableColumn<Socio, Double> colCuota;

    @FXML private ComboBox<String> comboTipoEntidadFiltro;
    @FXML private ComboBox<String> comboPeriodicidadFiltro;
    @FXML private TextField txtBuscar;
    @FXML private TableColumn<Socio, String> colEstado;

    private final SocioService socioService = new SocioService();
    private final ObservableList<Socio> listaMaestra = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellidos.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellidos()));
        colDni.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumentoIdentidad()));

        // Estas dos asumen que tienes los getters en tu modelo Socio. Si no, coméntalas de momento.
        colTipoEntidad.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipoEntidad()));
        colPeriodicidad.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPeriodicidad()));

        colCuota.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getCuota()).asObject());

        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(Boolean.TRUE.equals(cellData.getValue().getActivo()) ? "Activo" : "Baja"));
        colEstado.setCellFactory(FxUtils.celdaEstado());

        FilteredList<Socio> listaFiltrada = new FilteredList<>(listaMaestra, s -> true);
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            listaFiltrada.setPredicate(socio -> {
                if (newVal == null || newVal.isBlank()) return true;
                String filtro = newVal.toLowerCase();
                return socio.getNombre().toLowerCase().contains(filtro)
                        || socio.getApellidos().toLowerCase().contains(filtro);
            });
        });
        SortedList<Socio> listaSortable = new SortedList<>(listaFiltrada);
        listaSortable.comparatorProperty().bind(tablaSocios.comparatorProperty());
        tablaSocios.setItems(listaSortable);

        comboTipoEntidadFiltro.getSelectionModel().select(0);
        comboPeriodicidadFiltro.getSelectionModel().select(0);
        cargarSociosEnTabla();

        tablaSocios.setRowFactory(tv -> {
            TableRow<Socio> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Socio socioSeleccionado = row.getItem();
                    abrirFichaSocio(socioSeleccionado);
                }
            });
            return row ;
        });
    }

    public void nuevoSocio(ActionEvent actionEvent) {
        try {
            FxUtils.abrirModal("agregarSocio-view.fxml", "Agregar nuevo socio");
            cargarSociosEnTabla();
        } catch (IOException e) {
            System.err.println("Error al abrir el pop-up: " + e.getMessage());
        }
    }

    public void cargarSociosEnTabla() {
        listaMaestra.setAll(socioService.obtenerTodos());
    }

    private void abrirFichaSocio(Socio socio) {
        try {
            // 1. Cargar el FXML de la ficha
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/practicasalma/proyectoalma/view/ficha-socio.fxml"));
            Parent root = loader.load();

            // 2. OBTENER EL CONTROLADOR DE LA FICHA (¡Importante!)
            FichaSocioController controller = loader.getController();

            // 3. PASARLE EL ALUMNO SELECCIONADO
            controller.setSocio(socio);

            // 4. Mostrar la ventana nueva
            Stage stage = new Stage();
            stage.setTitle("Ficha del Alumno: " + socio.getNombre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL); // Bloquea la ventana de atrás hasta que cierres esta
            stage.initOwner(tablaSocios.getScene().getWindow()); // Dice que esta ventana pertenece a la principal
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al abrir la ficha: " + e.getMessage());
        }
    }

}
