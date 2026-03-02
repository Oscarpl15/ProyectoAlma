package com.practicasalma.proyectoalma.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class GestorBBDD {
    private static final EntityManagerFactory emf = buildEntityManagerFactory();

    private static EntityManagerFactory buildEntityManagerFactory() {
        try {
            
            return Persistence.createEntityManagerFactory("ProyectoAlma");
        } catch (Throwable ex) {
            System.err.println("Error al inicializar JPA: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}
