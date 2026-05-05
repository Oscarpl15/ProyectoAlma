package com.practicasalma.proyectoalma.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UtilFechaTest {

    // -----------------------------------------------------------------------
    // calcularCursoAcademico — usa GestorConfig (por defecto: julio = 7)
    // -----------------------------------------------------------------------

    @Test
    void calcularCursoAcademico_formatoEsCorrecto() {
        String resultado = UtilFecha.calcularCursoAcademico();
        assertTrue(resultado.matches("^\\d{4}/\\d{4}$"), "El formato debe ser YYYY/YYYY");
    }

    @Test
    void calcularCursoAcademico_segundoAnoEsPrimeroMasUno() {
        String resultado = UtilFecha.calcularCursoAcademico();
        String[] partes = resultado.split("/");
        int anyoInicio = Integer.parseInt(partes[0]);
        int anyoFin = Integer.parseInt(partes[1]);
        assertEquals(anyoInicio + 1, anyoFin);
    }

    @Test
    void calcularCursoAcademico_resultadoCoherenteConFechaActual() {
        LocalDate hoy = LocalDate.now();
        String resultado = UtilFecha.calcularCursoAcademico();
        int anyoInicio = Integer.parseInt(resultado.split("/")[0]);

        // El corte por defecto es el 1 de julio (mes 7)
        if (hoy.getMonthValue() >= 7) {
            assertEquals(hoy.getYear(), anyoInicio);
        } else {
            assertEquals(hoy.getYear() - 1, anyoInicio);
        }
    }

    // -----------------------------------------------------------------------
    // calcularCursoAcademicoPersonal — puro, corte en julio
    // -----------------------------------------------------------------------

    @Test
    void calcularCursoAcademicoPersonal_formatoEsCorrecto() {
        String resultado = UtilFecha.calcularCursoAcademicoPersonal();
        assertTrue(resultado.matches("^\\d{4}/\\d{4}$"), "El formato debe ser YYYY/YYYY");
    }

    @Test
    void calcularCursoAcademicoPersonal_segundoAnoEsPrimeroMasUno() {
        String resultado = UtilFecha.calcularCursoAcademicoPersonal();
        String[] partes = resultado.split("/");
        int anyoInicio = Integer.parseInt(partes[0]);
        int anyoFin = Integer.parseInt(partes[1]);
        assertEquals(anyoInicio + 1, anyoFin);
    }

    @Test
    void calcularCursoAcademicoPersonal_resultadoCoherenteConFechaActual() {
        LocalDate hoy = LocalDate.now();
        String resultado = UtilFecha.calcularCursoAcademicoPersonal();
        int anyoInicio = Integer.parseInt(resultado.split("/")[0]);

        if (hoy.getMonthValue() >= 7) {
            assertEquals(hoy.getYear(), anyoInicio);
        } else {
            assertEquals(hoy.getYear() - 1, anyoInicio);
        }
    }
}
