package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class Filtrados {
    public List<Socio> porPeriodicidad(String valor) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Socio> query = cb.createQuery(Socio.class);
            Root<Socio> root = query.from(Socio.class);

            query.select(root).where(cb.equal(root.get("periodicidad"), valor));

            return em.createQuery(query).getResultList();
        } catch (Exception e) {
            System.err.println("Error al filtrar socios: " + e.getMessage());
            return List.of();
        }
    }
}


