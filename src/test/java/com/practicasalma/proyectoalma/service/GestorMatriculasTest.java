package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GestorMatriculasTest {

    // ---- yaExisteMatricula ----

    @Test
    void yaExisteMatricula_formatoSlash_devuelveTrue() {
        Alumno alumno = alumnoConMatricula("2025/2026", "1º Primaria");
        assertTrue(GestorMatriculas.yaExisteMatricula(alumno, "2025/2026"));
    }

    @Test
    void yaExisteMatricula_formatoGuion_devuelveTrue() {
        Alumno alumno = alumnoConMatricula("2025-2026", "1º Primaria");
        assertTrue(GestorMatriculas.yaExisteMatricula(alumno, "2025/2026"));
    }

    @Test
    void yaExisteMatricula_anyoDistinto_devuelveFalse() {
        Alumno alumno = alumnoConMatricula("2024/2025", "1º Primaria");
        assertFalse(GestorMatriculas.yaExisteMatricula(alumno, "2025/2026"));
    }

    @Test
    void yaExisteMatricula_sinMatriculas_devuelveFalse() {
        Alumno alumno = new Alumno("Test", "A", "Calle", LocalDate.of(2010, 1, 1));
        assertFalse(GestorMatriculas.yaExisteMatricula(alumno, "2025/2026"));
    }

    // ---- siguienteCurso ----

    @Test
    void siguienteCurso_primerCurso_devuelveSegundo() {
        assertEquals("2º Infantil", GestorMatriculas.siguienteCurso("1º Infantil"));
    }

    @Test
    void siguienteCurso_quintoDerimaria_devuelveSexto() {
        assertEquals("6º Primaria", GestorMatriculas.siguienteCurso("5º Primaria"));
    }

    @Test
    void siguienteCurso_ultimoCurso_devuelveNull() {
        assertNull(GestorMatriculas.siguienteCurso("6º Primaria"));
    }

    @Test
    void siguienteCurso_cursoDesconocido_devuelveNull() {
        assertNull(GestorMatriculas.siguienteCurso("7º Primaria"));
    }

    // ---- cursoAnterior ----

    @Test
    void cursoAnterior_segundoCurso_devuelvePrimero() {
        assertEquals("1º Infantil", GestorMatriculas.cursoAnterior("2º Infantil"));
    }

    @Test
    void cursoAnterior_primerCurso_devuelveNull() {
        assertNull(GestorMatriculas.cursoAnterior("1º Infantil"));
    }

    @Test
    void cursoAnterior_cursoDesconocido_devuelveNull() {
        assertNull(GestorMatriculas.cursoAnterior("Bachillerato"));
    }

    // ---- esPeriodoGeneracion ----

    @Test
    void esPeriodoGeneracion_enJulio_devuelveTrue() {
        LocalDate fechaMock = LocalDate.of(2025, 7, 15);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getMatriculasMesInicio).thenReturn(6);
            gc.when(GestorConfig::getMatriculasDiaInicio).thenReturn(1);
            assertTrue(GestorMatriculas.esPeriodoGeneracion());
        }
    }

    @Test
    void esPeriodoGeneracion_enSeptiembre10_devuelveTrue() {
        LocalDate fechaMock = LocalDate.of(2025, 9, 10);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getMatriculasMesInicio).thenReturn(6);
            gc.when(GestorConfig::getMatriculasDiaInicio).thenReturn(1);
            assertTrue(GestorMatriculas.esPeriodoGeneracion());
        }
    }

    @Test
    void esPeriodoGeneracion_enSeptiembre11_devuelveFalse() {
        LocalDate fechaMock = LocalDate.of(2025, 9, 11);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getMatriculasMesInicio).thenReturn(6);
            gc.when(GestorConfig::getMatriculasDiaInicio).thenReturn(1);
            assertFalse(GestorMatriculas.esPeriodoGeneracion());
        }
    }

    @Test
    void esPeriodoGeneracion_enMayo_devuelveFalse() {
        LocalDate fechaMock = LocalDate.of(2025, 5, 20);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getMatriculasMesInicio).thenReturn(6);
            gc.when(GestorConfig::getMatriculasDiaInicio).thenReturn(1);
            assertFalse(GestorMatriculas.esPeriodoGeneracion());
        }
    }

    @Test
    void esPeriodoGeneracion_enOctubre_devuelveFalse() {
        LocalDate fechaMock = LocalDate.of(2025, 10, 1);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getMatriculasMesInicio).thenReturn(6);
            gc.when(GestorConfig::getMatriculasDiaInicio).thenReturn(1);
            assertFalse(GestorMatriculas.esPeriodoGeneracion());
        }
    }

    // ---- Auxiliar ----

    private Alumno alumnoConMatricula(String anyo, String curso) {
        Alumno alumno = new Alumno("Test", "A", "Calle", LocalDate.of(2010, 1, 1));
        Matricula m = new Matricula();
        m.setAnyoAcademico(anyo);
        m.setCurso(curso);
        alumno.addMatricula(m);
        return alumno;
    }
}
