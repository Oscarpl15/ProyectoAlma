package com.practicasalma.proyectoalma;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL; // Importa esto para verificar

public class Launcher extends Application { // No hace falta poner javafx.application.Application si ya está importado arriba

    @Override
    public void start(Stage stage) throws IOException {

        // CORRECCIÓN 1: Usamos Launcher.class en lugar de Application.class
        // Esto le dice a Java: "Busca en MIS carpetas"
        URL fxmlUrl = Launcher.class.getResource("/com/practicasalma/proyectoalma/view/main-view.fxml");

        // DEBUG: Esto te dirá en la consola si realmente lo ha encontrado antes de fallar
        if (fxmlUrl == null) {
            System.out.println("ERROR FATAL: No se encuentra el archivo FXML en /view/main-view.fxml");
            System.out.println("Revisa que la carpeta 'resources' esté marcada como Root");
            return; // Paramos para no explotar
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);

        // CORRECCIÓN 2: También aquí cambiamos a Launcher.class
        URL cssUrl = Launcher.class.getResource("/com/practicasalma/proyectoalma/css/estilos.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("AVISO: No se ha encontrado el CSS, la app se verá fea pero funcionará.");
        }

        stage.setTitle("Gestión Fundación - Panel de Control");

        // Agregamos logo de la fundación a la ventana
        String rutaIcono = "/com/practicasalma/proyectoalma/assets/logo.png";
        stage.getIcons().add(new Image(getClass().getResourceAsStream(rutaIcono)));
        
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
