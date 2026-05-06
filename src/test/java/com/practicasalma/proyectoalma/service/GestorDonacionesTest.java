package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Socio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GestorDonacionesTest {

    private SocioService mockSocioService;
    private static final LocalDate HOY = LocalDate.of(2025, 8, 15);

    @BeforeEach
    void setUp() {
        mockSocioService = Mockito.mock(SocioService.class);
    }

    // ---- Filtros de elegibilidad ----

    @Test
    void procesarSocios_socioInactivo_noGeneraDonaciones() {
        Socio socio = socioMensual(LocalDate.of(2025, 7, 15));
        socio.setActivo(false);

        GestorDonaciones.procesarSocios(List.of(socio), HOY, mockSocioService);

        assertEquals(1, socio.getDonaciones().size()); // solo la original
        verify(mockSocioService, never()).actualizarSocio(any());
    }

    @Test
    void procesarSocios_cuotaCero_noGeneraDonaciones() {
        Socio socio = socioMensual(LocalDate.of(2025, 7, 15));
        socio.setCuota(0.0);

        GestorDonaciones.procesarSocios(List.of(socio), HOY, mockSocioService);

        assertEquals(1, socio.getDonaciones().size());
        verify(mockSocioService, never()).actualizarSocio(any());
    }

    @Test
    void procesarSocios_periodicidadPuntual_noGeneraDonaciones() {
        Socio socio = socioBase("Puntual", 50.0, LocalDate.of(2025, 7, 15));

        GestorDonaciones.procesarSocios(List.of(socio), HOY, mockSocioService);

        assertEquals(1, socio.getDonaciones().size());
        verify(mockSocioService, never()).actualizarSocio(any());
    }

    @Test
    void procesarSocios_sinDonacionesPrevias_noGeneraDonaciones() {
        Socio socio = new Socio("Test", "A", "Calle", null, "Física");
        socio.setActivo(true);
        socio.setPeriodicidad("Mensual");
        socio.setCuota(50.0);
        // sin donaciones

        GestorDonaciones.procesarSocios(List.of(socio), HOY, mockSocioService);

        assertTrue(socio.getDonaciones().isEmpty());
        verify(mockSocioService, never()).actualizarSocio(any());
    }

    @Test
    void procesarSocios_donacionReciente_noGeneraDonaciones() {
        // Última donación fue ayer → próxima es en un mes → no genera nada hoy
        Socio socio = socioMensual(HOY.minusDays(1));

        GestorDonaciones.procesarSocios(List.of(socio), HOY, mockSocioService);

        assertEquals(1, socio.getDonaciones().size());
        verify(mockSocioService, never()).actualizarSocio(any());
    }

    // ---- Generación de donaciones ----

    @Test
    void procesarSocios_unMesPendienteMensual_generaUnaDonacion() {
        Socio socio = socioMensual(LocalDate.of(2025, 7, 15));

        GestorDonaciones.procesarSocios(List.of(socio), HOY, mockSocioService);

        assertEquals(2, socio.getDonaciones().size());
        assertEquals(LocalDate.of(2025, 8, 15), ultimaDonacion(socio).getFecha());
    }

    @Test
    void procesarSocios_tresMesesPendientesMensual_generaTresDonaciones() {
        Socio socio = socioMensual(LocalDate.of(2025, 5, 15));

        GestorDonaciones.procesarSocios(List.of(socio), HOY, mockSocioService);

        assertEquals(4, socio.getDonaciones().size()); // 1 original + 3 nuevas
    }

    @Test
    void procesarSocios_trimestralPendiente_generaUnaDonacion() {
        Socio socio = socioBase("Trimestral", 150.0, LocalDate.of(2025, 5, 15));

        GestorDonaciones.procesarSocios(List.of(socio), HOY, mockSocioService);

        assertEquals(2, socio.getDonaciones().size());
        assertEquals(LocalDate.of(2025, 8, 15), ultimaDonacion(socio).getFecha());
    }

    @Test
    void procesarSocios_anualPendiente_generaUnaDonacion() {
        Socio socio = socioBase("Anual", 600.0, LocalDate.of(2024, 8, 15));

        GestorDonaciones.procesarSocios(List.of(socio), HOY, mockSocioService);

        assertEquals(2, socio.getDonaciones().size());
        assertEquals(LocalDate.of(2025, 8, 15), ultimaDonacion(socio).getFecha());
    }

    @Test
    void procesarSocios_donacionGenerada_tieneImporteYPeriodicidadCorrectos() {
        Socio socio = socioMensual(LocalDate.of(2025, 7, 15));

        GestorDonaciones.procesarSocios(List.of(socio), HOY, mockSocioService);

        Donacion nueva = ultimaDonacion(socio);
        assertEquals(BigDecimal.valueOf(50.0), nueva.getImporte());
        assertEquals("Mensual", nueva.getPeriodicidad());
    }

    // ---- Llamadas al service ----

    @Test
    void procesarSocios_conPendientes_lllamaActualizarSocio() {
        Socio socio = socioMensual(LocalDate.of(2025, 7, 15));

        GestorDonaciones.procesarSocios(List.of(socio), HOY, mockSocioService);

        verify(mockSocioService, times(1)).actualizarSocio(socio);
    }

    @Test
    void procesarSocios_sinPendientes_noLlamaActualizarSocio() {
        Socio socio = socioMensual(HOY); // última donación es hoy → próxima es en un mes

        GestorDonaciones.procesarSocios(List.of(socio), HOY, mockSocioService);

        verify(mockSocioService, never()).actualizarSocio(any());
    }

    // ---- Auxiliares ----

    private Socio socioMensual(LocalDate fechaUltimaDonacion) {
        return socioBase("Mensual", 50.0, fechaUltimaDonacion);
    }

    private Socio socioBase(String periodicidad, double cuota, LocalDate fechaDonacion) {
        Socio socio = new Socio("Test", "A", "Calle", null, "Física");
        socio.setActivo(true);
        socio.setPeriodicidad(periodicidad);
        socio.setCuota(cuota);
        Donacion d = new Donacion(fechaDonacion, BigDecimal.valueOf(cuota), periodicidad, socio);
        socio.addDonacion(d);
        return socio;
    }

    private Donacion ultimaDonacion(Socio socio) {
        List<Donacion> ds = socio.getDonaciones();
        return ds.get(ds.size() - 1);
    }
}
