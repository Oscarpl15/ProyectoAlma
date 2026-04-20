package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.model.Voluntario;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import jakarta.persistence.EntityManager;

public class AsignacionPersonalDAO {

    public void guardar(AsignacionPersonal asig) throws Exception {
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
            throw new Exception("Error al guardar asignación: " + e.getMessage());
        } finally {
            if (em != null) em.close();
        }
    }
}
