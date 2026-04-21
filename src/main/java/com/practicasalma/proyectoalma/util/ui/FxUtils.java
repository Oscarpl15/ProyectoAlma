package com.practicasalma.proyectoalma.util.ui;

import com.practicasalma.proyectoalma.Launcher;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

public class FxUtils {

    private static final String RUTA_ICONO = "/com/practicasalma/proyectoalma/assets/logo.png";

    public static void abrirModal(String nombreFxml, String titulo) throws IOException {
        FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("view/" + nombreFxml));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(titulo);
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);

        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        stage.setMaxWidth(pantalla.getWidth() * 0.92);
        stage.setMaxHeight(pantalla.getHeight() * 0.92);
        stage.centerOnScreen();

        try {
            stage.getIcons().add(new Image(FxUtils.class.getResourceAsStream(RUTA_ICONO)));
        } catch (Exception ignored) {}
        stage.showAndWait();
    }

    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> celdaEstado() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("celda-activo", "celda-baja");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("Activo".equals(item) ? "celda-activo" : "celda-baja");
                }
            }
        };
    }

    public static void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
