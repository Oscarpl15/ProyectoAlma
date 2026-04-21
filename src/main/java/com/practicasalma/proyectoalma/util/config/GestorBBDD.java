package com.practicasalma.proyectoalma.util.config;

import com.practicasalma.proyectoalma.exception.ConfiguracionException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Punto central de acceso al {@link EntityManagerFactory} de JPA/Hibernate.
 * <p>
 * La inicialización es diferida: no se crea el factory al arrancar la JVM,
 * sino cuando {@link Launcher} llama explícitamente a {@link #inicializar(String)}
 * una vez que la ruta de la base de datos está disponible en la configuración.
 * </p>
 * <p>
 * Singleton estático — no debe instanciarse.
 * </p>
 */
public class GestorBBDD {

    private static EntityManagerFactory emf;

    private GestorBBDD() {}

    /**
     * Crea el {@link EntityManagerFactory} apuntando a la base de datos SQLite indicada.
     * <p>
     * Debe llamarse una única vez al arrancar la aplicación, antes de cualquier acceso a datos.
     * Hibernate aplicará {@code hbm2ddl.auto=update}, creando o actualizando las tablas si es necesario.
     * </p>
     *
     * @param rutaAbsoluta ruta absoluta al fichero {@code .db} de SQLite
     * @throws ConfiguracionException si Hibernate no puede inicializarse con la ruta proporcionada
     */
    public static void inicializar(String rutaAbsoluta) {
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url", "jdbc:sqlite:" + rutaAbsoluta);
        try {
            emf = Persistence.createEntityManagerFactory("ProyectoAlma", props);
        } catch (Throwable ex) {
            throw new ConfiguracionException("Error al inicializar la base de datos: " + ex.getMessage(), ex);
        }
    }

    /**
     * Devuelve el {@link EntityManagerFactory} ya inicializado.
     *
     * @return factory de JPA listo para crear {@code EntityManager}
     * @throws ConfiguracionException si {@link #inicializar(String)} no se ha llamado previamente
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            throw new ConfiguracionException("La base de datos no ha sido inicializada. Comprueba la configuración.");
        }
        return emf;
    }
}
