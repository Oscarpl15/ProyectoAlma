package com.practicasalma.proyectoalma.util.config;

import com.practicasalma.proyectoalma.exception.ConfiguracionException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class GestorBBDD {

    private static EntityManagerFactory emf;

    public static void inicializar(String rutaAbsoluta) {
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url", "jdbc:sqlite:" + rutaAbsoluta);
        try {
            emf = Persistence.createEntityManagerFactory("ProyectoAlma", props);
        } catch (Throwable ex) {
            throw new ConfiguracionException("Error al inicializar la base de datos: " + ex.getMessage(), ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            throw new ConfiguracionException("La base de datos no ha sido inicializada. Comprueba la configuración.");
        }
        return emf;
    }
}
