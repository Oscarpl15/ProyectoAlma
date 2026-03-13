package com.practicasalma.proyectoalma.util;

import java.time.LocalDate;

public class UtilFecha {

    public static String calcularCursoAcademico() {
        LocalDate hoy = LocalDate.now();
        int anyoActual = hoy.getYear();
        int mesActual = hoy.getMonthValue();

        if (mesActual >= 6) {
            return anyoActual + "-" + (anyoActual + 1);
        }

        return (anyoActual - 1) + "-" + anyoActual;
    }
}
