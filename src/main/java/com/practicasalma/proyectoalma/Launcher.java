package com.practicasalma.proyectoalma;

import atlantafx.base.theme.PrimerLight;
import com.practicasalma.proyectoalma.controller.ConfiguracionInicialController;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Punto de entrada de la aplicación JavaFX.
 * <p>
 * Secuencia de arranque:
 * <ol>
 *   <li>Aplica el tema visual AtlantaFX {@code PrimerLight}.</li>
 *   <li>Lee la configuración de {@link com.practicasalma.proyectoalma.util.config.GestorConfig}.
 *       Si no está configurado, muestra el asistente de configuración inicial.</li>
 *   <li>Inicializa {@link com.practicasalma.proyectoalma.util.config.GestorBBDD} con la ruta de la BD.</li>
 *   <li>Ejecuta {@link com.practicasalma.proyectoalma.service.GestorMatriculas} y
 *       {@link com.practicasalma.proyectoalma.service.GestorAsignaciones} para procesar
 *       renovaciones automáticas de inicio de curso.</li>
 *   <li>Carga y muestra la vista principal ({@code main-view.fxml}).</li>
 * </ol>
 * </p>
 * <p>
 * {@link MainLauncher} es el wrapper de entrada real que llama a este {@code main()} —
 * necesario para evitar el error de módulos de JavaFX al ejecutar sin {@code module-info.java}.
 * </p>
 */
public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        String rutaBBDD = GestorConfig.getRutaBBDD();
        boolean necesitaConfiguracion = !GestorConfig.estaConfigurado()
                || rutaBBDD == null
                || !new File(rutaBBDD).exists();

        if (necesitaConfiguracion) {
            ConfiguracionInicialController.mostrar();
            rutaBBDD = GestorConfig.getRutaBBDD();
            if (rutaBBDD == null || rutaBBDD.isBlank()) {
                return;
            }
        }

        GestorBBDD.inicializar(rutaBBDD);

        URL fxmlUrl = Launcher.class.getResource("/com/practicasalma/proyectoalma/view/main-view.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("No se encuentra el archivo FXML principal.");
        }

        Scene scene = new Scene(new FXMLLoader(fxmlUrl).load());

        stage.setTitle("Gestion Fundacion - Panel de Control");

        FxUtils.aplicarIcono(stage);

        stage.setScene(scene);

        Rectangle2D limitesPantalla = Screen.getPrimary().getVisualBounds();
        stage.setX(limitesPantalla.getMinX());
        stage.setY(limitesPantalla.getMinY());
        stage.setWidth(limitesPantalla.getWidth());
        stage.setHeight(limitesPantalla.getHeight());
        stage.setMaximized(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}