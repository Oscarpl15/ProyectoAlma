package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AlumnoDAO;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.UtilFecha;
import com.practicasalma.proyectoalma.dao.MatriculaDAO;
import com.practicasalma.proyectoalma.exception.ConfiguracionException;
import com.practicasalma.proyectoalma.exception.PersistenciaException;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Gestor del proceso automático de renovación de matrículas al inicio de curso.
 * <p>
 * Se ejecuta al arrancar la aplicación (desde {@code Launcher}) únicamente durante
 * el periodo de generación: <b>julio, agosto y hasta el 10 de septiembre</b>.
 * Para cada alumno activo sin matrícula en el curso actual:
 * <ul>
 *   <li>Si no está en 6º Primaria, se le crea automáticamente la matrícula del curso siguiente.</li>
 *   <li>Si está en 6º Primaria, se devuelven esos alumnos al llamador (controlador de diálogo)
 *       para que el usuario decida si pasan o no al siguiente nivel.</li>
 * </ul>
 * Los alumnos en 6º que el usuario pospone se guardan en
 * {@code datos/postponed_matriculas.properties} para no volver a preguntar hasta la fecha indicada.
 * </p>
 */
public class GestorMatriculas {

    private static final List<String> CURSOS = List.of(
            "1º Infantil", "2º Infantil", "3º Infantil",
            "1º Primaria", "2º Primaria", "3º Primaria",
            "4º Primaria", "5º Primaria", "6º Primaria"
    );

    private static final String RUTA_POSTPONED = "datos/postponed_matriculas.properties";

    /**
     * DTO que agrupa los datos necesarios para que el controlador presente el diálogo
     * de alumnos en 6º Primaria que necesitan confirmación manual.
     */
    public static class DatosDialogo {
        public final List<Alumno> alumnosSexto;
        public final String anyoAcademico;
        public final MatriculaDAO matriculaDAO;
        public final AlumnoDAO alumnoDAO;
        public final Properties postponed;
        public final List<String> errores;

        DatosDialogo(List<Alumno> alumnosSexto, String anyoAcademico,
                     MatriculaDAO matriculaDAO, AlumnoDAO alumnoDAO, Properties postponed,
                     List<String> errores) {
            this.alumnosSexto = alumnosSexto;
            this.anyoAcademico = anyoAcademico;
            this.matriculaDAO = matriculaDAO;
            this.alumnoDAO = alumnoDAO;
            this.errores = errores;
            this.postponed = postponed;
        }

        public boolean isEmpty() { return alumnosSexto.isEmpty(); }
    }

    /**
     * Punto de entrada principal. Detecta alumnos sin matrícula en el curso actual,
     * procesa automáticamente los que no están en 6º y devuelve un {@link DatosDialogo}
     * con los alumnos de 6º que requieren confirmación manual, o {@code null} si no hay nada que hacer.
     *
     * @return datos para el diálogo de confirmación, o {@code null} si no hay nada pendiente
     */
    public static DatosDialogo ejecutar() {
        if (!esPeriodoGeneracion()) return null;

        String anyoActual = UtilFecha.calcularCursoAcademico();
        AlumnoDAO alumnoDAO = new AlumnoDAO();
        MatriculaDAO matriculaDAO = new MatriculaDAO();

        List<Alumno> alumnos = alumnoDAO.obtenerTodos();

        List<Alumno> sinMatricula = alumnos.stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .filter(a -> !yaExisteMatricula(a, anyoActual))
                .collect(Collectors.toList());

        if (sinMatricula.isEmpty()) return null;

        Properties postponed = cargarPostponed();
        LocalDate hoy = LocalDate.now();

        List<Alumno> enSexto = sinMatricula.stream()
                .filter(a -> "6º Primaria".equals(obtenerUltimoCurso(a)))
                .filter(a -> {
                    String fechaStr = postponed.getProperty(String.valueOf(a.getId()));
                    if (fechaStr == null) return true;
                    LocalDate fechaPostponer = LocalDate.parse(fechaStr);
                    if (hoy.isBefore(fechaPostponer)) return false;
                    postponed.remove(String.valueOf(a.getId()));
                    guardarPostponed(postponed);
                    return true;
                })
                .collect(Collectors.toList());

        List<Alumno> resto = sinMatricula.stream()
                .filter(a -> !"6º Primaria".equals(obtenerUltimoCurso(a)))
                .collect(Collectors.toList());

        List<String> errores = new ArrayList<>();
        for (Alumno a : resto) {
            String cursoActual = obtenerUltimoCurso(a);
            String nuevoCurso = cursoActual == null ? CURSOS.get(0) : siguienteCurso(cursoActual);
            if (nuevoCurso == null) continue;
            try {
                Matricula m = new Matricula(nuevoCurso, a);
                m.setAnyoAcademico(anyoActual);
                matriculaDAO.guardar(m);
            } catch (PersistenciaException e) {
                errores.add((a.getNombre() != null ? a.getNombre() : "") + " " +
                        (a.getApellidos() != null ? a.getApellidos() : ""));
            }
        }

        if (enSexto.isEmpty() && errores.isEmpty()) return null;
        return new DatosDialogo(enSexto, anyoActual, matriculaDAO, alumnoDAO, postponed, errores);
    }

