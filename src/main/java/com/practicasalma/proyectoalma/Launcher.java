package com.practicasalma.proyectoalma;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        // Cargar el tema base de AtlantaFX
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // Cargar la vista principal (que ya incluye su propio CSS internamente)
        URL fxmlUrl = Launcher.class.getResource("/com/practicasalma/proyectoalma/view/main-view.fxml");

        if (fxmlUrl == null) {
            System.err.println("ERROR FATAL: No se encuentra el archivo FXML principal.");
            return;
        }

        Scene scene = new Scene(new FXMLLoader(fxmlUrl).load());

        // Configurar la ventana (Stage)
        stage.setTitle("Gestión Fundación - Panel de Control");

        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/practicasalma/proyectoalma/assets/logo.png")));
        } catch (Exception e) {
            System.err.println("AVISO: No se pudo cargar el logo de la aplicación.");
        }

        stage.setScene(scene);

        // Calcular pantalla y forzar maximizado sin parpadeos
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