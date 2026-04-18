package com.practicasalma.proyectoalma.util;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;

import java.util.function.Predicate;

public class AlumnoFiltro {

    public static Predicate<Alumno> construir(String texto, String cursoEscolar,
                                               String cursoAlumno, String grupo,
                                               boolean seguimientoSS, boolean seguimientoSAF,
                                               String estado) {
        return alumno -> {

            // Texto: nombre o apellidos
            if (texto != null && !texto.isBlank()) {
                String filtro = texto.toLowerCase();
                boolean coincide =
                        (alumno.getNombre() != null && alumno.getNombre().toLowerCase().contains(filtro)) ||
                        (alumno.getApellidos() != null && alumno.getApellidos().toLowerCase().contains(filtro));
                if (!coincide) return false;
            }

            // Curso escolar: tiene alguna matrícula con ese año académico
            if (cursoEscolar != null && !"Todos".equals(cursoEscolar)) {
                String altFormato = cursoEscolar.replace("/", "-");
                if (alumno.getMatriculas() == null) return false;
                boolean tieneMatricula = alumno.getMatriculas().stream().anyMatch(m ->
                        cursoEscolar.equals(m.getAnyoAcademico()) || altFormato.equals(m.getAnyoAcademico()));
                if (!tieneMatricula) return false;
            }

            // Curso del alumno: curso de su última matrícula
            if (cursoAlumno != null && !"Todos".equals(cursoAlumno)) {
                Matricula ultima = ultimaMatricula(alumno);
                if (ultima == null || !cursoAlumno.equals(ultima.getCurso())) return false;
            }

            // Grupo: grupo de su última matrícula
            if (grupo != null && !"Todos".equals(grupo)) {
                Matricula ultima = ultimaMatricula(alumno);
                if (ultima == null || !grupo.equals(ultima.getGrupoAsignado())) return false;
            }

            // Seguimiento SS
            if (seguimientoSS && !Boolean.TRUE.equals(alumno.getSeguimientoServiciosSociales())) {
                return false;
            }

            // Seguimiento SAF
            if (seguimientoSAF && !Boolean.TRUE.equals(alumno.getSeguimientoSaf())) {
                return false;
            }

            // Estado: Activo / Baja
            if (estado != null && !"Todos".equals(estado)) {
                boolean activo = Boolean.TRUE.equals(alumno.getActivo());
                if ("Activo".equals(estado) && !activo) return false;
                if ("Inactivo".equals(estado) && activo) return false;
            }

            return true;
        };
    }

    private static Matricula ultimaMatricula(Alumno alumno) {
        if (alumno.getMatriculas() == null || alumno.getMatriculas().isEmpty()) return null;
        return alumno.getMatriculas().get(alumno.getMatriculas().size() - 1);
    }
}