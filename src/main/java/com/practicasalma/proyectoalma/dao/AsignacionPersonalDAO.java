package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.model.Voluntario;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;

/**
 * Acceso a datos para la entidad {@link AsignacionPersonal}.
 * <p>
 * Una asignación registra la vinculación de un docente o voluntario a un curso
 * académico concreto. Exactamente una de las dos referencias ({@code docente} o
 * {@code voluntario}) debe ser no nula — la otra se deja a {@code null}.
 * Al guardar, se reemplaza el objeto detached por la entidad gestionada mediante
 * {@code em.find()} para evitar problemas de persistencia transitiva.
 * </p>
 */
public class AsignacionPersonalDAO {

    /**
     * Persiste una nueva asignación de personal, reemplazando las referencias
     * detached de docente/voluntario por sus entidades gestionadas.
     *
     * @param asig asignación a insertar; debe tener docente o voluntario con ID válido
     */
    public void guardar(AsignacionPersonal asig) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            if (asig.getDocente() != null) {
                Docente d = em.find(Docente.class, asig.getDocente().getId());
                asig.setDocente(d);
            }
            if (asig.getVoluntario() != null) {
                Voluntario v = em.find(Voluntario.class, asig.getVoluntario().getId());
                asig.setVoluntario(v);
            }
            em.persist(asig);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al guardar la asignación: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }
}
