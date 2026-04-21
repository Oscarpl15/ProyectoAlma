package com.practicasalma.proyectoalma;

/**
 * Clase de entrada real del ejecutable ({@code MainLauncher} es la clase configurada en el MANIFEST).
 * <p>
 * Actúa como wrapper de {@link Launcher} para evitar el error
 * <i>"JavaFX runtime components are missing"</i> que ocurre cuando la clase principal
 * extiende {@code javafx.application.Application} y no hay {@code module-info.java}.
 * </p>
 */
public class MainLauncher {
    public static void main(String[] args) {
        Launcher.main(args);
    }
}
