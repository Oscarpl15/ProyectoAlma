package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AsignacionPersonalDAO;
import com.practicasalma.proyectoalma.dao.VoluntarioDAO;
import com.practicasalma.proyectoalma.exception.EntidadDuplicadaException;
import com.practicasalma.proyectoalma.exception.ValidacionException;
import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Voluntario;
import com.practicasalma.proyectoalma.util.UtilFecha;
import com.practicasalma.proyectoalma.util.validacion.Validador;

import java.util.List;

/**
 * Lógica de negocio para la gestión de voluntarios.
 * <p>
 * Idéntica en estructura a {@link DocenteService}: valida datos (DNI/NIE, teléfono,
 * correo, fecha de nacimiento), verifica duplicados por DNI y asigna automáticamente
 * el curso académico actual al dar de alta o reactivar un voluntario.
 * </p>
 */
public class VoluntarioService {

    private final VoluntarioDAO voluntarioDAO = new VoluntarioDAO();

    private void validarVoluntario(Voluntario voluntario) {
        if (voluntario.getFechaNacimiento() != null && !Validador.esFechaNacimientoValida(voluntario.getFechaNacimiento())) {
            throw new ValidacionException("La fecha de nacimiento no es válida.");
        }
        String dni = voluntario.getDocumentoIdentidad();
        if (dni != null && !dni.isBlank() && !Validador.esDni(dni) && !Validador.esNie(dni)) {
            throw new ValidacionException("El DNI/NIE introducido no es válido. Revisa las letras y números.");
        }
        String telefono = voluntario.getTelefono();
        if (telefono != null && !telefono.isBlank() && !Validador.esTelefonoValido(telefono)) {
            throw new ValidacionException("El teléfono no es válido. Debe tener 9 dígitos.");
        }
        String correo = voluntario.getCorreo();
        if (correo != null && !correo.isBlank() && !Validador.esEmailValido(correo)) {
            throw new ValidacionException("El correo electrónico no tiene un formato válido.");
        }
    }

    /**
     * Persiste un voluntario nuevo validando sus datos y creando su primera asignación.
     *
     * @param voluntario voluntario a persistir (sin ID)
     * @throws com.practicasalma.proyectoalma.exception.ValidacionException       si algún dato no supera las validaciones
     * @throws com.practicasalma.proyectoalma.exception.EntidadDuplicadaException si ya existe un voluntario con ese DNI
     */
    public void guardarVoluntario(Voluntario voluntario) {
        validarVoluntario(voluntario);

        String dni = voluntario.getDocumentoIdentidad();
        if (dni != null && !dni.isBlank() && voluntarioDAO.existeDni(dni)) {
            throw new EntidadDuplicadaException("Ya existe un voluntario registrado con ese DNI/NIE.");
        }

        String anyo = UtilFecha.calcularCursoAcademicoPersonal();
        boolean yaExiste = voluntario.getHistorialAsignaciones().stream()
                .anyMatch(a -> anyo.equals(a.getAnyoAcademico())
                        || anyo.replace("/", "-").equals(a.getAnyoAcademico()));
        if (!yaExiste) {
            AsignacionPersonal asig = new AsignacionPersonal(voluntario, anyo);
            voluntario.addAsignacion(asig);
        }
        voluntarioDAO.guardar(voluntario);
    }

    public Voluntario obtenerCompleto(Long id) {
        return voluntarioDAO.obtenerCompleto(id);
    }

    public void actualizarVoluntario(Voluntario voluntario) {
        validarVoluntario(voluntario);
        voluntarioDAO.actualizar(voluntario);
    }

    public void actualizarActivo(Long id, boolean activo) {
        voluntarioDAO.actualizarActivo(id, activo);
    }

    /**
     * Registra una nueva asignación de continuación para un voluntario en el año académico indicado.
     *
     * @param voluntario    voluntario que continúa
     * @param anyoAcademico año académico en formato {@code "AAAA/AAAA+1"}
     */
    public void registrarContinuacion(Voluntario voluntario, String anyoAcademico) {
        AsignacionPersonalDAO asignacionDAO = new AsignacionPersonalDAO();
        asignacionDAO.guardar(new AsignacionPersonal(voluntario, anyoAcademico));
    }

    public void darDeBaja(Long id) {
        voluntarioDAO.actualizarActivo(id, false);
    }

    public void darDeAlta(Long id) {
        voluntarioDAO.actualizarActivo(id, true);
        Voluntario voluntario = voluntarioDAO.obtenerCompleto(id);
        String anyo = UtilFecha.calcularCursoAcademicoPersonal();
        boolean yaExiste = voluntario.getHistorialAsignaciones() != null &&
                voluntario.getHistorialAsignaciones().stream()
                        .anyMatch(a -> anyo.equals(a.getAnyoAcademico()));
        if (!yaExiste) {
            registrarContinuacion(voluntario, anyo);
        }
    }

    public List<Voluntario> obtenerTodos() {
        return voluntarioDAO.obtenerTodos();
    }
}
