package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AlumnoDAO;
import com.practicasalma.proyectoalma.exception.EntidadDuplicadaException;
import com.practicasalma.proyectoalma.exception.ValidacionException;
import com.practicasalma.proyectoalma.model.Alumno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        alumno.setDocumentoIdentidad("00000000A"); // letra incorrecta
        alumno.setTipoDocumento("DNI");

        assertThrows(ValidacionException.class,
                () -> service.matricularNuevoAlumno(alumno, "1º Primaria", null));
    }

    @Test
    void matricularNuevoAlumno_nieInvalidoLanzaValidacionException() {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));
        alumno.setDocumentoIdentidad("X0000000A"); // letra incorrecta
        alumno.setTipoDocumento("NIE");

        assertThrows(ValidacionException.class,
                () -> service.matricularNuevoAlumno(alumno, "1º Primaria", null));
    }

    @Test
    void matricularNuevoAlumno_telefonoInvalidoLanzaValidacionException() {
        Alumno alumno = alumnoConFecha(LocalDate.of(2010, 1, 1));
        alumno.setTelefono("12345"); // menos de 9 dígitos

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

    // ---- Auxiliar ----

    private Alumno alumnoConFecha(LocalDate fecha) {
        return new Alumno("Test", "Apellido", "Calle 1", fecha);
    }
}
