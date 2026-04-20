package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AlumnoDAO;
import com.practicasalma.proyectoalma.dao.MatriculaDAO;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class GestorMatriculas {

    private static final List<String> CURSOS = List.of(
            "1º Infantil", "2º Infantil", "3º Infantil",
            "1º Primaria", "2º Primaria", "3º Primaria",
            "4º Primaria", "5º Primaria", "6º Primaria"
    );

    private static final String RUTA_POSTPONED = "datos/postponed_matriculas.properties";

    // Datos necesarios para que RenovacionController construya las filas del diálogo
    public static class DatosDialogo {
        public final List<Alumno> alumnosSexto;
        public final String anyoAcademico;
        public final MatriculaDAO matriculaDAO;
        public final AlumnoDAO alumnoDAO;
        public final Properties postponed;

        DatosDialogo(List<Alumno> alumnosSexto, String anyoAcademico,
                     MatriculaDAO matriculaDAO, AlumnoDAO alumnoDAO, Properties postponed) {
            this.alumnosSexto = alumnosSexto;
            this.anyoAcademico = anyoAcademico;
            this.matriculaDAO = matriculaDAO;
            this.alumnoDAO = alumnoDAO;
            this.postponed = postponed;
        }

        public boolean isEmpty() { return alumnosSexto.isEmpty(); }
    }

    // Retorna los datos para el diálogo (o null si no es período o no hay nada que mostrar).
    // Auto-genera matrículas para los alumnos que NO están en 6º.
    public static DatosDialogo ejecutar() {
        if (!esPeriodoGeneracion()) return null;

        String anyoActual = calcularAnyoAcademico();
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

        for (Alumno a : resto) {
            String cursoActual = obtenerUltimoCurso(a);
            String nuevoCurso = cursoActual == null ? CURSOS.get(0) : siguienteCurso(cursoActual);
            if (nuevoCurso == null) continue;
            try {
                Matricula m = new Matricula(nuevoCurso, a);
                m.setAnyoAcademico(anyoActual);
                matriculaDAO.guardar(m);
            } catch (Exception e) {
                System.err.println("Error al generar matrícula para " + a.getNombre() + ": " + e.getMessage());
            }
        }

        if (enSexto.isEmpty()) return null;
        return new DatosDialogo(enSexto, anyoActual, matriculaDAO, alumnoDAO, postponed);
    }

    public static boolean esPeriodoGeneracion() {
        LocalDate hoy = LocalDate.now();
        int mes = hoy.getMonthValue();
        int dia = hoy.getDayOfMonth();
        //return mes == 7 || mes == 8 || (mes == 9 && dia <= 10);
        return mes == 4;
    }

    public static String calcularAnyoAcademico() {
        LocalDate hoy = LocalDate.now();
        int anyo = hoy.getYear();
        int mes = hoy.getMonthValue();
        //if (mes >= 6) {
            return anyo + "/" + (anyo + 1);
        //}
        //return (anyo - 1) + "/" + anyo;
    }

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
        if (alumno.getMatriculas() == null || alumno.getMatriculas().isEmpty()) return null;
        return alumno.getMatriculas().get(alumno.getMatriculas().size() - 1).getCurso();
    }

    public static Properties cargarPostponed() {
        Properties props = new Properties();
        File f = new File(RUTA_POSTPONED);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                props.load(fis);
            } catch (Exception e) {
                System.err.println("Error cargando postponed: " + e.getMessage());
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
            System.err.println("Error guardando postponed: " + e.getMessage());
        }
    }
}
