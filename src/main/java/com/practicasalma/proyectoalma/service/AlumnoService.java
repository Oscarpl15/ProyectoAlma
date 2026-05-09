package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AlumnoDAO;
import com.practicasalma.proyectoalma.dao.PersonaAutorizadaDAO;
import com.practicasalma.proyectoalma.dao.TutorDAO;
import com.practicasalma.proyectoalma.exception.EntidadDuplicadaException;
import com.practicasalma.proyectoalma.exception.ValidacionException;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.model.PersonaAutorizada;
import com.practicasalma.proyectoalma.model.Tutor;
import com.practicasalma.proyectoalma.util.UtilFecha;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.validacion.Validador;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio para la gestión de alumnos.
 * <p>
 * Coordina las validaciones de datos (DNI, teléfono, fecha de nacimiento),
 * la comprobación de duplicados y la creación automática de matrículas.
 * Al dar de alta a un alumno ya existente se calcula su próximo curso
 * según los años transcurridos desde la última matrícula.
 * </p>
 */
public class AlumnoService {

    private static final List<String> CURSOS_PRIMARIA = List.of(
            "1º Infantil", "2º Infantil", "3º Infantil",
            "1º Primaria", "2º Primaria", "3º Primaria",
            "4º Primaria", "5º Primaria", "6º Primaria"
    );

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final PersonaAutorizadaDAO paDAO = new PersonaAutorizadaDAO();
    private final TutorDAO tutorDAO = new TutorDAO();

    private void validarAlumno(Alumno alumno) {
        if (alumno.getFechaNacimiento() != null && !Validador.esFechaNacimientoValida(alumno.getFechaNacimiento())) {
            throw new ValidacionException("La fecha de nacimiento no es válida.");
        }
        String dni = alumno.getDocumentoIdentidad();
        if (dni != null && !dni.isBlank()) {
            String tipo = alumno.getTipoDocumento();
            if ("DNI".equals(tipo)) {
                if (!Validador.esDni(dni)) throw new ValidacionException("El DNI introducido no es válido. Revisa el número y la letra.");
            } else if ("NIE".equals(tipo)) {
                if (!Validador.esNie(dni)) throw new ValidacionException("El NIE introducido no es válido. Revisa el número y la letra.");
            } else if (!"Pasaporte".equals(tipo) && !Validador.esDni(dni) && !Validador.esNie(dni)) {
                throw new ValidacionException("El DNI/NIE introducido no es válido. Revisa las letras y números.");
            }
        }
        String telefono = alumno.getTelefono();
        if (telefono != null && !telefono.isBlank() && !Validador.esTelefonoValido(telefono)) {
            throw new ValidacionException("El teléfono no es válido. Debe tener 9 dígitos.");
        }
        String cp = alumno.getCodigoPostal();
        if (cp != null && !cp.isBlank() && !Validador.esCodigoPostalValido(cp)) {
            throw new ValidacionException("El código postal no es válido. Debe tener exactamente 5 dígitos.");
        }
    }

    /**
     * Registra un alumno nuevo junto con su matrícula inicial para el curso académico actual.
     *
     * @param alumno alumno a persistir (sin ID)
     * @param curso  nombre del curso (p. ej., "1º Primaria")
     * @param grupo  grupo asignado, puede ser {@code null}
     * @throws com.practicasalma.proyectoalma.exception.ValidacionException      si algún dato no supera las validaciones
     * @throws com.practicasalma.proyectoalma.exception.EntidadDuplicadaException si ya existe un alumno con ese DNI
     */
    public void matricularNuevoAlumno(Alumno alumno, String curso, String grupo) {
        normalizarDocumento(alumno);
        validarAlumno(alumno);

        String dni = alumno.getDocumentoIdentidad();
        if (dni != null && !dni.isBlank() && alumnoDAO.existeDni(dni)) {
            throw new EntidadDuplicadaException("Ya existe un alumno registrado con ese DNI/NIE.");
        }

        String anyoActual = UtilFecha.calcularCursoAcademico();
        Matricula matricula = new Matricula(curso.trim(), alumno);
        matricula.setAnyoAcademico(anyoActual);
        if (grupo != null) {
            matricula.setGrupoAsignado(grupo.trim());
        }
        alumno.addMatricula(matricula);

        alumnoDAO.guardar(alumno);
    }

    public void actualizarAlumno(Alumno alumno) {
        normalizarDocumento(alumno);
        validarAlumno(alumno);
        String dni = alumno.getDocumentoIdentidad();
        if (dni != null && !dni.isBlank() && alumnoDAO.existeDniParaOtro(dni, alumno.getId())) {
            throw new EntidadDuplicadaException("Ya existe otro alumno registrado con ese DNI/NIE.");
        }
        alumnoDAO.actualizarDatosPersonales(alumno);
    }

    public void vincularTutor(Alumno alumno, Tutor tutor) {
        if (tutor.getId() == null) {
            String doc = tutor.getDocumentoIdentidad();
            if (doc != null && !doc.isBlank()) {
                Tutor existente = tutorDAO.buscarPorDocumento(doc);
                if (existente != null) {
                    tutor = existente;
                } else {
                    tutorDAO.guardar(tutor);
                }
            } else {
                tutorDAO.guardar(tutor);
            }
        }
        alumnoDAO.vincularTutor(alumno.getId(), tutor.getId());
    }

