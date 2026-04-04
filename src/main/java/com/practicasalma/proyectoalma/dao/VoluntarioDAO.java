package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.model.Voluntario;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

public class VoluntarioDAO {

    public void guardar(Voluntario voluntario) throws Exception {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.persist(voluntario);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new Exception("Error al guardar voluntario: " + e.getMessage());
        } finally {
            if (em != null) em.close();
        }
    }

    public void actualizar(Voluntario voluntario) throws Exception {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.merge(voluntario);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new Exception("Error al actualizar voluntario: " + e.getMessage());
        } finally {
            if (em != null) em.close();
        }
    }

    public List<Voluntario> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("SELECT v FROM Voluntario v", Voluntario.class).getResultList();
        } catch (Exception e) {
            System.err.println("Error al cargar voluntarios: " + e.getMessage());
            return List.of();
        }
    }
}
