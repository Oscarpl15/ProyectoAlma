package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.EntidadNoEncontradaException;
import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
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
     * Actualiza únicamente los campos escalares de un alumno y los campos escalares
     * de sus matrículas existentes. No toca las colecciones ManyToMany
     * (autorizadaRecoger, tutores, familiares) para evitar problemas de merge
     * con entidades detached del PersistentBag de Hibernate.
     * Usar para el guardado de la ficha del alumno; los vínculos de personas
     * autorizadas y tutores se gestionan por sus propios métodos.
     */
    public void actualizarDatosPersonales(Alumno alumno) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            Alumno managed = em.find(Alumno.class, alumno.getId());
            if (managed == null) throw new EntidadNoEncontradaException("Alumno con id " + alumno.getId() + " no encontrado.");

            managed.setNombre(alumno.getNombre());
            managed.setApellidos(alumno.getApellidos());
            managed.setDocumentoIdentidad(alumno.getDocumentoIdentidad());
            managed.setTipoDocumento(alumno.getTipoDocumento());
            managed.setDireccion(alumno.getDireccion());
            managed.setTelefono(alumno.getTelefono());
            managed.setCorreo(alumno.getCorreo());
            managed.setActivo(alumno.getActivo());
            managed.setCiudad(alumno.getCiudad());
            managed.setCodigoPostal(alumno.getCodigoPostal());
            managed.setNacionalidad(alumno.getNacionalidad());
            managed.setGenero(alumno.getGenero());
            managed.setRutaFotoPerfil(alumno.getRutaFotoPerfil());

            managed.setFechaNacimiento(alumno.getFechaNacimiento());
            managed.setAutorizaUsoDatos(alumno.getAutorizaUsoDatos());
            managed.setAutorizaImagen(alumno.getAutorizaImagen());
            managed.setAutorizaActividades(alumno.getAutorizaActividades());
            managed.setAutorizaComunicaciones(alumno.getAutorizaComunicaciones());
            managed.setAutorizaIrseSolo(alumno.getAutorizaIrseSolo());
            managed.setRutaDocAutoriza(alumno.getRutaDocAutoriza());
            managed.setColegio(alumno.getColegio());
            managed.setSeguimientoServiciosSociales(alumno.getSeguimientoServiciosSociales());
            managed.setSeguimientoSaf(alumno.getSeguimientoSaf());
            managed.setDerivacionSS(alumno.getDerivacionSS());
            managed.setDerivacionSaf(alumno.getDerivacionSaf());
            managed.setDerivacionEoep(alumno.getDerivacionEoep());
            managed.setDerivacionColegio(alumno.getDerivacionColegio());
            managed.setDerivacionOtro(alumno.getDerivacionOtro());
            managed.setDerivadoPor(alumno.getDerivadoPor());
            managed.setNumRepeticionesPrevias(alumno.getNumRepeticionesPrevias());
            managed.setDetalleCursosRepetidos(alumno.getDetalleCursosRepetidos());

            for (Matricula detachedM : alumno.getMatriculas()) {
                if (detachedM.getId() != null) {
                    Matricula managedM = em.find(Matricula.class, detachedM.getId());
                    if (managedM != null) {
                        managedM.setCurso(detachedM.getCurso());
                        managedM.setGrupoAsignado(detachedM.getGrupoAsignado());
                        managedM.setEsRepeticion(detachedM.getEsRepeticion());
                        managedM.setAnyoAcademico(detachedM.getAnyoAcademico());
                    }
                }
            }

            em.getTransaction().commit();
        } catch (EntidadNoEncontradaException e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al actualizar el alumno: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    /**
     * Actualiza un alumno existente (merge JPA).
     * Usar solo cuando se necesite sincronizar colecciones (darDeBaja, darDeAlta,
     * vincular/desvincular tutores y personas autorizadas).
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
     * Devuelve todos los alumnos con sus matrículas y tutores pre-cargados.
     * El DISTINCT evita duplicados provocados por el join de matrículas.
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
            List<Alumno> alumnos = em.createQuery(query).getResultList();
            em.createQuery("SELECT DISTINCT a FROM Alumno a LEFT JOIN FETCH a.tutores", Alumno.class)
              .getResultList();
            return alumnos;
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
            alumno.getFamiliares().size();
            alumno.getPeriodosActividad().size();
            return alumno;
        } catch (EntidadNoEncontradaException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar los detalles del alumno: " + e.getMessage(), e);
        }
    }

    /**
     * Renombra un grupo en todas las matrículas, independientemente del año académico.
     * Se usa al cambiar el nombre de un grupo: la coherencia del nombre debe mantenerse
     * en toda la historia del alumno.
     *
     * @param grupoViejo nombre actual del grupo tal como está almacenado en BD
     * @param grupoNuevo nuevo nombre del grupo
     */
    public void renombrarGrupoEnTodasLasMatriculas(String grupoViejo, String grupoNuevo) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.createQuery(
                    "UPDATE Matricula m SET m.grupoAsignado = :nuevo WHERE m.grupoAsignado = :viejo")
                    .setParameter("nuevo", grupoNuevo)
                    .setParameter("viejo", grupoViejo)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al renombrar grupo en matrículas: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    /**
     * Elimina la asignación de grupo (pone {@code null}) en las matrículas del año académico
     * indicado cuyo grupo coincida con {@code grupoEliminado}. Las matrículas de años
     * anteriores no se tocan, preservando el historial.
     *
     * @param grupoEliminado nombre del grupo eliminado
     * @param anyo           año académico en formato {@code "AAAA/AAAA+1"}
     */
    public void eliminarGrupoEnCursoActual(String grupoEliminado, String anyo) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.createQuery(
                    "UPDATE Matricula m SET m.grupoAsignado = NULL WHERE m.grupoAsignado = :viejo AND m.anyoAcademico = :anyo")
                    .setParameter("viejo", grupoEliminado)
                    .setParameter("anyo", anyo)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al eliminar grupo en matrículas: " + e.getMessage(), e);
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

    /**
     * Comprueba si otro alumno distinto al indicado ya tiene ese DNI.
     * Se usa en actualizaciones para evitar duplicados sin bloquear al propio registro.
     *
     * @param dni         documento de identidad a verificar
     * @param idExcluido  ID del alumno que se está editando (se excluye de la búsqueda)
     * @return {@code true} si otro alumno distinto ya tiene ese DNI
     */
    public boolean existeDniParaOtro(String dni, Long idExcluido) {
        try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
            Long count = em.createQuery(
                    "SELECT COUNT(a) FROM Alumno a WHERE UPPER(a.documentoIdentidad) = UPPER(:dni) AND a.id <> :id", Long.class)
                    .setParameter("dni", dni)
                    .setParameter("id", idExcluido)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new PersistenciaException("Error al verificar DNI del alumno: " + e.getMessage(), e);
        }
    }

    public void vincularPersonaAutorizada(Long alumnoId, Long paId) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.createNativeQuery(
                    "INSERT OR IGNORE INTO alumno_persona_autorizada (alumno_id, persona_autorizada_id) VALUES (?, ?)")
                    .setParameter(1, alumnoId)
                    .setParameter(2, paId)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al vincular persona autorizada: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    public void desvincularPersonaAutorizada(Long alumnoId, Long paId) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.createNativeQuery(
                    "DELETE FROM alumno_persona_autorizada WHERE alumno_id = ? AND persona_autorizada_id = ?")
                    .setParameter(1, alumnoId)
                    .setParameter(2, paId)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al desvincular persona autorizada: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    public void vincularTutor(Long alumnoId, Long tutorId) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.createNativeQuery(
                    "INSERT OR IGNORE INTO alumno_tutor (alumno_id, tutor_id) VALUES (?, ?)")
                    .setParameter(1, alumnoId)
                    .setParameter(2, tutorId)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al vincular tutor: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }

    public void desvincularTutor(Long alumnoId, Long tutorId) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.createNativeQuery(
                    "DELETE FROM alumno_tutor WHERE alumno_id = ? AND tutor_id = ?")
                    .setParameter(1, alumnoId)
                    .setParameter(2, tutorId)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al desvincular tutor: " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }
}
