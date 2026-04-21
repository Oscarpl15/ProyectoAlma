package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.PersonaAutorizada;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

public class PersonaAutorizadaDAO {

    public void guardar(PersonaAutorizada pa) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.persist(pa);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al guardar la persona autorizada: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    public List<PersonaAutorizada> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("SELECT p FROM PersonaAutorizada p ORDER BY p.apellidos, p.nombre", PersonaAutorizada.class)
                    .getResultList();
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar las personas autorizadas: " + e.getMessage(), e);
        }
    }
}
