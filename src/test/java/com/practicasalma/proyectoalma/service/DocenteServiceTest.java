package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.DocenteDAO;
import com.practicasalma.proyectoalma.exception.EntidadDuplicadaException;
import com.practicasalma.proyectoalma.exception.ValidacionException;
import com.practicasalma.proyectoalma.model.Docente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DocenteServiceTest {

    private DocenteService service;
    private DocenteDAO mockDAO;

    @BeforeEach
    void setUp() throws Exception {
        service = new DocenteService();
        mockDAO = Mockito.mock(DocenteDAO.class);
        Field campo = DocenteService.class.getDeclaredField("docenteDAO");
        campo.setAccessible(true);
        campo.set(service, mockDAO);
    }

    // ---- Validación: fecha de nacimiento ----

    @Test
    void guardarDocente_fechaNacimientoFuturaLanzaValidacionException() {
        Docente docente = docenteBase();
        docente.setFechaNacimiento(LocalDate.now().plusDays(1));

        assertThrows(ValidacionException.class, () -> service.guardarDocente(docente));
    }

    // ---- Validación: DNI ----

    @Test
    void guardarDocente_dniInvalidoLanzaValidacionException() {
        Docente docente = docenteBase();
        docente.setDocumentoIdentidad("00000000A");
        docente.setTipoDocumento("DNI");

        assertThrows(ValidacionException.class, () -> service.guardarDocente(docente));
    }

    @Test
    void guardarDocente_nieInvalidoLanzaValidacionException() {
        Docente docente = docenteBase();
        docente.setDocumentoIdentidad("X0000000A");
        docente.setTipoDocumento("NIE");

        assertThrows(ValidacionException.class, () -> service.guardarDocente(docente));
    }

    // ---- Validación: teléfono ----

    @Test
    void guardarDocente_telefonoInvalidoLanzaValidacionException() {
        Docente docente = docenteBase();
        docente.setTelefono("123");

        assertThrows(ValidacionException.class, () -> service.guardarDocente(docente));
    }

    // ---- Validación: correo ----

    @Test
    void guardarDocente_correoInvalidoLanzaValidacionException() {
        Docente docente = docenteBase();
        docente.setCorreo("correo-sin-arroba");

        assertThrows(ValidacionException.class, () -> service.guardarDocente(docente));
    }

    // ---- Validación: duplicado ----

    @Test
    void guardarDocente_dniDuplicadoLanzaEntidadDuplicadaException() {
        Docente docente = docenteBase();
        docente.setDocumentoIdentidad("12345678Z");
        docente.setTipoDocumento("DNI");

        when(mockDAO.existeDni("12345678Z")).thenReturn(true);

        assertThrows(EntidadDuplicadaException.class, () -> service.guardarDocente(docente));
    }

    // ---- Flujo válido ----

    @Test
    void guardarDocente_datosValidosLlamaAGuardar() {
        Docente docente = docenteBase();
        docente.setDocumentoIdentidad("12345678Z");
        docente.setTipoDocumento("DNI");

        when(mockDAO.existeDni("12345678Z")).thenReturn(false);

        service.guardarDocente(docente);

        verify(mockDAO, times(1)).guardar(docente);
    }

    @Test
    void guardarDocente_sinDniNoBuscaDuplicado() {
        Docente docente = docenteBase();

        service.guardarDocente(docente);

        verify(mockDAO, never()).existeDni(anyString());
        verify(mockDAO, times(1)).guardar(docente);
    }

    // ---- Actualizar ----

    @Test
    void actualizarDocente_dniDuplicadoParaOtroLanzaEntidadDuplicadaException() {
        Docente docente = docenteBase();
        docente.setDocumentoIdentidad("12345678Z");
        docente.setTipoDocumento("DNI");

        when(mockDAO.existeDniParaOtro(eq("12345678Z"), any())).thenReturn(true);

        assertThrows(EntidadDuplicadaException.class, () -> service.actualizarDocente(docente));
    }

    @Test
    void actualizarDocente_dniNoTomadoLlamaAActualizar() {
        Docente docente = docenteBase();
        docente.setDocumentoIdentidad("12345678Z");
        docente.setTipoDocumento("DNI");

        when(mockDAO.existeDniParaOtro(eq("12345678Z"), any())).thenReturn(false);

        service.actualizarDocente(docente);

        verify(mockDAO, times(1)).actualizar(docente);
    }

    // ---- Auxiliar ----

    private Docente docenteBase() {
        return new Docente("Test", "Apellido", "Calle 1", null, null, null, LocalDate.of(1985, 6, 15));
    }
}
