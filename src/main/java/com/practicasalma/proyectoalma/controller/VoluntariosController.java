package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Voluntario;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;

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

    // Datos falsos para que la tabla no esté vacía
    // NOTA: Ajusta los parámetros del "new Voluntario(...)" si tu constructor es diferente
    private ObservableList<Voluntario> listaFalsa = FXCollections.observableArrayList(
            new Voluntario("Carlos", "Ruiz Gómez", "C/ Falsa 123", "600 11 22 33", "11223344X", "carlos@email.com", LocalDate.now()),
            new Voluntario("Ana", "Martínez", "Av. Libertad 4", "699 88 77 66", "99887766Z", "ana@email.com", LocalDate.now())
    );

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

        FilteredList<Voluntario> listaFiltrada = new FilteredList<>(listaFalsa, v -> true);
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

    @FXML
    private void nuevoVoluntario() {
        // Aquí cargaremos el Pop-up de agregar voluntario más adelante
        System.out.println("Botón: Abrir ventana de nuevo voluntario");
    }
}
