package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.VoluntarioDAO;
import com.practicasalma.proyectoalma.exception.EntidadDuplicadaException;
import com.practicasalma.proyectoalma.exception.ValidacionException;
import com.practicasalma.proyectoalma.model.Voluntario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VoluntarioServiceTest {

    private VoluntarioService service;
    private VoluntarioDAO mockDAO;

    @BeforeEach
    void setUp() throws Exception {
        service = new VoluntarioService();
        mockDAO = Mockito.mock(VoluntarioDAO.class);
        Field campo = VoluntarioService.class.getDeclaredField("voluntarioDAO");
        campo.setAccessible(true);
        campo.set(service, mockDAO);
    }

    // ---- Validación: fecha de nacimiento ----

    @Test
    void guardarVoluntario_fechaNacimientoFuturaLanzaValidacionException() {
        Voluntario voluntario = voluntarioBase();
        voluntario.setFechaNacimiento(LocalDate.now().plusDays(1));

        assertThrows(ValidacionException.class, () -> service.guardarVoluntario(voluntario));
    }

    // ---- Validación: DNI ----

    @Test
    void guardarVoluntario_dniInvalidoLanzaValidacionException() {
        Voluntario voluntario = voluntarioBase();
        voluntario.setDocumentoIdentidad("00000000A");
        voluntario.setTipoDocumento("DNI");

        assertThrows(ValidacionException.class, () -> service.guardarVoluntario(voluntario));
    }

    @Test
    void guardarVoluntario_nieInvalidoLanzaValidacionException() {
        Voluntario voluntario = voluntarioBase();
        voluntario.setDocumentoIdentidad("X0000000A");
        voluntario.setTipoDocumento("NIE");

        assertThrows(ValidacionException.class, () -> service.guardarVoluntario(voluntario));
    }

    // ---- Validación: teléfono ----

    @Test
    void guardarVoluntario_telefonoInvalidoLanzaValidacionException() {
        Voluntario voluntario = voluntarioBase();
        voluntario.setTelefono("123");

        assertThrows(ValidacionException.class, () -> service.guardarVoluntario(voluntario));
    }

    // ---- Validación: correo ----

    @Test
    void guardarVoluntario_correoInvalidoLanzaValidacionException() {
        Voluntario voluntario = voluntarioBase();
        voluntario.setCorreo("correo-sin-arroba");

        assertThrows(ValidacionException.class, () -> service.guardarVoluntario(voluntario));
    }

    // ---- Validación: duplicado ----

    @Test
    void guardarVoluntario_dniDuplicadoLanzaEntidadDuplicadaException() {
        Voluntario voluntario = voluntarioBase();
        voluntario.setDocumentoIdentidad("12345678Z");
        voluntario.setTipoDocumento("DNI");

        when(mockDAO.existeDni("12345678Z")).thenReturn(true);

        assertThrows(EntidadDuplicadaException.class, () -> service.guardarVoluntario(voluntario));
    }

    // ---- Flujo válido ----

    @Test
    void guardarVoluntario_datosValidosLlamaAGuardar() {
        Voluntario voluntario = voluntarioBase();
        voluntario.setDocumentoIdentidad("12345678Z");
        voluntario.setTipoDocumento("DNI");

        when(mockDAO.existeDni("12345678Z")).thenReturn(false);

        service.guardarVoluntario(voluntario);

        verify(mockDAO, times(1)).guardar(voluntario);
    }

    @Test
    void guardarVoluntario_sinDniNoBuscaDuplicado() {
        Voluntario voluntario = voluntarioBase();

        service.guardarVoluntario(voluntario);

        verify(mockDAO, never()).existeDni(anyString());
        verify(mockDAO, times(1)).guardar(voluntario);
    }

    // ---- Actualizar ----

    @Test
    void actualizarVoluntario_dniDuplicadoParaOtroLanzaEntidadDuplicadaException() {
        Voluntario voluntario = voluntarioBase();
        voluntario.setDocumentoIdentidad("12345678Z");
        voluntario.setTipoDocumento("DNI");

        when(mockDAO.existeDniParaOtro(eq("12345678Z"), any())).thenReturn(true);

        assertThrows(EntidadDuplicadaException.class, () -> service.actualizarVoluntario(voluntario));
    }

    @Test
    void actualizarVoluntario_dniNoTomadoLlamaAActualizar() {
        Voluntario voluntario = voluntarioBase();
        voluntario.setDocumentoIdentidad("12345678Z");
        voluntario.setTipoDocumento("DNI");

        when(mockDAO.existeDniParaOtro(eq("12345678Z"), any())).thenReturn(false);

        service.actualizarVoluntario(voluntario);

        verify(mockDAO, times(1)).actualizar(voluntario);
    }

    // ---- Auxiliar ----

    private Voluntario voluntarioBase() {
        return new Voluntario("Test", "Apellido", "Calle 1", null, null, null, LocalDate.of(1990, 3, 20));
    }
}
