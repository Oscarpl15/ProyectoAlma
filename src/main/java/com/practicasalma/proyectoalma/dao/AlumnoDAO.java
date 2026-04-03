package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class AlumnoDAO {

    public void guardar(Alumno alumno) throws Exception {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();

            // Persistimos el alumno (como tiene Cascade, guardará también su matrícula)
            em.persist(alumno);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new Exception("Error técnico al guardar en BBDD: " + e.getMessage());
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void actualizar(Alumno alumno) throws Exception {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();

            // MERGE es el comando mágico de Hibernate para hacer un UPDATE
            em.merge(alumno);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new Exception("Error técnico al actualizar en BBDD: " + e.getMessage());
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Alumno> obtenerTodos() {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Alumno> query = cb.createQuery(Alumno.class);
            Root<Alumno> root = query.from(Alumno.class);

            // --- LA MAGIA ESTÁ AQUÍ ---
            // Le decimos que se traiga las matrículas de paso (LEFT por si algún niño aún no tiene)
            root.fetch("matriculas", JoinType.LEFT);

            // Ponemos distinct(true) para que no nos devuelva alumnos duplicados si tienen 2 matrículas
            query.select(root).distinct(true);

            return em.createQuery(query).getResultList();
        } catch (Exception e) {
            System.err.println("Error al cargar todos los alumnos: " + e.getMessage());
            return List.of();
        }
    }
}