package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException;
import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.Voluntario;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Acceso a datos para la entidad {@link Voluntario}.
 * <p>
 * Misma estructura que {@link DocenteDAO}: CRUD, carga de colecciones lazy,
 * cambio de estado activo/baja y verificación de DNI duplicado.
 * </p>
 */
public class VoluntarioDAO {

    /**
     * Persiste un nuevo voluntario en la base de datos.
     *
     * @param voluntario entidad a insertar (sin ID asignado)
     */
    public void guardar(Voluntario voluntario) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.persist(voluntario);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al guardar el voluntario: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    /**
     * Actualiza un voluntario existente (merge JPA).
     *
     * @param voluntario entidad con los campos modificados y el ID ya asignado
     */
    public void actualizar(Voluntario voluntario) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.merge(voluntario);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al actualizar el voluntario: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    /**
     * Carga un voluntario con su historial de asignaciones y periodos de actividad inicializados.
     *
     * @param id identificador del voluntario
     * @return voluntario con colecciones listas para usar fuera del EntityManager
     * @throws com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException si el ID no existe
     */
    public Voluntario obtenerCompleto(Long id) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            Voluntario voluntario = em.find(Voluntario.class, id);
            if (voluntario == null) throw new EntidadNoEncontradaException("Voluntario con id " + id + " no encontrado.");
            voluntario.getHistorialAsignaciones().size();
            voluntario.getPeriodosActividad().size();
            return voluntario;
        } catch (EntidadNoEncontradaException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar los detalles del voluntario: " + e.getMessage(), e);
        }
    }

    /**
     * Cambia el estado activo/baja de un voluntario.
     *
     * @param id     identificador del voluntario
     * @param activo {@code true} para reactivar, {@code false} para dar de baja
     * @throws com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException si el ID no existe
     */
    public void actualizarActivo(Long id, boolean activo) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            Voluntario v = em.find(Voluntario.class, id);
            if (v == null) throw new EntidadNoEncontradaException("Voluntario con id " + id + " no encontrado.");
            v.setActivo(activo);
            em.getTransaction().commit();
        } catch (EntidadNoEncontradaException e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al actualizar estado del voluntario: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    /**
     * Devuelve todos los voluntarios con su historial de asignaciones pre-cargado.
     *
     * @return lista de voluntarios, vacía si no hay ninguno
     */
    public List<Voluntario> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery(
                    "SELECT DISTINCT v FROM Voluntario v LEFT JOIN FETCH v.historialAsignaciones",
                    Voluntario.class).getResultList();
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar los voluntarios: " + e.getMessage(), e);
        }
    }

    /**
     * Comprueba si ya existe un voluntario con ese documento de identidad (sin distinguir mayúsculas).
     *
     * @param dni documento de identidad a verificar
     * @return {@code true} si ya hay un voluntario con ese DNI
     */
    public boolean existeDni(String dni) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            Long count = em.createQuery(
                    "SELECT COUNT(v) FROM Voluntario v WHERE UPPER(v.documentoIdentidad) = UPPER(:dni)", Long.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new PersistenciaException("Error al verificar DNI del voluntario: " + e.getMessage(), e);
        }
    }
}
