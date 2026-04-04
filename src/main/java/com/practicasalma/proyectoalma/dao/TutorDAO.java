package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.model.Tutor;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TutorDAO {

    public void guardar(Tutor tutor) throws Exception {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.persist(tutor);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new Exception("Error al guardar el tutor: " + e.getMessage());
        } finally {
            if (em != null) em.close();
        }
    }

    public List<Tutor> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("SELECT t FROM Tutor t ORDER BY t.apellidos, t.nombre", Tutor.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error al cargar tutores: " + e.getMessage());
            return List.of();
        }
    }
}
