package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.DocenteDAO;
import com.practicasalma.proyectoalma.dao.VoluntarioDAO;
import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.model.Voluntario;

import com.practicasalma.proyectoalma.exception.ConfiguracionException;
import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Gestor del proceso automático de renovación de asignaciones del personal al inicio de curso.
 * <p>
 * Funciona de forma análoga a {@link GestorMatriculas} pero para docentes y voluntarios.
 * Se ejecuta desde {@code Launcher} al arrancar la aplicación, únicamente a partir de julio
 * (mes ≥ 7), que es cuando comienza el nuevo curso académico del personal.
 * </p>
 * <p>
 * Los docentes y voluntarios activos sin asignación para el curso nuevo se devuelven
 * al controlador (diálogo de renovación). Los que el usuario pospone se guardan en
 * {@code datos/postponed_asignaciones.properties} con claves {@code d_<id>} (docentes)
 * y {@code v_<id>} (voluntarios).
 * </p>
 */
public class GestorAsignaciones {

    private static final String RUTA_POSTPONED = "datos/postponed_asignaciones.properties";

    /**
     * DTO con las listas de docentes y voluntarios que necesitan asignación para el curso nuevo.
     */
    public static class DatosPersonal {
        public final List<Docente> docentes;
        public final List<Voluntario> voluntarios;
        public final String anyoNuevo;
        public final Properties postponed;

        DatosPersonal(List<Docente> docentes, List<Voluntario> voluntarios,
                      String anyoNuevo, Properties postponed) {
            this.docentes = docentes;
            this.voluntarios = voluntarios;
            this.anyoNuevo = anyoNuevo;
            this.postponed = postponed;
        }

        public boolean isEmpty() {
            return docentes.isEmpty() && voluntarios.isEmpty();
        }
    }

    /**
     * Punto de entrada principal. Filtra el personal activo sin asignación en el curso nuevo
     * (descartando los pospuestos hasta una fecha futura) y devuelve los pendientes,
     * o {@code null} si no estamos en periodo de generación o no hay nadie pendiente.
     *
     * @return datos para el diálogo de renovación, o {@code null} si no hay nada que hacer
     */
    public static DatosPersonal prepararDatos() {
        if (!esPeriodoGeneracion()) return null;

        String anyoNuevo = calcularNuevoCursoAcademico();
        DocenteDAO docenteDAO = new DocenteDAO();
        VoluntarioDAO voluntarioDAO = new VoluntarioDAO();

        List<Docente> docentes = docenteDAO.obtenerTodos();
        List<Voluntario> voluntarios = voluntarioDAO.obtenerTodos();

        Properties postponed = cargarPostponed();
        LocalDate hoy = LocalDate.now();

        List<Docente> docentesPendientes = docentes.stream()
                .filter(d -> Boolean.TRUE.equals(d.getActivo()))
                .filter(d -> !yaExisteAsignacion(d.getHistorialAsignaciones(), anyoNuevo))
                .filter(d -> {
                    String key = "d_" + d.getId();
                    String fechaStr = postponed.getProperty(key);
                    if (fechaStr == null) return true;
                    LocalDate fecha = LocalDate.parse(fechaStr);
                    if (hoy.isBefore(fecha)) return false;
                    postponed.remove(key);
                    guardarPostponed(postponed);
                    return true;
                })
                .collect(Collectors.toList());

        List<Voluntario> voluntariosPendientes = voluntarios.stream()
                .filter(v -> Boolean.TRUE.equals(v.getActivo()))
                .filter(v -> !yaExisteAsignacion(v.getHistorialAsignaciones(), anyoNuevo))
                .filter(v -> {
                    String key = "v_" + v.getId();
                    String fechaStr = postponed.getProperty(key);
                    if (fechaStr == null) return true;
                    LocalDate fecha = LocalDate.parse(fechaStr);
                    if (hoy.isBefore(fecha)) return false;
                    postponed.remove(key);
                    guardarPostponed(postponed);
                    return true;
                })
                .collect(Collectors.toList());

        if (docentesPendientes.isEmpty() && voluntariosPendientes.isEmpty()) return null;

        return new DatosPersonal(docentesPendientes, voluntariosPendientes, anyoNuevo, postponed);
    }

    public static boolean esPeriodoGeneracion() {
        int mes = LocalDate.now().getMonthValue();
        return mes >= 7;
    }

    public static String calcularNuevoCursoAcademico() {
        int anyo = LocalDate.now().getYear();
        return anyo + "/" + (anyo + 1);
    }

    private static boolean yaExisteAsignacion(List<AsignacionPersonal> asignaciones, String anyoNuevo) {
        if (asignaciones == null) return false;
        String alt = anyoNuevo.replace("/", "-");
        return asignaciones.stream()
                .anyMatch(a -> anyoNuevo.equals(a.getAnyoAcademico()) || alt.equals(a.getAnyoAcademico()));
    }

    public static Properties cargarPostponed() {
        Properties props = new Properties();
        File f = new File(RUTA_POSTPONED);
        if (f.exists()) {
            try (InputStream is = new FileInputStream(f)) {
                props.load(is);
            } catch (IOException e) {
                throw new ConfiguracionException("No se pudo leer el fichero de asignaciones aplazadas: " + e.getMessage(), e);
            }
        }
        return props;
    }

    public static void guardarPostponed(Properties props) {
        try (OutputStream os = new FileOutputStream(RUTA_POSTPONED)) {
            props.store(os, null);
        } catch (IOException e) {
            throw new ConfiguracionException("No se pudo guardar el fichero de asignaciones aplazadas: " + e.getMessage(), e);
        }
    }
}
