package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import jakarta.persistence.EntityManager;

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
}