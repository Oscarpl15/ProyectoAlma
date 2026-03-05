package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.Launcher;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Socio;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SocioController {

    @FXML private TableView<Socio> tablaSocios;
    @FXML private TableColumn<Socio, String> colNombre;
    @FXML private TableColumn<Socio, String> colApellidos;
    @FXML private TableColumn<Socio, String> colDireccion;
    @FXML private TableColumn<Socio, String> colDni;
    @FXML private TableColumn<Socio, Double> colCuota;

    @FXML private ComboBox<String> comboTipoEntidadFiltro;
    @FXML private ComboBox<String> comboPeriodicidadFiltro;

    private ObservableList<Socio> listaFalsa = FXCollections.observableArrayList(
            new Socio("Asociación Vecinos", "Centro", "Calle Mayor 1", "G12345678", 50.0),
            new Socio("Juan", "Pérez García", "Av. Libertad 20", "12345678Z",  10.0)
    );

    @FXML
    public void initialize(){

        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellidos.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellidos()));
        colDireccion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDireccion()));
        colDni.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDni()));
        colCuota.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getCuota()).asObject());

        tablaSocios.setItems(listaFalsa);

        comboTipoEntidadFiltro.getSelectionModel().select(0);
        comboPeriodicidadFiltro.getSelectionModel().select(0);

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
            FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("view/agregarSocio-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setTitle("Agregar nuevo socio");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            String rutaIcono = "/com/practicasalma/proyectoalma/assets/logo.png";
            stage.getIcons().add(new Image(getClass().getResourceAsStream(rutaIcono)));
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error al abrir el pop-up: " + e.getMessage());
            e.printStackTrace();
        }
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

    private void mostrarMensaje(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
