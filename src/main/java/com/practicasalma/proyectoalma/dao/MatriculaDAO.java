package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException;
import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;

/**
 * Acceso a datos para la entidad {@link Matricula}.
 * <p>
 * Gestiona la inserción y actualización de matrículas. Al guardar, se usa
 * {@code em.getReference()} para evitar cargar el alumno completo — solo
 * se necesita la FK. Al actualizar, solo se modifican los campos editables
 * desde la ficha de matrícula: curso, grupo asignado y si es repetición.
 * </p>
 */
public class MatriculaDAO {

    /**
     * Persiste una nueva matrícula asociada a un alumno existente.
     * Usa una referencia proxy del alumno para no cargar la entidad completa.
     *
     * @param matricula entidad a insertar; debe tener el alumno con ID válido
     */
    public void guardar(Matricula matricula) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            Alumno ref = em.getReference(Alumno.class, matricula.getAlumno().getId());
            matricula.setAlumno(ref);
            em.persist(matricula);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al guardar la matrícula: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    /**
     * Actualiza los campos editables de una matrícula existente.
     *
     * @param id            identificador de la matrícula
     * @param curso         nuevo valor del curso (p. ej., "Primaria 1")
     * @param grupoAsignado nuevo grupo (p. ej., "A")
     * @param esRepeticion  {@code true} si el alumno repite este curso
     * @throws com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException si el ID no existe
     */
    public void actualizar(Long id, String curso, String grupoAsignado, boolean esRepeticion) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            Matricula m = em.find(Matricula.class, id);
            if (m == null) throw new EntidadNoEncontradaException("Matrícula con id " + id + " no encontrada.");
            m.setCurso(curso);
            m.setGrupoAsignado(grupoAsignado);
            m.setEsRepeticion(esRepeticion);
            em.getTransaction().commit();
        } catch (EntidadNoEncontradaException e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al actualizar la matrícula: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }
}
