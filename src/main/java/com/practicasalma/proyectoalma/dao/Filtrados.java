package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.util.GestorBBDD;
import com.practicasalma.proyectoalma.util.UtilFecha;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public List<Socio> porTipoEntidad(String tipoEntidad) {
        if (tipoEntidad == null || tipoEntidad.trim().isEmpty()) {
            return List.of();
        }

        String tipoNormalizado = tipoEntidad.trim().toLowerCase(Locale.ROOT);

        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Socio> query = cb.createQuery(Socio.class);
            Root<Socio> root = query.from(Socio.class);

            query.select(root).where(
                    cb.equal(cb.lower(root.get("tipoEntidad")), tipoNormalizado)
            );

            return em.createQuery(query).getResultList();
        } catch (Exception e) {
            System.err.println("Error al filtrar socios por tipo de entidad: " + e.getMessage());
            return List.of();
        }
    }

    public List<Alumno> porGrupoCursoActual(String grupo) {
        if (grupo == null || grupo.trim().isEmpty()) {
            return List.of();
        }

        String grupoNormalizado = grupo.trim().toLowerCase(Locale.ROOT);
        String cursoActual = UtilFecha.calcularCursoAcademico();

        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Alumno> query = cb.createQuery(Alumno.class);
            Root<Alumno> root = query.from(Alumno.class);
            Join<Object, Object> matriculas = root.join("matriculas");

            query.select(root)
                    .distinct(true)
                    .where(
                            cb.and(
                                    cb.equal(cb.lower(matriculas.get("grupoAsignado")), grupoNormalizado),
                                    cb.equal(matriculas.get("anyoAcademico"), cursoActual)
                            )
                    );

            return em.createQuery(query).getResultList();
        } catch (Exception e) {
            System.err.println("Error al filtrar alumnos por grupo del curso actual: " + e.getMessage());
            return List.of();
        }
    }
}


