package com.practicasalma.proyectoalma.util.ui;

import com.practicasalma.proyectoalma.Launcher;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import java.util.Optional;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

/**
 * Utilidades de interfaz de usuario reutilizables en los controllers JavaFX.
 * <p>
 * Centraliza operaciones comunes como abrir ventanas modales, mostrar alertas
 * y configurar cell factories para tablas, evitando código repetido en los controllers.
 * </p>
 * <p>
 * Singleton estático — no debe instanciarse.
 * </p>
 */
public class FxUtils {

    private static final String RUTA_ICONO = "/com/practicasalma/proyectoalma/assets/LogoCertificado.png";

    private FxUtils() {}

    public static void aplicarIcono(Stage stage) {
        try {
            stage.getIcons().add(new Image(FxUtils.class.getResourceAsStream(RUTA_ICONO)));
        } catch (Exception ignored) {}
    }

    public static void aplicarIconoADialog(Dialog<?> dialog) {
        dialog.setOnShowing(e -> {
            try {
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                aplicarIcono(stage);
            } catch (Exception ignored) {}
        });
    }

    /**
     * Abre una ventana modal (bloqueante) cargando el FXML indicado.
     * <p>
     * La ventana se centra en pantalla y está limitada al 92% del área visible
     * para adaptarse a resoluciones pequeñas. La ejecución se detiene hasta que
     * el usuario cierre la ventana.
     * </p>
     *
     * @param nombreFxml nombre del fichero FXML dentro de {@code resources/.../view/}
     *                   (p. ej., {@code "agregarAlumno-view.fxml"})
     * @param titulo     título de la ventana
     * @throws IOException si el fichero FXML no se encuentra o no se puede cargar
     */
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

    /**
     * Devuelve un {@code CellFactory} que colorea las celdas de estado con CSS.
     * <p>
     * Las celdas con valor {@code "Activo"} reciben la clase CSS {@code celda-activo};
     * el resto reciben {@code celda-baja}.
     * </p>
     *
     * @param <T> tipo del modelo de fila de la tabla
     * @return callback listo para asignar con {@code columna.setCellFactory(...)}
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
     * Muestra un diálogo de alerta y espera a que el usuario lo cierre.
     *
     * @param tipo    tipo de alerta ({@code INFORMATION}, {@code WARNING}, {@code ERROR})
     * @param titulo  título de la ventana
     * @param mensaje contenido del mensaje
     */
    public static void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        aplicarEstilos(alert);
        alert.showAndWait();
    }

    public static Optional<ButtonType> mostrarConfirmacion(String titulo, String cabecera, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecera);
        alert.setContentText(mensaje);
        aplicarEstilos(alert);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> mostrarConfirmacionSiNo(String titulo, String cabecera, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecera);
        alert.setContentText(mensaje);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        aplicarEstilos(alert);
        return alert.showAndWait();
    }

    private static void aplicarEstilos(Alert alert) {
        try {
            String css = FxUtils.class.getResource(
                    "/com/practicasalma/proyectoalma/css/estilos.css").toExternalForm();
            alert.getDialogPane().getStylesheets().add(css);
        } catch (Exception ignored) {}
        alert.setOnShowing(e -> {
            try {
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                aplicarIcono(stage);
            } catch (Exception ignored) {}
        });
    }
}
