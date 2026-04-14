package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import jakarta.persistence.EntityManager;

public class MatriculaDAO {

    public void guardar(Matricula matricula) throws Exception {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            // Reattach el alumno como proxy para evitar LazyInitializationException
            Alumno ref = em.getReference(Alumno.class, matricula.getAlumno().getId());
            matricula.setAlumno(ref);
            em.persist(matricula);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new Exception("Error al guardar matrícula: " + e.getMessage());
        } finally {
            if (em != null) em.close();
        }
    }

    public void actualizar(Long id, String curso, String grupoAsignado, boolean esRepeticion) throws Exception {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            Matricula m = em.find(Matricula.class, id);
            if (m == null) throw new Exception("Matrícula con id " + id + " no encontrada.");
            m.setCurso(curso);
            m.setGrupoAsignado(grupoAsignado);
            m.setEsRepeticion(esRepeticion);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new Exception("Error al actualizar matrícula: " + e.getMessage());
        } finally {
            if (em != null) em.close();
        }
    }
}