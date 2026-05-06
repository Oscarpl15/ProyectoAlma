package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AsignacionPersonalDAO;
import com.practicasalma.proyectoalma.dao.DocenteDAO;
import com.practicasalma.proyectoalma.exception.EntidadDuplicadaException;
import com.practicasalma.proyectoalma.exception.ValidacionException;
import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.util.UtilFecha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DocenteServiceTest {

    private DocenteService service;
    private DocenteDAO mockDAO;
    private AsignacionPersonalDAO mockAsignacionDAO;

    @BeforeEach
    void setUp() throws Exception {
        service = new DocenteService();
        mockDAO = Mockito.mock(DocenteDAO.class);
        mockAsignacionDAO = Mockito.mock(AsignacionPersonalDAO.class);
        Field campoDAO = DocenteService.class.getDeclaredField("docenteDAO");
        campoDAO.setAccessible(true);
        campoDAO.set(service, mockDAO);
        Field campoAsig = DocenteService.class.getDeclaredField("asignacionDAO");
        campoAsig.setAccessible(true);
        campoAsig.set(service, mockAsignacionDAO);
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

    // ---- darDeBaja ----

    @Test
    void darDeBaja_enPeriodo_eliminaAsignacionDelCursoActual() {
        Docente docente = docenteConAsignacion("2025/2026");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(docente);
        LocalDate fechaMock = LocalDate.of(2025, 8, 15);

        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);

            service.darDeBaja(1L);

            assertTrue(docente.getHistorialAsignaciones().isEmpty());
            assertFalse(docente.getActivo());
            verify(mockDAO).actualizar(docente);
            verify(mockDAO, never()).actualizarActivo(anyLong(), anyBoolean());
        }
    }

    @Test
    void darDeBaja_enPeriodo_noEliminaAsignacionDeAnioAnterior() {
        Docente docente = docenteConAsignacion("2024/2025");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(docente);
        LocalDate fechaMock = LocalDate.of(2025, 8, 15);

        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);

            service.darDeBaja(1L);

            assertEquals(1, docente.getHistorialAsignaciones().size());
            assertEquals("2024/2025", docente.getHistorialAsignaciones().get(0).getAnyoAcademico());
        }
    }

    @Test
    void darDeBaja_fueraPeriodo_usaActualizarActivoLigero() {
        LocalDate fechaMock = LocalDate.of(2025, 11, 15);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);

            service.darDeBaja(1L);

            verify(mockDAO).actualizarActivo(1L, false);
            verify(mockDAO, never()).obtenerCompleto(anyLong());
            verify(mockDAO, never()).actualizar(any());
        }
    }

    @Test
    void darDeBaja_enJunio_usaActualizarActivoLigero() {
        LocalDate fechaMock = LocalDate.of(2025, 6, 15);
        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);

            service.darDeBaja(1L);

            verify(mockDAO).actualizarActivo(1L, false);
            verify(mockDAO, never()).obtenerCompleto(anyLong());
        }
    }

    // ---- darDeAlta ----

    @Test
    void darDeAlta_sinAsignacion_creaAsignacionParaCursoActual() {
        Docente docente = docenteBase();
        when(mockDAO.obtenerCompleto(1L)).thenReturn(docente);

        try (MockedStatic<UtilFecha> uf = Mockito.mockStatic(UtilFecha.class)) {
            uf.when(UtilFecha::calcularCursoAcademicoPersonal).thenReturn("2025/2026");

            service.darDeAlta(1L);

            verify(mockAsignacionDAO).guardar(any(AsignacionPersonal.class));
        }
    }

    @Test
    void darDeAlta_yaAsignado_noCreaNuevaAsignacion() {
        Docente docente = docenteConAsignacion("2025/2026");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(docente);

        try (MockedStatic<UtilFecha> uf = Mockito.mockStatic(UtilFecha.class)) {
            uf.when(UtilFecha::calcularCursoAcademicoPersonal).thenReturn("2025/2026");

            service.darDeAlta(1L);

            verify(mockAsignacionDAO, never()).guardar(any());
        }
    }

    // ---- Auxiliares ----

    private Docente docenteBase() {
        return new Docente("Test", "Apellido", "Calle 1", null, null, null, LocalDate.of(1985, 6, 15));
    }

    private Docente docenteConAsignacion(String anyo) {
        Docente docente = docenteBase();
        AsignacionPersonal a = new AsignacionPersonal();
        a.setAnyoAcademico(anyo);
        a.setGrupoAsignado("Sin asignar");
        docente.addAsignacion(a);
        return docente;
    }
}
