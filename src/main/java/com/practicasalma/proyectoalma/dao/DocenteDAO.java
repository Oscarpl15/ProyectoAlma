package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

public class DocenteDAO {

    public void guardar(Docente docente) throws Exception {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.persist(docente);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new Exception("Error al guardar docente: " + e.getMessage());
        } finally {
            if (em != null) em.close();
        }
    }

    public void actualizar(Docente docente) throws Exception {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.merge(docente);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new Exception("Error al actualizar docente: " + e.getMessage());
        } finally {
            if (em != null) em.close();
        }
    }

    public List<Docente> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("SELECT d FROM Docente d", Docente.class).getResultList();
        } catch (Exception e) {
            System.err.println("Error al cargar docentes: " + e.getMessage());
            return List.of();
        }
    }
}
