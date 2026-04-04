package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.Launcher;
import com.practicasalma.proyectoalma.model.Docente;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import com.practicasalma.proyectoalma.service.DocenteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
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
        colProtDatos.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getAutoProteccionDatos() != null ? cellData.getValue().getAutoProteccionDatos() : false));

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
            cargarDocentesEnTabla();

        } catch (IOException e) {
            System.err.println("Error al abrir el pop-up: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cargarDocentesEnTabla() {
        listaMaestra.setAll(docenteService.obtenerTodos());
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