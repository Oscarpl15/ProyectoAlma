package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.DocenteDAO;
import com.practicasalma.proyectoalma.dao.VoluntarioDAO;
import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.model.Voluntario;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class GestorAsignaciones {

    private static final String RUTA_POSTPONED = "datos/postponed_asignaciones.properties";

    // DTO con los datos necesarios para mostrar el diálogo de renovación
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

    // Prepara los datos para el diálogo. Devuelve null si no es período o no hay pendientes.
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
        return LocalDate.now().getMonthValue() >= 4;
    } // Aquí iría 7

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
                System.err.println("No se pudo cargar postponed_asignaciones.properties: " + e.getMessage());
            }
        }
        return props;
    }

    public static void guardarPostponed(Properties props) {
        try (OutputStream os = new FileOutputStream(RUTA_POSTPONED)) {
            props.store(os, null);
        } catch (IOException e) {
            System.err.println("No se pudo guardar postponed_asignaciones.properties: " + e.getMessage());
        }
    }
}
