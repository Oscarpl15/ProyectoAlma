package com.practicasalma.proyectoalma.util;

import com.practicasalma.proyectoalma.Launcher;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

public class FxUtils {

    private static final String RUTA_ICONO = "/com/practicasalma/proyectoalma/assets/logo.png";

    /**
     * Abre un formulario emergente (modal) y espera a que se cierre.
     * Uso: FxUtils.abrirModal("agregarAlumno-view.fxml", "Agregar alumno");
     */
    public static void abrirModal(String nombreFxml, String titulo) throws IOException {
        FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("view/" + nombreFxml));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(titulo);
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        try {
            stage.getIcons().add(new Image(FxUtils.class.getResourceAsStream(RUTA_ICONO)));
        } catch (Exception ignored) {}
        stage.showAndWait();
    }

    /**
     * Devuelve el CellFactory para columnas de Estado (Activo / Baja) con sus estilos CSS.
     * Uso: columna.setCellFactory(FxUtils.celdaEstado());
     */
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

    /**
     * Muestra un Alert informativo, de aviso o de error.
     * Uso: FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Título", "Mensaje");
     */
    public static void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
