package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.model.PersonaAutorizada;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

public class PersonaAutorizadaDAO {

    public void guardar(PersonaAutorizada pa) throws Exception {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.persist(pa);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new Exception("Error al guardar la persona autorizada: " + e.getMessage());
        } finally {
            if (em != null) em.close();
        }
    }

    public List<PersonaAutorizada> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("SELECT p FROM PersonaAutorizada p ORDER BY p.apellidos, p.nombre", PersonaAutorizada.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error al cargar personas autorizadas: " + e.getMessage());
            return List.of();
        }
    }
}
