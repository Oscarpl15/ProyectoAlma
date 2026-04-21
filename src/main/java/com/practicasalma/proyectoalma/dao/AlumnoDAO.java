package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException;
import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class AlumnoDAO {

    public void guardar(Alumno alumno) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.persist(alumno);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al guardar el alumno: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    public void actualizar(Alumno alumno) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.merge(alumno);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al actualizar el alumno: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    public List<Alumno> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Alumno> query = cb.createQuery(Alumno.class);
            Root<Alumno> root = query.from(Alumno.class);
            root.fetch("matriculas", JoinType.LEFT);
            query.select(root).distinct(true);
            return em.createQuery(query).getResultList();
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar los alumnos: " + e.getMessage(), e);
        }
    }

    public void actualizarActivo(Long id, boolean activo) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            Alumno a = em.find(Alumno.class, id);
            if (a == null) throw new EntidadNoEncontradaException("Alumno con id " + id + " no encontrado.");
            a.setActivo(activo);
            em.getTransaction().commit();
        } catch (EntidadNoEncontradaException e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al actualizar estado del alumno: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    public Alumno obtenerCompleto(Long id) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            Alumno alumno = em.find(Alumno.class, id);
            if (alumno == null) throw new EntidadNoEncontradaException("Alumno con id " + id + " no encontrado.");
            alumno.getMatriculas().size();
            alumno.getAutorizadaRecoger().size();
            alumno.getTutores().size();
            return alumno;
        } catch (EntidadNoEncontradaException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar los detalles del alumno: " + e.getMessage(), e);
        }
    }

    public boolean existeDni(String dni) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            Long count = em.createQuery(
                    "SELECT COUNT(a) FROM Alumno a WHERE UPPER(a.documentoIdentidad) = UPPER(:dni)", Long.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new PersistenciaException("Error al verificar DNI del alumno: " + e.getMessage(), e);
        }
    }
}
