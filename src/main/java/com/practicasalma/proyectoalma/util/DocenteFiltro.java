package com.practicasalma.proyectoalma.util;

import com.practicasalma.proyectoalma.model.Docente;

import java.util.function.Predicate;

public class DocenteFiltro {

    public static Predicate<Docente> construir(String texto, String cursoEscolar, String estado) {
        return docente -> {

            // Texto: nombre o apellidos
            if (texto != null && !texto.isBlank()) {
                String filtro = texto.toLowerCase();
                boolean coincide =
                        (docente.getNombre() != null && docente.getNombre().toLowerCase().contains(filtro)) ||
                        (docente.getApellidos() != null && docente.getApellidos().toLowerCase().contains(filtro));
                if (!coincide) return false;
            }

            // Curso escolar: tiene alguna asignación con ese año académico
            if (cursoEscolar != null && !"Todos".equals(cursoEscolar)) {
                String altFormato = cursoEscolar.replace("/", "-");
                if (docente.getHistorialAsignaciones() == null || docente.getHistorialAsignaciones().isEmpty())
                    return false;
                boolean tieneAsignacion = docente.getHistorialAsignaciones().stream().anyMatch(a ->
                        cursoEscolar.equals(a.getAnyoAcademico()) || altFormato.equals(a.getAnyoAcademico()));
                if (!tieneAsignacion) return false;
            }

            // Estado: Activo / Baja
            if (estado != null && !"Todos".equals(estado)) {
                boolean activo = Boolean.TRUE.equals(docente.getActivo());
                if ("Activo".equals(estado) && !activo) return false;
                if ("Inactivo".equals(estado) && activo) return false;
            }

            return true;
        };
    }
}