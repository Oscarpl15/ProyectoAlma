package com.practicasalma.proyectoalma.util;

import java.time.LocalDate;

public class UtilFecha {

    public static String calcularCursoAcademico() {
        LocalDate hoy = LocalDate.now();
        int anyoActual = hoy.getYear();
        int mesActual = hoy.getMonthValue();

        if (mesActual >= 6) {
            return anyoActual + "/" + (anyoActual + 1);
        }

        return (anyoActual - 1) + "/" + anyoActual;
    }

    // Para docentes/voluntarios: el nuevo año empieza en julio
    public static String calcularCursoAcademicoPersonal() {
        LocalDate hoy = LocalDate.now();
        int anyoActual = hoy.getYear();
        int mesActual = hoy.getMonthValue();

        if (mesActual >= 7) {
            return anyoActual + "/" + (anyoActual + 1);
        }

        return (anyoActual - 1) + "/" + anyoActual;
    }
}
