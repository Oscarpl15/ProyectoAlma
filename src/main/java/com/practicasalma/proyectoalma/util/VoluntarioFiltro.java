package com.practicasalma.proyectoalma.util;

import com.practicasalma.proyectoalma.model.Voluntario;

import java.util.function.Predicate;

public class VoluntarioFiltro {

    public static Predicate<Voluntario> construir(String texto, String cursoEscolar, String estado) {
        return voluntario -> {

            // Texto: nombre o apellidos
            if (texto != null && !texto.isBlank()) {
                String filtro = texto.toLowerCase();
                boolean coincide =
                        (voluntario.getNombre() != null && voluntario.getNombre().toLowerCase().contains(filtro)) ||
                        (voluntario.getApellidos() != null && voluntario.getApellidos().toLowerCase().contains(filtro));
                if (!coincide) return false;
            }

            // Curso escolar: tiene alguna asignación con ese año académico
            if (cursoEscolar != null && !"Todos".equals(cursoEscolar)) {
                String altFormato = cursoEscolar.replace("/", "-");
                if (voluntario.getHistorialAsignaciones() == null || voluntario.getHistorialAsignaciones().isEmpty())
                    return false;
                boolean tieneAsignacion = voluntario.getHistorialAsignaciones().stream().anyMatch(a ->
                        cursoEscolar.equals(a.getAnyoAcademico()) || altFormato.equals(a.getAnyoAcademico()));
                if (!tieneAsignacion) return false;
            }

            // Estado: Activo / Baja
            if (estado != null && !"Todos".equals(estado)) {
                boolean activo = Boolean.TRUE.equals(voluntario.getActivo());
                if ("Activo".equals(estado) && !activo) return false;
                if ("Inactivo".equals(estado) && activo) return false;
            }

            return true;
        };
    }
}