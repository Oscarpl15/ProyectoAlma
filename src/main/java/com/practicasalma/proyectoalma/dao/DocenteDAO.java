package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException;
import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Acceso a datos para la entidad {@link Docente}.
 * <p>
 * Cubre las operaciones CRUD básicas y la carga del historial de asignaciones del docente.
 * El patrón de manejo de {@code EntityManager} es idéntico al resto de DAOs:
 * apertura, transacción, rollback en caso de error y cierre garantizado en {@code finally}.
 * </p>
 */
public class DocenteDAO {

    /**
     * Persiste un nuevo docente en la base de datos.
     *
     * @param docente entidad a insertar (sin ID asignado)
     */
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

    /**
     * Actualiza un docente existente (merge JPA).
     *
     * @param docente entidad con los campos modificados y el ID ya asignado
     */
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

    /**
     * Carga un docente con su historial de asignaciones y periodos de actividad inicializados.
     *
     * @param id identificador del docente
     * @return docente con colecciones listas para usar fuera del EntityManager
     * @throws com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException si el ID no existe
     */
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

    /**
     * Cambia el estado activo/baja de un docente.
     *
     * @param id     identificador del docente
     * @param activo {@code true} para reactivar, {@code false} para dar de baja
     * @throws com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException si el ID no existe
     */
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

    /**
     * Devuelve todos los docentes con su historial de asignaciones pre-cargado.
     *
     * @return lista de docentes, vacía si no hay ninguno
     */
    public List<Docente> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery(
                    "SELECT DISTINCT d FROM Docente d LEFT JOIN FETCH d.historialAsignaciones",
                    Docente.class).getResultList();
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar los docentes: " + e.getMessage(), e);
        }
    }

    /**
     * Comprueba si ya existe un docente con ese documento de identidad (sin distinguir mayúsculas).
     *
     * @param dni documento de identidad a verificar
     * @return {@code true} si ya hay un docente con ese DNI
     */
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
