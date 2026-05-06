package com.practicasalma.proyectoalma.service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GestorAsignacionesTest {

    // ---- esPeriodoGeneracion ----

    @Test
    void esPeriodoGeneracion_enJulio_devuelveTrue() {
        LocalDate fechaMock = LocalDate.of(2025, 7, 1);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            assertTrue(GestorAsignaciones.esPeriodoGeneracion());
        }
    }

    @Test
    void esPeriodoGeneracion_enDiciembre_devuelveTrue() {
        LocalDate fechaMock = LocalDate.of(2025, 12, 15);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            assertTrue(GestorAsignaciones.esPeriodoGeneracion());
        }
    }

    @Test
    void esPeriodoGeneracion_enJunio_devuelveFalse() {
        LocalDate fechaMock = LocalDate.of(2025, 6, 30);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            assertFalse(GestorAsignaciones.esPeriodoGeneracion());
        }
    }

    @Test
    void esPeriodoGeneracion_enEnero_devuelveFalse() {
        LocalDate fechaMock = LocalDate.of(2025, 1, 15);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            assertFalse(GestorAsignaciones.esPeriodoGeneracion());
        }
    }

    // ---- calcularNuevoCursoAcademico ----

    @Test
    void calcularNuevoCursoAcademico_devuelveAnioActualMasUno() {
        LocalDate fechaMock = LocalDate.of(2025, 8, 15);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            assertEquals("2025/2026", GestorAsignaciones.calcularNuevoCursoAcademico());
        }
    }
}
