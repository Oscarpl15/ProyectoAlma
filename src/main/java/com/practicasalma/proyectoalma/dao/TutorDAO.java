package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.Tutor;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Acceso a datos para la entidad {@link Tutor}.
 * <p>
 * Los tutores son los representantes legales de los alumnos menores de edad.
 * Se vinculan al alumno mediante la tabla de unión {@code alumno_tutor} (relación N:M).
 * Un mismo tutor puede estar asignado a varios alumnos (hermanos, por ejemplo).
 * </p>
 */
public class TutorDAO {

    /**
     * Persiste un nuevo tutor en la base de datos.
     *
     * @param tutor entidad a insertar (sin ID asignado)
     */
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

    /**
     * Devuelve todos los tutores registrados ordenados por apellidos y nombre.
     *
     * @return lista de tutores, vacía si no hay ninguno
     */
    public List<Tutor> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("SELECT t FROM Tutor t ORDER BY t.apellidos, t.nombre", Tutor.class)
                    .getResultList();
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar los tutores: " + e.getMessage(), e);
        }
    }

    public Tutor buscarPorDocumento(String documento) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            List<Tutor> result = em.createQuery(
                    "SELECT t FROM Tutor t WHERE t.documentoIdentidad = :doc", Tutor.class)
                    .setParameter("doc", documento)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    public long contarAlumnos(Long tutorId) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery(
                    "SELECT COUNT(a) FROM Alumno a JOIN a.tutores t WHERE t.id = :id", Long.class)
                    .setParameter("id", tutorId)
                    .getSingleResult();
        } catch (Exception e) {
            return 1;
        }
    }

    public void eliminar(Tutor tutor) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            Tutor managed = em.find(Tutor.class, tutor.getId());
            if (managed != null) em.remove(managed);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            if (em != null) em.close();
        }
    }
}
