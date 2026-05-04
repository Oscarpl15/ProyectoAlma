package com.practicasalma.proyectoalma.util.config;

import com.practicasalma.proyectoalma.dao.ConfiguracionDAO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Gestor de la configuración persistente de la aplicación.
 * <p>
 * Usa dos almacenes según el tipo de parámetro:
 * <ul>
 *   <li><b>{@code ~/.alma/config.properties}</b> — solo los parámetros de arranque que se
 *       necesitan antes de abrir la BD: {@code ruta.bbdd} y {@code ruta.documentos}.</li>
 *   <li><b>Tabla {@code configuracion} de la BD</b> — el resto: grupos, correo, contraseña
 *       y configuración del recordatorio. Estos parámetros viajan con el fichero {@code .db}
 *       al cambiar de equipo.</li>
 * </ul>
 * El directorio {@code ~/.alma/} se crea automáticamente si no existe.
 * </p>
 * <p>
 * Singleton estático — no debe instanciarse.
 * </p>
 */
public class GestorConfig {

    private static final String CONFIG_DIR  = System.getProperty("user.home") + "/.alma";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.properties";

    // Claves del fichero de propiedades (solo arranque)
    private static final String CLAVE_BBDD       = "ruta.bbdd";
    private static final String CLAVE_DOCUMENTOS = "ruta.documentos";

    // Claves de la tabla configuracion en BD
    private static final String CLAVE_CORREO_REMITENTE        = "correo.remitente";
    private static final String CLAVE_CORREO_PASSWORD_APP     = "correo.password.app";
    private static final String CLAVE_GRUPOS                  = "grupos";
    private static final String CLAVE_CERTIFICADOS_ULTIMO_ANO = "certificados.ultimo_ano";

    private static final String GRUPOS_DEFECTO =
            "g1 lunes/miercoles,g2 lunes/miercoles,g1 martes/jueves,g2 martes/jueves";

    private static final ConfiguracionDAO configuracionDAO = new ConfiguracionDAO();

    private GestorConfig() {}

    // ── PARÁMETROS DE ARRANQUE (fichero de propiedades) ──────────────────────

    /**
     * Devuelve la ruta absoluta configurada para la base de datos.
     *
     * @return ruta al fichero {@code .db}, o {@code null} si no está configurada
     */
    public static String getRutaBBDD() {
        return leerPropiedad(CLAVE_BBDD);
    }

    /**
     * Guarda (o actualiza) la ruta de la base de datos en el fichero de configuración.
     *
     * @param ruta ruta absoluta al fichero {@code .db}
     */
    public static void setRutaBBDD(String ruta) {
        guardarPropiedad(CLAVE_BBDD, ruta);
    }

    /**
     * Devuelve la ruta absoluta configurada para el directorio de documentos.
     *
     * @return ruta al directorio raíz de documentos, o {@code null} si no está configurada
     */
    public static String getRutaDocumentos() {
        return leerPropiedad(CLAVE_DOCUMENTOS);
    }

    /**
     * Guarda (o actualiza) la ruta del directorio de documentos en el fichero de configuración.
     *
     * @param ruta ruta absoluta al directorio raíz de documentos
     */
    public static void setRutaDocumentos(String ruta) {
        guardarPropiedad(CLAVE_DOCUMENTOS, ruta);
    }

    /**
     * Indica si tanto la ruta de BD como la de documentos están configuradas.
     *
     * @return {@code true} si ambas rutas tienen valor no vacío
     */
    public static boolean estaConfigurado() {
        String rutaBBDD = getRutaBBDD();
        String rutaDocs = getRutaDocumentos();
        return rutaBBDD != null && !rutaBBDD.isBlank()
                && rutaDocs != null && !rutaDocs.isBlank();
    }

    // ── CORREO (BD) ───────────────────────────────────────────────────────────

    /**
     * Devuelve la dirección de correo remitente almacenada en la BD.
     *
     * @return dirección de correo, o {@code null} si no está configurada o la BD no está lista
     */
    public static String getCorreoRemitente() {
        return configuracionDAO.get(CLAVE_CORREO_REMITENTE);
    }

    /**
     * Guarda la dirección de correo remitente en la BD.
     *
     * @param correo dirección de correo
     */
    public static void setCorreoRemitente(String correo) {
        configuracionDAO.set(CLAVE_CORREO_REMITENTE, correo);
    }

    /**
     * Devuelve la contraseña de aplicación del correo almacenada en la BD.
     *
     * @return contraseña de aplicación, o {@code null} si no está configurada o la BD no está lista
     */
    public static String getCorreoPasswordApp() {
        return configuracionDAO.get(CLAVE_CORREO_PASSWORD_APP);
    }

    /**
     * Guarda la contraseña de aplicación del correo en la BD.
     *
     * @param passwordApp contraseña de aplicación de Gmail
     */
    public static void setCorreoPasswordApp(String passwordApp) {
        configuracionDAO.set(CLAVE_CORREO_PASSWORD_APP, passwordApp);
    }

    // ── GRUPOS (BD) ───────────────────────────────────────────────────────────

    /**
     * Devuelve la lista de grupos configurados.
     * Si la BD no está disponible o no hay valor guardado, devuelve los 4 grupos por defecto.
     *
     * @return lista mutable de nombres de grupos
     */
    public static List<String> getGrupos() {
        String valor = configuracionDAO.get(CLAVE_GRUPOS);
        if (valor == null || valor.isBlank()) {
            return new ArrayList<>(Arrays.asList(GRUPOS_DEFECTO.split(",")));
        }
        return new ArrayList<>(Arrays.asList(valor.split(",")));
    }

    /**
     * Guarda la lista de grupos en la BD.
     *
     * @param grupos lista de nombres de grupos a persistir
     */
    public static void setGrupos(List<String> grupos) {
        configuracionDAO.set(CLAVE_GRUPOS, String.join(",", grupos));
    }

    // ── CERTIFICADOS ANUALES (BD) ─────────────────────────────────────────────

    /** @return año en que se enviaron por última vez los certificados, o {@code null} si nunca */
    public static String getCertificadosUltimoAno() {
        return configuracionDAO.get(CLAVE_CERTIFICADOS_ULTIMO_ANO);
    }

    /** Persiste el año en que se han enviado los certificados anuales. */
    public static void setCertificadosUltimoAno(String ano) {
        configuracionDAO.set(CLAVE_CERTIFICADOS_ULTIMO_ANO, ano);
    }

    // ── ACCESO AL FICHERO DE PROPIEDADES (privado) ────────────────────────────

    private static String leerPropiedad(String clave) {
        File f = new File(CONFIG_FILE);
        if (!f.exists()) return null;
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(f)) {
            props.load(fis);
            return props.getProperty(clave);
        } catch (IOException e) {
            return null;
        }
    }

    private static void guardarPropiedad(String clave, String valor) {
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) dir.mkdirs();

        Properties props = new Properties();
        File f = new File(CONFIG_FILE);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                props.load(fis);
            } catch (IOException ignored) {}
        }

        props.setProperty(clave, valor != null ? valor : "");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            props.store(fos, "Configuracion Fundacion Alma");
        } catch (IOException e) {
            throw new com.practicasalma.proyectoalma.exception.ConfiguracionException(
                    "Error al guardar la configuración: " + e.getMessage(), e);
        }
    }
}
