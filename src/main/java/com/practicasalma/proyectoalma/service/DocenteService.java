package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AsignacionPersonalDAO;
import com.practicasalma.proyectoalma.dao.DocenteDAO;
import com.practicasalma.proyectoalma.exception.EntidadDuplicadaException;
import com.practicasalma.proyectoalma.exception.ValidacionException;
import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.util.UtilFecha;
import com.practicasalma.proyectoalma.util.validacion.Validador;

import java.util.List;

/**
 * Lógica de negocio para la gestión de docentes.
 * <p>
 * Valida los datos del docente (DNI/NIE, teléfono, correo, fecha de nacimiento)
 * y registra automáticamente una asignación al curso académico actual al dar
 * de alta a un docente nuevo o al reactivarlo.
 * </p>
 */
public class DocenteService {

    private final DocenteDAO docenteDAO = new DocenteDAO();

    private void validarDocente(Docente docente) {
        if (docente.getFechaNacimiento() != null && !Validador.esFechaNacimientoValida(docente.getFechaNacimiento())) {
            throw new ValidacionException("La fecha de nacimiento no es válida.");
        }
        String dni = docente.getDocumentoIdentidad();
        if (dni != null && !dni.isBlank()) {
            String tipo = docente.getTipoDocumento();
            if ("DNI".equals(tipo)) {
                if (!Validador.esDni(dni)) throw new ValidacionException("El DNI introducido no es válido. Revisa el número y la letra.");
            } else if ("NIE".equals(tipo)) {
                if (!Validador.esNie(dni)) throw new ValidacionException("El NIE introducido no es válido. Revisa el número y la letra.");
            } else if (!"Pasaporte".equals(tipo) && !Validador.esDni(dni) && !Validador.esNie(dni)) {
                throw new ValidacionException("El DNI/NIE introducido no es válido. Revisa las letras y números.");
            }
        }
        String telefono = docente.getTelefono();
        if (telefono != null && !telefono.isBlank() && !Validador.esTelefonoValido(telefono)) {
            throw new ValidacionException("El teléfono no es válido. Debe tener 9 dígitos.");
        }
        String correo = docente.getCorreo();
        if (correo != null && !correo.isBlank() && !Validador.esEmailValido(correo)) {
            throw new ValidacionException("El correo electrónico no tiene un formato válido.");
        }
    }

    /**
     * Persiste un docente nuevo validando sus datos y creando su primera asignación.
     *
     * @param docente docente a persistir (sin ID)
     * @throws com.practicasalma.proyectoalma.exception.ValidacionException       si algún dato no supera las validaciones
     * @throws com.practicasalma.proyectoalma.exception.EntidadDuplicadaException si ya existe un docente con ese DNI
     */
    public void guardarDocente(Docente docente) {
        validarDocente(docente);

        String dni = docente.getDocumentoIdentidad();
        if (dni != null && !dni.isBlank() && docenteDAO.existeDni(dni)) {
            throw new EntidadDuplicadaException("Ya existe un docente registrado con ese DNI/NIE.");
        }

        String anyo = UtilFecha.calcularCursoAcademicoPersonal();
        boolean yaExiste = docente.getHistorialAsignaciones().stream()
                .anyMatch(a -> anyo.equals(a.getAnyoAcademico())
                        || anyo.replace("/", "-").equals(a.getAnyoAcademico()));
        if (!yaExiste) {
            AsignacionPersonal asig = new AsignacionPersonal(docente, anyo);
            docente.addAsignacion(asig);
        }
        docenteDAO.guardar(docente);
    }

    public Docente obtenerCompleto(Long id) {
        return docenteDAO.obtenerCompleto(id);
    }

    public void actualizarDocente(Docente docente) {
        validarDocente(docente);
        docenteDAO.actualizar(docente);
    }

    public void actualizarActivo(Long id, boolean activo) {
        docenteDAO.actualizarActivo(id, activo);
    }

    /**
     * Registra una nueva asignación de continuación para un docente en el año académico indicado.
     * Se usa cuando el diálogo de inicio de curso confirma la renovación manual.
     *
     * @param docente       docente que continúa
     * @param anyoAcademico año académico en formato {@code "AAAA/AAAA+1"}
     */
    public void registrarContinuacion(Docente docente, String anyoAcademico) {
        AsignacionPersonalDAO asignacionDAO = new AsignacionPersonalDAO();
        asignacionDAO.guardar(new AsignacionPersonal(docente, anyoAcademico));
    }

    public void darDeBaja(Long id) {
        docenteDAO.actualizarActivo(id, false);
    }

    public void darDeAlta(Long id) {
        docenteDAO.actualizarActivo(id, true);
        Docente docente = docenteDAO.obtenerCompleto(id);
        String anyo = UtilFecha.calcularCursoAcademicoPersonal();
        boolean yaExiste = docente.getHistorialAsignaciones() != null &&
                docente.getHistorialAsignaciones().stream()
                        .anyMatch(a -> anyo.equals(a.getAnyoAcademico()));
        if (!yaExiste) {
            registrarContinuacion(docente, anyo);
        }
    }

    public List<Docente> obtenerTodos() {
        return docenteDAO.obtenerTodos();
    }
}
