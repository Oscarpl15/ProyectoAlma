package com.practicasalma.proyectoalma.util.config;

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
 * Lee y escribe el fichero {@code ~/.alma/config.properties} que almacena:
 * <ul>
 *   <li>{@code ruta.bbdd} — ruta absoluta al fichero SQLite de la base de datos.</li>
 *   <li>{@code ruta.documentos} — ruta absoluta al directorio raíz de documentos.</li>
 * </ul>
 * El directorio {@code ~/.alma/} se crea automáticamente si no existe.
 * </p>
 * <p>
 * Singleton estático — no debe instanciarse.
 * </p>
 */
public class GestorConfig {

    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.alma";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.properties";
    private static final String CLAVE_BBDD = "ruta.bbdd";
    private static final String CLAVE_DOCUMENTOS = "ruta.documentos";
    private static final String CLAVE_CORREO_REMITENTE = "correo.remitente";
    private static final String CLAVE_CORREO_PASSWORD_APP = "correo.password.app";
    private static final String CLAVE_GRUPOS = "grupos";
    private static final String GRUPOS_DEFECTO = "g1 lunes/miercoles,g2 lunes/miercoles,g1 martes/jueves,g2 martes/jueves";
    private static final String CLAVE_RECORDATORIO_DESTINATARIOS = "correo.recordatorio.destinatarios";
    private static final String CLAVE_RECORDATORIO_ASUNTO = "correo.recordatorio.asunto";
    private static final String CLAVE_RECORDATORIO_CUERPO = "correo.recordatorio.cuerpo";
    private static final String CLAVE_RECORDATORIO_ULTIMO_ANO = "correo.recordatorio.ultimo_ano";
    private static final String CLAVE_RECORDATORIO_FECHA = "correo.recordatorio.fecha";

    private GestorConfig() {}

    /**
     * Devuelve la ruta absoluta configurada para la base de datos.
     *
     * @return ruta al fichero {@code .db}, o {@code null} si no está configurada
     */
    public static String getRutaBBDD() {
        return leerPropiedad(CLAVE_BBDD);
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
     * Guarda (o actualiza) la ruta de la base de datos en el fichero de configuración.
     *
     * @param ruta ruta absoluta al fichero {@code .db}
     */
    public static void setRutaBBDD(String ruta) {
        guardarPropiedad(CLAVE_BBDD, ruta);
    }

    /**
     * Guarda (o actualiza) la ruta del directorio de documentos en el fichero de configuración.
     *
     * @param ruta ruta absoluta al directorio raíz de documentos
     */
    public static void setRutaDocumentos(String ruta) {
        guardarPropiedad(CLAVE_DOCUMENTOS, ruta);
    }

    public static String getCorreoRemitente() {
        return leerPropiedad(CLAVE_CORREO_REMITENTE);
    }

    public static void setCorreoRemitente(String correo) {
        guardarPropiedad(CLAVE_CORREO_REMITENTE, correo);
    }

    public static String getRecordatorioDestinatarios() {
        return leerPropiedad(CLAVE_RECORDATORIO_DESTINATARIOS);
    }

    public static String getRecordatorioAsunto() {
        return leerPropiedad(CLAVE_RECORDATORIO_ASUNTO);
    }

    public static String getRecordatorioCuerpo() {
        return leerPropiedad(CLAVE_RECORDATORIO_CUERPO);
    }

    public static String getRecordatorioUltimoAno() {
        return leerPropiedad(CLAVE_RECORDATORIO_ULTIMO_ANO);
    }

    public static String getRecordatorioFecha() {
        return leerPropiedad(CLAVE_RECORDATORIO_FECHA);
    }

    public static void setRecordatorioDestinatarios(String destinatarios) {
        guardarPropiedad(CLAVE_RECORDATORIO_DESTINATARIOS, destinatarios);
    }

    public static void setRecordatorioAsunto(String asunto) {
        guardarPropiedad(CLAVE_RECORDATORIO_ASUNTO, asunto);
    }

    public static void setRecordatorioCuerpo(String cuerpo) {
        guardarPropiedad(CLAVE_RECORDATORIO_CUERPO, cuerpo);
    }

    public static void setRecordatorioUltimoAno(String ano) {
        guardarPropiedad(CLAVE_RECORDATORIO_ULTIMO_ANO, ano);
    }

    public static void setRecordatorioFecha(String fecha) {
        guardarPropiedad(CLAVE_RECORDATORIO_FECHA, fecha);
    }

    /**
     * Devuelve la lista de grupos configurados. Si no hay ninguno guardado, devuelve los 4 grupos por defecto.
     *
     * @return lista mutable de nombres de grupos
     */
    public static List<String> getGrupos() {
        String valor = leerPropiedad(CLAVE_GRUPOS);
        if (valor == null || valor.isBlank()) {
            return new ArrayList<>(Arrays.asList(GRUPOS_DEFECTO.split(",")));
        }
        return new ArrayList<>(Arrays.asList(valor.split(",")));
    }

    /**
     * Guarda la lista de grupos en el fichero de configuración.
     *
     * @param grupos lista de nombres de grupos a persistir
     */
    public static void setGrupos(List<String> grupos) {
        guardarPropiedad(CLAVE_GRUPOS, String.join(",", grupos));
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

    // Lee una clave del fichero de propiedades. Devuelve null si el fichero no existe o hay error.
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

    // Escribe o actualiza una clave en el fichero de propiedades, creando el directorio si hace falta.
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
