package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException;
import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

public class DocenteDAO {

    public void guardar(Docente docente) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.persist(docente);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al guardar el docente: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    public void actualizar(Docente docente) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.merge(docente);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al actualizar el docente: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    public Docente obtenerCompleto(Long id) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            Docente docente = em.find(Docente.class, id);
            if (docente == null) throw new EntidadNoEncontradaException("Docente con id " + id + " no encontrado.");
            docente.getHistorialAsignaciones().size();
            docente.getPeriodosActividad().size();
            return docente;
        } catch (EntidadNoEncontradaException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar los detalles del docente: " + e.getMessage(), e);
        }
    }

    public void actualizarActivo(Long id, boolean activo) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            Docente d = em.find(Docente.class, id);
            if (d == null) throw new EntidadNoEncontradaException("Docente con id " + id + " no encontrado.");
            d.setActivo(activo);
            em.getTransaction().commit();
        } catch (EntidadNoEncontradaException e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al actualizar estado del docente: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    public List<Docente> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery(
                    "SELECT DISTINCT d FROM Docente d LEFT JOIN FETCH d.historialAsignaciones",
                    Docente.class).getResultList();
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar los docentes: " + e.getMessage(), e);
        }
    }

    public boolean existeDni(String dni) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            Long count = em.createQuery(
                    "SELECT COUNT(d) FROM Docente d WHERE UPPER(d.documentoIdentidad) = UPPER(:dni)", Long.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new PersistenciaException("Error al verificar DNI del docente: " + e.getMessage(), e);
        }
    }
}
