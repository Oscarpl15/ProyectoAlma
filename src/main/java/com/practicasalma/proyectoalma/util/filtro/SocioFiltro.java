package com.practicasalma.proyectoalma.util.filtro;

import com.practicasalma.proyectoalma.model.Socio;

import java.util.function.Predicate;

public class SocioFiltro {

    public static Predicate<Socio> construir(String texto, String tipoEntidad,
                                              String periodicidad, String estado) {
        return socio -> {

            if (texto != null && !texto.isBlank()) {
                String filtro = texto.toLowerCase();
                boolean coincide =
                        (socio.getNombre() != null && socio.getNombre().toLowerCase().contains(filtro)) ||
                        (socio.getApellidos() != null && socio.getApellidos().toLowerCase().contains(filtro));
                if (!coincide) return false;
            }

            if (tipoEntidad != null && !"Todos".equals(tipoEntidad)) {
                if (!tipoEntidad.equalsIgnoreCase(socio.getTipoEntidad())) return false;
            }

            if (periodicidad != null && !"Todos".equals(periodicidad)) {
                if (!periodicidad.equalsIgnoreCase(socio.getPeriodicidad())) return false;
            }

            if (estado != null && !"Todos".equals(estado)) {
                boolean activo = Boolean.TRUE.equals(socio.getActivo());
                if ("Activo".equals(estado) && !activo) return false;
                if ("Inactivo".equals(estado) && activo) return false;
            }

            return true;
        };
    }
}
