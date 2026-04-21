package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.PersonaAutorizada;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Acceso a datos para la entidad {@link PersonaAutorizada}.
 * <p>
 * Las personas autorizadas son las que pueden recoger a un alumno del centro.
 * Se vinculan al alumno en la tabla de unión {@code alumno_autorizada}.
 * Este DAO solo necesita insertar nuevas personas y listarlas todas para poder
 * asignarlas a alumnos desde la ficha de alumno.
 * </p>
 */
public class PersonaAutorizadaDAO {

    /**
     * Persiste una nueva persona autorizada en la base de datos.
     *
     * @param pa entidad a insertar (sin ID asignado)
     */
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

    /**
     * Devuelve todas las personas autorizadas ordenadas por apellidos y nombre.
     *
     * @return lista de personas autorizadas, vacía si no hay ninguna
     */
    public List<PersonaAutorizada> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("SELECT p FROM PersonaAutorizada p ORDER BY p.apellidos, p.nombre", PersonaAutorizada.class)
                    .getResultList();
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar las personas autorizadas: " + e.getMessage(), e);
        }
    }
}
