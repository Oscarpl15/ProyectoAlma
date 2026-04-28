package com.practicasalma.proyectoalma.dao;

import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.ConfiguracionApp;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import jakarta.persistence.EntityManager;

/**
 * Acceso a datos para la tabla de configuración ({@link ConfiguracionApp}).
 * <p>
 * Los métodos de lectura devuelven {@code null} si la base de datos aún no está
 * inicializada (primer arranque antes de configurar la ruta), lo que permite que
 * {@link com.practicasalma.proyectoalma.util.config.GestorConfig} aplique valores
 * por defecto sin lanzar excepciones.
 * </p>
 */
public class ConfiguracionDAO {

    /**
     * Lee el valor asociado a la clave indicada.
     *
     * @param clave clave del parámetro de configuración
     * @return valor almacenado, o {@code null} si no existe o la BD no está lista
     */
    public String get(String clave) {
        try {
            try (EntityManager em = GestorBBDD.getEntityManagerFactory().createEntityManager()) {
                ConfiguracionApp conf = em.find(ConfiguracionApp.class, clave);
                return conf != null ? conf.getValor() : null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Guarda o actualiza el valor asociado a la clave indicada.
     *
     * @param clave  clave del parámetro
     * @param valor  valor a persistir; puede ser {@code null} para borrar el valor
     * @throws PersistenciaException si la BD no está inicializada o falla la escritura
     */
    public void set(String clave, String valor) {
        EntityManager em = null;
        try {
            em = GestorBBDD.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            ConfiguracionApp conf = em.find(ConfiguracionApp.class, clave);
            if (conf == null) {
                em.persist(new ConfiguracionApp(clave, valor));
            } else {
                conf.setValor(valor);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new PersistenciaException("Error al guardar configuración '" + clave + "': " + e.getMessage(), e);
        } finally {
            if (em != null) em.close();
        }
    }
}
