package com.practicasalma.proyectoalma.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class GestorConfig {

    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.alma";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.properties";
    private static final String CLAVE_BBDD = "ruta.bbdd";
    private static final String CLAVE_DOCUMENTOS = "ruta.documentos";

    public static String getRutaBBDD() {
        return leerPropiedad(CLAVE_BBDD);
    }

    public static String getRutaDocumentos() {
        return leerPropiedad(CLAVE_DOCUMENTOS);
    }

    public static void setRutaBBDD(String ruta) {
        guardarPropiedad(CLAVE_BBDD, ruta);
    }

    public static void setRutaDocumentos(String ruta) {
        guardarPropiedad(CLAVE_DOCUMENTOS, ruta);
    }

    public static boolean estaConfigurado() {
        String rutaBBDD = getRutaBBDD();
        String rutaDocs = getRutaDocumentos();
        return rutaBBDD != null && !rutaBBDD.isBlank()
                && rutaDocs != null && !rutaDocs.isBlank();
    }

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

        props.setProperty(clave, valor);
        try (FileOutputStream fos = new FileOutputStream(f)) {
            props.store(fos, "Configuracion Fundacion Alma");
        } catch (IOException e) {
            System.err.println("Error al guardar configuracion: " + e.getMessage());
        }
    }
}