package com.practicasalma.proyectoalma.util;

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
            System.err.println("Error al inicializar JPA: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            throw new IllegalStateException("GestorBBDD no ha sido inicializado. Llama a inicializar() primero.");
        }
        return emf;
    }
}