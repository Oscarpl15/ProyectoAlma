package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GestorCertificadosAnualesTest {

    // ---- filtrarConDonacionesEnAno ----

    @Test
    void filtrar_socioConDonacionEnAno_incluido() {
        Socio socio = socioConDonacion(LocalDate.of(2024, 6, 1), BigDecimal.valueOf(100));
        List<Socio> resultado = GestorCertificadosAnuales.filtrarConDonacionesEnAno(List.of(socio), 2024);
        assertEquals(1, resultado.size());
    }

    @Test
    void filtrar_socioSinDonacionesEnAno_excluido() {
        Socio socio = socioConDonacion(LocalDate.of(2023, 6, 1), BigDecimal.valueOf(100));
        List<Socio> resultado = GestorCertificadosAnuales.filtrarConDonacionesEnAno(List.of(socio), 2024);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void filtrar_socioNuloEnLista_ignoradoSinExcepcion() {
        List<Socio> resultado = GestorCertificadosAnuales.filtrarConDonacionesEnAno(List.of(), 2024);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void filtrar_variosAnios_soloDevolvelosDelAnioIndicado() {
        Socio conDonacion = socioConDonacion(LocalDate.of(2024, 3, 1), BigDecimal.valueOf(50));
        Socio sinDonacion = socioConDonacion(LocalDate.of(2023, 3, 1), BigDecimal.valueOf(50));
        List<Socio> resultado = GestorCertificadosAnuales.filtrarConDonacionesEnAno(
                List.of(conDonacion, sinDonacion), 2024);
        assertEquals(1, resultado.size());
        assertSame(conDonacion, resultado.get(0));
    }

    // ---- construirLineasConfirmacion ----

    @Test
    void construirLineas_calculaTotalCorrectamente() {
        Socio socio = socioConDonacion(LocalDate.of(2024, 1, 1), BigDecimal.valueOf(100));
        socio.addDonacion(new Donacion(LocalDate.of(2024, 4, 1), BigDecimal.valueOf(50), "Trimestral", socio));
        List<String> lineas = GestorCertificadosAnuales.construirLineasConfirmacion(List.of(socio), 2024);
        assertEquals(1, lineas.size());
        assertTrue(lineas.get(0).contains("150.00 €"));
    }

    @Test
    void construirLineas_socioSinDonacionesEnAnio_muestra0Euros() {
        Socio socio = socioConDonacion(LocalDate.of(2023, 1, 1), BigDecimal.valueOf(100));
        List<String> lineas = GestorCertificadosAnuales.construirLineasConfirmacion(List.of(socio), 2024);
        assertTrue(lineas.get(0).contains("0.00 €"));
    }

    @Test
    void construirLineas_nombreYApellidosNulos_noLanzaExcepcion() {
        Socio socio = new Socio();
        assertDoesNotThrow(
                () -> GestorCertificadosAnuales.construirLineasConfirmacion(List.of(socio), 2024));
    }

    // ---- esPeriodoEnvio ----

    @Test
    void esPeriodoEnvio_despuesDiaConfiguracion_devuelveTrue() {
        LocalDate fechaMock = LocalDate.of(2025, 1, 25);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getCertificadosMesEnvio).thenReturn(1);
            gc.when(GestorConfig::getCertificadosDiaEnvio).thenReturn(20);
            assertTrue(GestorCertificadosAnuales.esPeriodoEnvio());
        }
    }

    @Test
    void esPeriodoEnvio_antesdeDiaConfiguracion_devuelveFalse() {
        LocalDate fechaMock = LocalDate.of(2025, 1, 10);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getCertificadosMesEnvio).thenReturn(1);
            gc.when(GestorConfig::getCertificadosDiaEnvio).thenReturn(20);
            assertFalse(GestorCertificadosAnuales.esPeriodoEnvio());
        }
    }

    @Test
    void esPeriodoEnvio_mesPosterior_devuelveTrue() {
        LocalDate fechaMock = LocalDate.of(2025, 3, 1);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getCertificadosMesEnvio).thenReturn(1);
            gc.when(GestorConfig::getCertificadosDiaEnvio).thenReturn(20);
            assertTrue(GestorCertificadosAnuales.esPeriodoEnvio());
        }
    }

    // ---- yaEnviadoEsteAno ----

    @Test
    void yaEnviadoEsteAno_mismoAnio_devuelveTrue() {
        LocalDate fechaMock = LocalDate.of(2025, 3, 1);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getCertificadosUltimoAno).thenReturn("2025");
            assertTrue(GestorCertificadosAnuales.yaEnviadoEsteAno());
        }
    }

    @Test
    void yaEnviadoEsteAno_diferenteAnio_devuelveFalse() {
        LocalDate fechaMock = LocalDate.of(2025, 3, 1);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getCertificadosUltimoAno).thenReturn("2024");
            assertFalse(GestorCertificadosAnuales.yaEnviadoEsteAno());
        }
    }

    // ---- Auxiliar ----

    private Socio socioConDonacion(LocalDate fecha, BigDecimal importe) {
        Socio socio = new Socio("Test", "Apellido", "Calle", null, "Física");
        Donacion d = new Donacion(fecha, importe, "Puntual", socio);
        socio.addDonacion(d);
        return socio;
    }
}
