package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.Tutor;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TutorDAO {

    public void guardar(Tutor tutor) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.persist(tutor);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al guardar el tutor: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    public List<Tutor> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("SELECT t FROM Tutor t ORDER BY t.apellidos, t.nombre", Tutor.class)
                    .getResultList();
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar los tutores: " + e.getMessage(), e);
        }
    }
}
