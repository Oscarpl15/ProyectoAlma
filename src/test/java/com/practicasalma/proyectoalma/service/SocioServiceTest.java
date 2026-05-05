package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.SocioDAO;
import com.practicasalma.proyectoalma.exception.EntidadDuplicadaException;
import com.practicasalma.proyectoalma.exception.ValidacionException;
import com.practicasalma.proyectoalma.model.Socio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SocioServiceTest {

    private SocioService service;
    private SocioDAO mockDAO;

    @BeforeEach
    void setUp() throws Exception {
        service = new SocioService();
        mockDAO = Mockito.mock(SocioDAO.class);
        Field campo = SocioService.class.getDeclaredField("socioDAO");
        campo.setAccessible(true);
        campo.set(service, mockDAO);
    }

    // ---- Validación: DNI ----

    @Test
    void guardarSocio_dniInvalidoLanzaValidacionException() {
        Socio socio = new Socio("Test", "Apellido", "Calle 1", "00000000A", "Física");
        socio.setTipoDocumento("DNI");

        assertThrows(ValidacionException.class, () -> service.guardarSocio(socio));
    }

    @Test
    void guardarSocio_nieInvalidoLanzaValidacionException() {
        Socio socio = new Socio("Test", "Apellido", "Calle 1", "X0000000A", "Física");
        socio.setTipoDocumento("NIE");

        assertThrows(ValidacionException.class, () -> service.guardarSocio(socio));
    }

    // ---- Validación: teléfono ----

    @Test
    void guardarSocio_telefonoInvalidoLanzaValidacionException() {
        Socio socio = new Socio("Test", "Apellido", "Calle 1", null, "Física");
        socio.setTelefono("12345");

        assertThrows(ValidacionException.class, () -> service.guardarSocio(socio));
    }

    // ---- Validación: IBAN ----

    @Test
    void guardarSocio_ibanInvalidoLanzaValidacionException() {
        Socio socio = new Socio("Test", "Apellido", "Calle 1", null, "Física");
        socio.setCuentaBancaria("IBAN_INCORRECTO");

        assertThrows(ValidacionException.class, () -> service.guardarSocio(socio));
    }

    @Test
    void guardarSocio_ibanValidoNuncaLanzaExcepcion() {
        Socio socio = new Socio("Test", "Apellido", "Calle 1", null, "Física");
        socio.setCuentaBancaria("DE89370400440532013000");

        when(mockDAO.existeDni(any())).thenReturn(false);
        assertDoesNotThrow(() -> service.guardarSocio(socio));
        verify(mockDAO, times(1)).guardar(socio);
    }

    // ---- Validación: duplicado ----

    @Test
    void guardarSocio_dniDuplicadoLanzaEntidadDuplicadaException() {
        Socio socio = new Socio("Test", "Apellido", "Calle 1", "12345678Z", "Física");
        socio.setTipoDocumento("DNI");

        when(mockDAO.existeDni("12345678Z")).thenReturn(true);

        assertThrows(EntidadDuplicadaException.class, () -> service.guardarSocio(socio));
    }

    // ---- Flujo válido ----

    @Test
    void guardarSocio_datosValidosLlamaAGuardar() {
        Socio socio = new Socio("Test", "Apellido", "Calle 1", "12345678Z", "Física");
        socio.setTipoDocumento("DNI");

        when(mockDAO.existeDni("12345678Z")).thenReturn(false);

        service.guardarSocio(socio);

        verify(mockDAO, times(1)).guardar(socio);
    }

    @Test
    void guardarSocio_sinDniNoBuscaDuplicado() {
        Socio socio = new Socio("Test", "Apellido", "Calle 1", null, "Empresa");

        service.guardarSocio(socio);

        verify(mockDAO, never()).existeDni(anyString());
        verify(mockDAO, times(1)).guardar(socio);
    }

    // ---- Actualizar ----

    @Test
    void actualizarSocio_dniDuplicadoParaOtroLanzaEntidadDuplicadaException() {
        Socio socio = new Socio("Test", "Apellido", "Calle 1", "12345678Z", "Física");
        socio.setTipoDocumento("DNI");

        when(mockDAO.existeDniParaOtro(eq("12345678Z"), any())).thenReturn(true);

        assertThrows(EntidadDuplicadaException.class, () -> service.actualizarSocio(socio));
    }

    @Test
    void actualizarSocio_dniNoTomadoLlamaAActualizar() {
        Socio socio = new Socio("Test", "Apellido", "Calle 1", "12345678Z", "Física");
        socio.setTipoDocumento("DNI");

        when(mockDAO.existeDniParaOtro(eq("12345678Z"), any())).thenReturn(false);

        service.actualizarSocio(socio);

        verify(mockDAO, times(1)).actualizar(socio);
    }
}
