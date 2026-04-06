package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

public class SocioDAO {

    public void guardar(Socio socio) throws Exception {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.persist(socio);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new Exception("Error al guardar socio: " + e.getMessage());
        } finally {
            if (em != null) em.close();
        }
    }

    public void actualizar(Socio socio) throws Exception {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.merge(socio);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new Exception("Error al actualizar socio: " + e.getMessage());
        } finally {
            if (em != null) em.close();
        }
    }

    public Socio obtenerCompleto(Long id) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            Socio socio = em.find(Socio.class, id);
            if (socio != null) {
                socio.getDonaciones().size();
                socio.getPeriodosActividad().size();
            }
            return socio;
        } catch (Exception e) {
            System.err.println("Error al cargar detalles del socio: " + e.getMessage());
            return null;
        }
    }

    public List<Socio> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("SELECT s FROM Socio s", Socio.class).getResultList();
        } catch (Exception e) {
            System.err.println("Error al cargar socios: " + e.getMessage());
            return List.of();
        }
    }
}