    /**
     * Indica si la fecha actual está dentro del periodo de generación de matrículas
     * (julio, agosto y hasta el 10 de septiembre inclusive).
     *
     * @return {@code true} si estamos en periodo de generación
     */
    public static boolean esPeriodoGeneracion() {
        LocalDate hoy = LocalDate.now();
        int mesInicio = GestorConfig.getMatriculasMesInicio();
        int diaInicio = GestorConfig.getMatriculasDiaInicio();
        LocalDate inicio = LocalDate.of(hoy.getYear(), mesInicio, diaInicio);
        LocalDate fin = LocalDate.of(hoy.getYear(), 9, 10);
        return !hoy.isBefore(inicio) && !hoy.isAfter(fin);
    }

    /**
     * Comprueba si un alumno ya tiene matrícula para el año académico dado.
     * Acepta tanto el formato {@code "AAAA/AAAA+1"} como el alternativo {@code "AAAA-AAAA+1"}.
     *
     * @param alumno        alumno a comprobar
     * @param anyoAcademico año académico buscado
     * @return {@code true} si ya existe una matrícula para ese año
     */
    public static boolean yaExisteMatricula(Alumno alumno, String anyoAcademico) {
        if (alumno.getMatriculas() == null) return false;
        String alternativo = anyoAcademico.replace("/", "-");
        return alumno.getMatriculas().stream().anyMatch(m ->
                anyoAcademico.equals(m.getAnyoAcademico()) ||
                alternativo.equals(m.getAnyoAcademico())
        );
    }

    public static String siguienteCurso(String curso) {
        int idx = CURSOS.indexOf(curso);
        if (idx < 0 || idx >= CURSOS.size() - 1) return null;
        return CURSOS.get(idx + 1);
    }

    public static String cursoAnterior(String curso) {
        int idx = CURSOS.indexOf(curso);
        if (idx <= 0) return null;
        return CURSOS.get(idx - 1);
    }

    private static String obtenerUltimoCurso(Alumno alumno) {
        Matricula ultima = alumno.getUltimaMatricula();
        return ultima != null ? ultima.getCurso() : null;
    }

    public static Properties cargarPostponed() {
        Properties props = new Properties();
        File f = new File(RUTA_POSTPONED);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                props.load(fis);
            } catch (Exception e) {
                throw new ConfiguracionException("No se pudo leer el fichero de renovaciones aplazadas: " + e.getMessage(), e);
            }
        }
        return props;
    }

    public static void guardarPostponed(Properties props) {
        try {
            new File("datos").mkdirs();
            try (FileOutputStream fos = new FileOutputStream(RUTA_POSTPONED)) {
                props.store(fos, null);
            }
        } catch (Exception e) {
            throw new ConfiguracionException("No se pudo guardar el fichero de renovaciones aplazadas: " + e.getMessage(), e);
        }
    }
}