    public void desvincularTutor(Alumno alumno, Tutor tutor) {
        alumnoDAO.desvincularTutor(alumno.getId(), tutor.getId());
        if (tutor.getId() != null && tutorDAO.contarAlumnos(tutor.getId()) == 0) {
            tutorDAO.eliminar(tutor);
        }
    }

    public void vincularPersonaAutorizada(Alumno alumno, PersonaAutorizada pa) {
        if (pa.getId() == null) {
            String doc = pa.getDocumentoIdentidad();
            if (doc != null && !doc.isBlank()) {
                PersonaAutorizada existente = paDAO.buscarPorDocumento(doc);
                if (existente != null) {
                    pa = existente;
                } else {
                    paDAO.guardar(pa);
                }
            } else {
                paDAO.guardar(pa);
            }
        }
        alumnoDAO.vincularPersonaAutorizada(alumno.getId(), pa.getId());
    }

    public void desvincularPersonaAutorizada(Alumno alumno, PersonaAutorizada pa) {
        alumnoDAO.desvincularPersonaAutorizada(alumno.getId(), pa.getId());
        if (pa.getId() != null && paDAO.contarAlumnos(pa.getId()) == 0) {
            paDAO.eliminar(pa);
        }
    }

    public List<Alumno> obtenerTodos() {
        return alumnoDAO.obtenerTodos();
    }

    public Alumno obtenerCompleto(Long id) {
        return alumnoDAO.obtenerCompleto(id);
    }

    /**
     * Marca un alumno como inactivo (baja) sin eliminar sus datos ni matrículas.
     *
     * @param id identificador del alumno
     */
    public void darDeBaja(Long id) {
        Alumno alumno = alumnoDAO.obtenerCompleto(id);
        alumno.setActivo(false);
        int mes = LocalDate.now().getMonthValue();
        int mesInicio = GestorConfig.getMatriculasMesInicio();
        if (mes >= mesInicio && mes < 10) {
            int year = LocalDate.now().getYear();
            String anyoNuevo = year + "/" + (year + 1);
            String anyoNuevoAlt = year + "-" + (year + 1);
            alumno.getMatriculas().removeIf(m ->
                    anyoNuevo.equals(m.getAnyoAcademico()) || anyoNuevoAlt.equals(m.getAnyoAcademico()));
        }
        alumnoDAO.actualizar(alumno);
    }

    /**
     * Reactiva un alumno y, si no tiene matrícula para el curso actual, crea una nueva
     * calculando automáticamente el nivel según los años transcurridos.
     *
     * @param id identificador del alumno
     */
    public String darDeAlta(Long id) {
        Alumno alumno = alumnoDAO.obtenerCompleto(id);
        alumno.setActivo(true);
        String anyoNuevo = UtilFecha.calcularCursoAcademico();
        boolean yaMatriculado = alumno.getMatriculas().stream()
                .anyMatch(m -> anyoNuevo.equals(m.getAnyoAcademico()));
        String aviso = null;
        if (!yaMatriculado) {
            Matricula ultima = alumno.getUltimaMatricula();
            if (ultima != null && ultima.getCurso() == null) {
                aviso = "No se pudo determinar el curso de " + alumno.getNombre() +
                        " porque su última matrícula no tiene curso asignado. " +
                        "Se ha asignado 1º Infantil. Revísalo manualmente.";
            }
            Matricula nueva = new Matricula(calcularCursoParaAlta(alumno), alumno);
            nueva.setAnyoAcademico(anyoNuevo);
            alumno.addMatricula(nueva);
        }
        alumnoDAO.actualizar(alumno);
        return aviso;
    }

    private void normalizarDocumento(Alumno alumno) {
        String doc = alumno.getDocumentoIdentidad();
        if (doc != null && doc.isBlank()) {
            alumno.setDocumentoIdentidad(null);
        }
    }

    private String calcularCursoParaAlta(Alumno alumno) {
        Matricula ultima = alumno.getUltimaMatricula();
        if (ultima == null) {
            return CURSOS_PRIMARIA.get(0);
        }
        int indice = ultima.getCurso() != null ? CURSOS_PRIMARIA.indexOf(ultima.getCurso()) : -1;
        if (indice == -1) return CURSOS_PRIMARIA.get(0);

        String anyoAcademicoUltimo = ultima.getAnyoAcademico();
        if (anyoAcademicoUltimo != null && anyoAcademicoUltimo.contains("/")) {
            try {
                int anyoUltimo = Integer.parseInt(anyoAcademicoUltimo.split("/")[0]);
                int anyoActual = Integer.parseInt(UtilFecha.calcularCursoAcademico().split("/")[0]);
                indice = Math.min(indice + Math.max(0, anyoActual - anyoUltimo), CURSOS_PRIMARIA.size() - 1);
            } catch (NumberFormatException ignored) {
                indice = Math.min(indice + 1, CURSOS_PRIMARIA.size() - 1);
            }
        } else {
            indice = Math.min(indice + 1, CURSOS_PRIMARIA.size() - 1);
        }
        return CURSOS_PRIMARIA.get(indice);
    }
}
