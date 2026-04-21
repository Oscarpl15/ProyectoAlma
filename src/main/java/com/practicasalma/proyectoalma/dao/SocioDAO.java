package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException;
import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Acceso a datos para la entidad {@link Socio}.
 * <p>
 * Gestiona la persistencia de socios de la fundación, incluyendo la carga de
 * sus donaciones y periodos de actividad. Nota: {@code Socio} no tiene campo
 * {@code fechaNacimiento} en el modelo, a diferencia de {@code Alumno} o {@code Docente}.
 * </p>
 */
public class SocioDAO {

    /**
     * Persiste un nuevo socio en la base de datos.
     *
     * @param socio entidad a insertar (sin ID asignado)
     */
    public void guardar(Socio socio) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.persist(socio);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al guardar el socio: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    /**
     * Actualiza un socio existente (merge JPA).
     *
     * @param socio entidad con los campos modificados y el ID ya asignado
     */
    public void actualizar(Socio socio) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.merge(socio);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al actualizar el socio: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    /**
     * Carga un socio con sus donaciones y periodos de actividad inicializados.
     *
     * @param id identificador del socio
     * @return socio con colecciones listas para usar fuera del EntityManager
     * @throws com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException si el ID no existe
     */
    public Socio obtenerCompleto(Long id) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            Socio socio = em.find(Socio.class, id);
            if (socio == null) throw new EntidadNoEncontradaException("Socio con id " + id + " no encontrado.");
            socio.getDonaciones().size();
            socio.getPeriodosActividad().size();
            return socio;
        } catch (EntidadNoEncontradaException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar los detalles del socio: " + e.getMessage(), e);
        }
    }

    /**
     * Devuelve todos los socios registrados.
     *
     * @return lista de socios, vacía si no hay ninguno
     */
    public List<Socio> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("SELECT s FROM Socio s", Socio.class).getResultList();
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar los socios: " + e.getMessage(), e);
        }
    }

    /**
     * Comprueba si ya existe un socio con ese documento de identidad (sin distinguir mayúsculas).
     *
     * @param dni documento de identidad a verificar
     * @return {@code true} si ya hay un socio con ese DNI
     */
    public boolean existeDni(String dni) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            Long count = em.createQuery(
                    "SELECT COUNT(s) FROM Socio s WHERE UPPER(s.documentoIdentidad) = UPPER(:dni)", Long.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new PersistenciaException("Error al verificar DNI del socio: " + e.getMessage(), e);
        }
    }
}
