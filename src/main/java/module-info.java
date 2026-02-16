module com.practicasalma.proyectoalma {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.practicasalma.proyectoalma.controller to javafx.fxml;

    opens com.practicasalma.proyectoalma to javafx.fxml;
    exports com.practicasalma.proyectoalma;
}