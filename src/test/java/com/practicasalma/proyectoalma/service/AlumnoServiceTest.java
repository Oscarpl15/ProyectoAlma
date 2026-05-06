package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AlumnoDAO;
import com.practicasalma.proyectoalma.exception.EntidadDuplicadaException;
import com.practicasalma.proyectoalma.exception.ValidacionException;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.util.UtilFecha;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AlumnoServiceTest {

    private AlumnoService service;
    private AlumnoDAO mockDAO;

    @BeforeEach
    void setUp() throws Exception {
        service = new AlumnoService();
        mockDAO = Mockito.mock(AlumnoDAO.class);
        Field campo = AlumnoService.class.getDeclaredField("alumnoDAO");
        campo.setAccessible(true);
        campo.set(service, mockDAO);
    }

    // ---- Validación: DNI ----

    @Test
    void matricularNuevoAlumno_dniInvalidoLanzaValidacionException() {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));
        alumno.setDocumentoIdentidad("00000000A");
        alumno.setTipoDocumento("DNI");

        assertThrows(ValidacionException.class,
                () -> service.matricularNuevoAlumno(alumno, "1º Primaria", null));
    }

    @Test
    void matricularNuevoAlumno_nieInvalidoLanzaValidacionException() {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));
        alumno.setDocumentoIdentidad("X0000000A");
        alumno.setTipoDocumento("NIE");

        assertThrows(ValidacionException.class,
                () -> service.matricularNuevoAlumno(alumno, "1º Primaria", null));
    }

    @Test
    void matricularNuevoAlumno_telefonoInvalidoLanzaValidacionException() {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));
        alumno.setTelefono("12345");

        assertThrows(ValidacionException.class,
                () -> service.matricularNuevoAlumno(alumno, "1º Primaria", null));
    }

    @Test
    void matricularNuevoAlumno_fechaNacimientoFuturaLanzaValidacionException() {
        Alumno alumno = alumnoConFecha(LocalDate.now().plusDays(1));

        assertThrows(ValidacionException.class,
                () -> service.matricularNuevoAlumno(alumno, "1º Primaria", null));
    }

    // ---- Validación: duplicado ----

    @Test
    void matricularNuevoAlumno_dniDuplicadoLanzaEntidadDuplicadaException() {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));
        alumno.setDocumentoIdentidad("12345678Z");
        alumno.setTipoDocumento("DNI");

        when(mockDAO.existeDni("12345678Z")).thenReturn(true);

        assertThrows(EntidadDuplicadaException.class,
                () -> service.matricularNuevoAlumno(alumno, "1º Primaria", null));
    }

    // ---- Flujo válido ----

    @Test
    void matricularNuevoAlumno_datosValidosLlamaAGuardar() {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));
        alumno.setDocumentoIdentidad("12345678Z");
        alumno.setTipoDocumento("DNI");

        when(mockDAO.existeDni("12345678Z")).thenReturn(false);

        service.matricularNuevoAlumno(alumno, "1º Primaria", "g1 lunes/miercoles");

        verify(mockDAO, times(1)).guardar(alumno);
    }

    @Test
    void matricularNuevoAlumno_sinDniNoBuscaDuplicado() {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));

        service.matricularNuevoAlumno(alumno, "1º Primaria", null);

        verify(mockDAO, never()).existeDni(anyString());
        verify(mockDAO, times(1)).guardar(alumno);
    }

    // ---- Actualizar ----

    @Test
    void actualizarAlumno_dniDuplicadoParaOtroLanzaEntidadDuplicadaException() {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));
        alumno.setDocumentoIdentidad("12345678Z");
        alumno.setTipoDocumento("DNI");

        when(mockDAO.existeDniParaOtro(eq("12345678Z"), any())).thenReturn(true);

        assertThrows(EntidadDuplicadaException.class, () -> service.actualizarAlumno(alumno));
    }

    @Test
    void actualizarAlumno_dniNoTomadoLlamaAActualizar() {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));
        alumno.setDocumentoIdentidad("12345678Z");
        alumno.setTipoDocumento("DNI");

        when(mockDAO.existeDniParaOtro(eq("12345678Z"), any())).thenReturn(false);

        service.actualizarAlumno(alumno);

        verify(mockDAO, times(1)).actualizar(alumno);
    }

    // ---- darDeBaja ----

    @Test
    void darDeBaja_enPeriodo_eliminaMatriculaDelCursoActual() {
        Alumno alumno = alumnoConMatricula("2025/2026", "1º Primaria");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(alumno);
        LocalDate fechaMock = LocalDate.of(2025, 8, 15);

        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getMatriculasMesInicio).thenReturn(6);

            service.darDeBaja(1L);

            assertTrue(alumno.getMatriculas().isEmpty());
            assertFalse(alumno.getActivo());
            verify(mockDAO).actualizar(alumno);
        }
    }

    @Test
    void darDeBaja_enPeriodo_formatoGuion_eliminaMatricula() {
        Alumno alumno = alumnoConMatricula("2025-2026", "1º Primaria");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(alumno);
        LocalDate fechaMock = LocalDate.of(2025, 8, 15);

        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getMatriculasMesInicio).thenReturn(6);

            service.darDeBaja(1L);

            assertTrue(alumno.getMatriculas().isEmpty());
        }
    }

    @Test
    void darDeBaja_enPeriodo_noEliminaMatriculaDeAnioAnterior() {
        // alumno pospuesto: solo tiene la matrícula del año anterior
        Alumno alumno = alumnoConMatricula("2024/2025", "5º Primaria");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(alumno);
        LocalDate fechaMock = LocalDate.of(2025, 8, 15);

        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getMatriculasMesInicio).thenReturn(6);

            service.darDeBaja(1L);

            assertEquals(1, alumno.getMatriculas().size());
            assertEquals("2024/2025", alumno.getMatriculas().get(0).getAnyoAcademico());
        }
    }

    @Test
    void darDeBaja_fueraPeriodo_noEliminaMatricula() {
        Alumno alumno = alumnoConMatricula("2024/2025", "3º Primaria");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(alumno);
        LocalDate fechaMock = LocalDate.of(2025, 2, 15);

        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getMatriculasMesInicio).thenReturn(6);

            service.darDeBaja(1L);

            assertEquals(1, alumno.getMatriculas().size());
        }
    }

    @Test
    void darDeBaja_enOctubre_noEliminaMatricula() {
        Alumno alumno = alumnoConMatricula("2025/2026", "1º Primaria");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(alumno);
        LocalDate fechaMock = LocalDate.of(2025, 10, 5);

        try (MockedStatic<LocalDate> ld = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<GestorConfig> gc = Mockito.mockStatic(GestorConfig.class)) {
            ld.when(LocalDate::now).thenReturn(fechaMock);
            gc.when(GestorConfig::getMatriculasMesInicio).thenReturn(6);

            service.darDeBaja(1L);

            assertEquals(1, alumno.getMatriculas().size());
        }
    }

    // ---- darDeAlta ----

    @Test
    void darDeAlta_sinMatriculas_creaMatriculaEn1eroInfantil() {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));
        when(mockDAO.obtenerCompleto(1L)).thenReturn(alumno);

        try (MockedStatic<UtilFecha> uf = Mockito.mockStatic(UtilFecha.class)) {
            uf.when(UtilFecha::calcularCursoAcademico).thenReturn("2025/2026");

            String aviso = service.darDeAlta(1L);

            assertNull(aviso);
            assertTrue(alumno.getActivo());
            assertEquals(1, alumno.getMatriculas().size());
            assertEquals("2025/2026", alumno.getMatriculas().get(0).getAnyoAcademico());
            assertEquals("1º Infantil", alumno.getMatriculas().get(0).getCurso());
            verify(mockDAO).actualizar(alumno);
        }
    }

    @Test
    void darDeAlta_yaMatriculadoEnCursoActual_noCreaNuevaMatricula() {
        Alumno alumno = alumnoConMatricula("2025/2026", "1º Primaria");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(alumno);

        try (MockedStatic<UtilFecha> uf = Mockito.mockStatic(UtilFecha.class)) {
            uf.when(UtilFecha::calcularCursoAcademico).thenReturn("2025/2026");

            service.darDeAlta(1L);

            assertEquals(1, alumno.getMatriculas().size());
        }
    }

    @Test
    void darDeAlta_conMatriculaAnterior_avanzaCursoSegunAnioTranscurridos() {
        // 3º Primaria en 2023/2024 + 2 años = 5º Primaria en 2025/2026
        Alumno alumno = alumnoConMatricula("2023/2024", "3º Primaria");
        when(mockDAO.obtenerCompleto(1L)).thenReturn(alumno);

        try (MockedStatic<UtilFecha> uf = Mockito.mockStatic(UtilFecha.class)) {
            uf.when(UtilFecha::calcularCursoAcademico).thenReturn("2025/2026");

            service.darDeAlta(1L);

            Matricula nueva = alumno.getMatriculas().get(1);
            assertEquals("2025/2026", nueva.getAnyoAcademico());
            assertEquals("5º Primaria", nueva.getCurso());
        }
    }

    @Test
    void darDeAlta_ultimaMatriculaSinCurso_devuelveAvisoYAsigna1eroInfantil() {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));
        Matricula m = new Matricula();
        m.setAnyoAcademico("2024/2025");
        m.setCurso(null);
        alumno.addMatricula(m);
        when(mockDAO.obtenerCompleto(1L)).thenReturn(alumno);

        try (MockedStatic<UtilFecha> uf = Mockito.mockStatic(UtilFecha.class)) {
            uf.when(UtilFecha::calcularCursoAcademico).thenReturn("2025/2026");

            String aviso = service.darDeAlta(1L);

            assertNotNull(aviso);
            assertEquals(2, alumno.getMatriculas().size());
            assertEquals("1º Infantil", alumno.getMatriculas().get(1).getCurso());
        }
    }

    // ---- Auxiliares ----

    private Alumno alumnoConFecha(LocalDate fecha) {
        return new Alumno("Test", "Apellido", "Calle 1", fecha);
    }

    private Alumno alumnoConMatricula(String anyo, String curso) {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));
        Matricula m = new Matricula();
        m.setAnyoAcademico(anyo);
        m.setCurso(curso);
        alumno.addMatricula(m);
        return alumno;
    }
}
