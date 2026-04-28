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

/**
 * Acceso a datos para la entidad {@link Alumno}.
 * <p>
 * Encapsula todas las operaciones de persistencia JPA relativas a alumnos:
 * inserción, actualización, consulta con eager-loading de colecciones y
 * verificación de duplicados por DNI. Todos los métodos usan su propio
 * {@code EntityManager} de corta vida y hacen rollback automático en caso de error.
 * </p>
 * <p>
 * Los errores de base de datos se envuelven en {@link com.practicasalma.proyectoalma.exception.PersistenciaException};
 * los accesos a IDs inexistentes lanzan {@link com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException}.
 * </p>
 */
public class AlumnoDAO {

    /**
     * Persiste un nuevo alumno en la base de datos.
     *
     * @param alumno entidad a insertar (sin ID asignado)
     * @throws com.practicasalma.proyectoalma.exception.PersistenciaException si Hibernate no puede persistir
     */
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

    /**
     * Actualiza un alumno existente (merge JPA).
     *
     * @param alumno entidad con los campos modificados y el ID ya asignado
     */
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

    /**
     * Devuelve todos los alumnos con sus matrículas pre-cargadas (LEFT JOIN FETCH).
     * El DISTINCT evita duplicados provocados por el join.
     *
     * @return lista de alumnos, vacía si no hay ninguno
     */
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

    /**
     * Cambia el estado activo/baja de un alumno sin tocar el resto de sus datos.
     *
     * @param id     identificador del alumno
     * @param activo {@code true} para reactivar, {@code false} para dar de baja
     * @throws com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException si el ID no existe
     */
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

    /**
     * Carga un alumno con todas sus colecciones inicializadas:
     * matrículas, personas autorizadas a recogerle y tutores.
     * Se hace forzando la inicialización de cada lazy collection dentro del mismo contexto JPA.
     *
     * @param id identificador del alumno
     * @return alumno con colecciones listas para usar fuera del EntityManager
     * @throws com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException si el ID no existe
     */
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

    /**
     * Actualiza el grupo asignado en las matrículas del año académico indicado
     * cuyo grupo coincida con {@code grupoViejo}. Si {@code grupoNuevo} es {@code null},
     * los registros quedan sin grupo (borrado lógico de la asignación).
     *
     * @param grupoViejo nombre actual del grupo tal como está almacenado en BD
     * @param grupoNuevo nuevo nombre del grupo, o {@code null} para dejar sin grupo
     * @param anyo       año académico en formato {@code "AAAA/AAAA+1"}
     */
    public void actualizarGrupoEnCursoActual(String grupoViejo, String grupoNuevo, String anyo) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.createQuery(
                    "UPDATE Matricula m SET m.grupoAsignado = :nuevo WHERE m.grupoAsignado = :viejo AND m.anyoAcademico = :anyo")
                    .setParameter("nuevo", grupoNuevo)
                    .setParameter("viejo", grupoViejo)
                    .setParameter("anyo", anyo)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al actualizar grupo en matrículas: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    /**
     * Comprueba si ya existe un alumno con ese documento de identidad (sin distinguir mayúsculas).
     * Solo busca en la tabla {@code Alumno} — el mismo DNI puede existir en otras tablas (Socio, Docente…).
     *
     * @param dni documento de identidad a verificar
     * @return {@code true} si ya hay un alumno con ese DNI
     */
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
