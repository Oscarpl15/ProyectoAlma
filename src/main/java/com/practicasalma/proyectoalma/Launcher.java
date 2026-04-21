package com.practicasalma.proyectoalma;

import atlantafx.base.theme.PrimerLight;
import com.practicasalma.proyectoalma.controller.ConfiguracionInicialController;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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

        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/practicasalma/proyectoalma/assets/logo.png")));
        } catch (Exception ignored) {}

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