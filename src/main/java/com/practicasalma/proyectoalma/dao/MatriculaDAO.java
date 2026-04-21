package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException;
import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;

public class MatriculaDAO {

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
