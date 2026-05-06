package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AsignacionPersonalDAO;
import com.practicasalma.proyectoalma.dao.VoluntarioDAO;
import com.practicasalma.proyectoalma.exception.EntidadDuplicadaException;
import com.practicasalma.proyectoalma.exception.ValidacionException;
import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Voluntario;
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

class VoluntarioServiceTest {

    private VoluntarioService service;
    private VoluntarioDAO mockDAO;
    private AsignacionPersonalDAO mockAsignacionDAO;

    @BeforeEach
    void setUp() throws Exception {
        service = new VoluntarioService();
        mockDAO = Mockito.mock(VoluntarioDAO.class);
        mockAsignacionDAO = Mockito.mock(AsignacionPersonalDAO.class);
        Field campoDAO = VoluntarioService.class.getDeclaredField("voluntarioDAO");
        campoDAO.setAccessible(true);
        campoDAO.set(service, mockDAO);
        Field campoAsig = VoluntarioService.class.getDeclaredField("asignacionDAO");
        campoAsig.setAccessible(true);
        campoAsig.set(service, mockAsignacionDAO);
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

    // ---- darDeBaja ----

    @Test
    void darDeBaja_enPeriodo_eliminaAsignacionDelCursoActual() {
        Voluntario voluntario = voluntarioConAsignacion("2025/2026");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(voluntario);
        LocalDate fechaMock = LocalDate.of(2025, 8, 15);

        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);

            service.darDeBaja(1L);

            assertTrue(voluntario.getHistorialAsignaciones().isEmpty());
            assertFalse(voluntario.getActivo());
            verify(mockDAO).actualizar(voluntario);
            verify(mockDAO, never()).actualizarActivo(anyLong(), anyBoolean());
        }
    }

    @Test
    void darDeBaja_enPeriodo_noEliminaAsignacionDeAnioAnterior() {
        Voluntario voluntario = voluntarioConAsignacion("2024/2025");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(voluntario);
        LocalDate fechaMock = LocalDate.of(2025, 8, 15);

        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);

            service.darDeBaja(1L);

            assertEquals(1, voluntario.getHistorialAsignaciones().size());
            assertEquals("2024/2025", voluntario.getHistorialAsignaciones().get(0).getAnyoAcademico());
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

    // ---- darDeAlta ----

    @Test
    void darDeAlta_sinAsignacion_creaAsignacionParaCursoActual() {
        Voluntario voluntario = voluntarioBase();
        when(mockDAO.obtenerCompleto(1L)).thenReturn(voluntario);

        try (MockedStatic<UtilFecha> uf = Mockito.mockStatic(UtilFecha.class)) {
            uf.when(UtilFecha::calcularCursoAcademicoPersonal).thenReturn("2025/2026");

            service.darDeAlta(1L);

            verify(mockAsignacionDAO).guardar(any(AsignacionPersonal.class));
        }
    }

    @Test
    void darDeAlta_yaAsignado_noCreaNuevaAsignacion() {
        Voluntario voluntario = voluntarioConAsignacion("2025/2026");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(voluntario);

        try (MockedStatic<UtilFecha> uf = Mockito.mockStatic(UtilFecha.class)) {
            uf.when(UtilFecha::calcularCursoAcademicoPersonal).thenReturn("2025/2026");

            service.darDeAlta(1L);

            verify(mockAsignacionDAO, never()).guardar(any());
        }
    }

    // ---- Auxiliares ----

    private Voluntario voluntarioBase() {
        return new Voluntario("Test", "Apellido", "Calle 1", null, null, null, LocalDate.of(1990, 3, 20));
    }

    private Voluntario voluntarioConAsignacion(String anyo) {
        Voluntario voluntario = voluntarioBase();
        AsignacionPersonal a = new AsignacionPersonal();
        a.setAnyoAcademico(anyo);
        a.setGrupoAsignado("Sin asignar");
        voluntario.addAsignacion(a);
        return voluntario;
    }
}
