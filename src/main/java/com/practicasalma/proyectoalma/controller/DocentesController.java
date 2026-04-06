package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.Launcher;
import com.practicasalma.proyectoalma.model.Docente;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import com.practicasalma.proyectoalma.service.DocenteService;
import com.practicasalma.proyectoalma.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class DocentesController {

    @FXML private TableView<Docente> tablaDocentes;
    @FXML private TableColumn<Docente, String> colNombre;
    @FXML private TableColumn<Docente, String> colApellidos;
    @FXML private TableColumn<Docente, String> colTelefono;
    @FXML private TableColumn<Docente, String> colDni;
    @FXML private TableColumn<Docente, String> colCorreo;

    // Las nuevas columnas de documentación y estado
    @FXML private TableColumn<Docente, Boolean> colDelitos;
    @FXML private TableColumn<Docente, Boolean> colProtDatos;
    @FXML private TableColumn<Docente, String> colAB;
    @FXML private TextField txtBuscar;

    private final DocenteService docenteService = new DocenteService();
    private final ObservableList<Docente> listaMaestra = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Enlazar columnas con el modelo (Hemos quitado la de dirección)
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellidos.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellidos()));
        colTelefono.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTelefono()));
        colDni.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumentoIdentidad()));
        colCorreo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCorreo()));

        // Enlazamos los booleanos nuevos (Asumiendo que hiciste los getters en el modelo Docente)
        // Si aún no los tienes en el modelo, comenta estas dos líneas para que no de error
        colDelitos.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getAutoDelitosSexuales() != null ? cellData.getValue().getAutoDelitosSexuales() : false));
        colDelitos.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("celda-booleana-true", "celda-booleana-false");
                if (empty || item == null) { setText(null); }
                else { setText(item ? "✓" : "✗"); getStyleClass().add(item ? "celda-booleana-true" : "celda-booleana-false"); }
            }
        });

        colProtDatos.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getAutoProteccionDatos() != null ? cellData.getValue().getAutoProteccionDatos() : false));
        colProtDatos.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("celda-booleana-true", "celda-booleana-false");
                if (empty || item == null) { setText(null); }
                else { setText(item ? "✓" : "✗"); getStyleClass().add(item ? "celda-booleana-true" : "celda-booleana-false"); }
            }
        });

        colAB.setCellValueFactory(cellData ->
                new SimpleStringProperty(Boolean.TRUE.equals(cellData.getValue().getActivo()) ? "Activo" : "Baja"));
        colAB.setCellFactory(FxUtils.celdaEstado());

        // 2. Filtrado en tiempo real sobre datos de BD
        FilteredList<Docente> listaFiltrada = new FilteredList<>(listaMaestra, d -> true);
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            listaFiltrada.setPredicate(docente -> {
                if (newVal == null || newVal.isBlank()) return true;
                String filtro = newVal.toLowerCase();
                return docente.getNombre().toLowerCase().contains(filtro)
                        || docente.getApellidos().toLowerCase().contains(filtro);
            });
        });
        SortedList<Docente> listaSortable = new SortedList<>(listaFiltrada);
        listaSortable.comparatorProperty().bind(tablaDocentes.comparatorProperty());
        tablaDocentes.setItems(listaSortable);

        cargarDocentesEnTabla();

        // 3. EVENTO DOBLE CLIC
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
            FxUtils.abrirModal("agregarDocente-view.fxml", "Agregar docente");
            cargarDocentesEnTabla();
        } catch (IOException e) {
            System.err.println("Error al abrir el pop-up: " + e.getMessage());
        }
    }

    public void cargarDocentesEnTabla() {
        listaMaestra.setAll(docenteService.obtenerTodos());
    }

    private void abrirFichaDocente(Docente docenteTabla) {
        try {
            Docente docente = docenteService.obtenerCompleto(docenteTabla.getId());
            if (docente == null) return;

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
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al abrir la ficha: " + e.getMessage());
        }
    }
}