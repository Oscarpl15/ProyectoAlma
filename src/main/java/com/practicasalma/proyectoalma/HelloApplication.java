package com.practicasalma.proyectoalma;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import jakarta.persistence.EntityManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class HelloApplication {

    public static void main(String[] args) {
        System.out.println("Iniciando Hibernate...");
        EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager();

        em.getTransaction().begin();

        Alumno prueba = new Alumno("Juan", "Pérez López", "avenida jajaja", LocalDate.now());
        em.persist(prueba); // Guardamos en la base de datos

        em.getTransaction().commit();
        em.close();

        System.out.println("Prueba Perfecta.");

    }
}
