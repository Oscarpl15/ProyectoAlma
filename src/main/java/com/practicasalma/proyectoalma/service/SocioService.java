package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.SocioDAO;
import com.practicasalma.proyectoalma.exception.EntidadDuplicadaException;
import com.practicasalma.proyectoalma.exception.ValidacionException;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.util.validacion.Validador;

import java.util.List;

/**
 * Lógica de negocio para la gestión de socios.
 * <p>
 * Valida los datos del socio (DNI/NIE, teléfono, IBAN) antes de persistir.
 * Nota: {@code Socio} no tiene campo {@code fechaNacimiento}, por lo que
 * esa validación no aplica aquí a diferencia de otros tipos de persona.
 * </p>
 */
public class SocioService {

    private final SocioDAO socioDAO = new SocioDAO();

    private void validarSocio(Socio socio) {
        String dni = socio.getDocumentoIdentidad();
        if (dni != null && !dni.isBlank() && !Validador.esDni(dni) && !Validador.esNie(dni)) {
            throw new ValidacionException("El DNI/NIE introducido no es válido. Revisa las letras y números.");
        }
        String telefono = socio.getTelefono();
        if (telefono != null && !telefono.isBlank() && !Validador.esTelefonoValido(telefono)) {
            throw new ValidacionException("El teléfono no es válido. Debe tener 9 dígitos.");
        }
        String iban = socio.getCuentaBancaria();
        if (iban != null && !iban.isBlank() && !Validador.esIbanValido(iban)) {
            throw new ValidacionException("El IBAN introducido no es válido.");
        }
    }

    /**
     * Persiste un socio nuevo tras validar sus datos y comprobar que el DNI no está duplicado.
     *
     * @param socio socio a persistir (sin ID)
     * @throws com.practicasalma.proyectoalma.exception.ValidacionException       si algún dato no supera las validaciones
     * @throws com.practicasalma.proyectoalma.exception.EntidadDuplicadaException si ya existe un socio con ese DNI
     */
    public void guardarSocio(Socio socio) {
        validarSocio(socio);

        String dni = socio.getDocumentoIdentidad();
        if (dni != null && !dni.isBlank() && socioDAO.existeDni(dni)) {
            throw new EntidadDuplicadaException("Ya existe un socio registrado con ese DNI/NIE.");
        }

        socioDAO.guardar(socio);
    }

    public Socio obtenerCompleto(Long id) {
        return socioDAO.obtenerCompleto(id);
    }

    public void actualizarSocio(Socio socio) {
        validarSocio(socio);
        socioDAO.actualizar(socio);
    }

    public void darDeBaja(Long id) {
        Socio socio = socioDAO.obtenerCompleto(id);
        socio.setActivo(false);
        socioDAO.actualizar(socio);
    }

    public void darDeAlta(Long id) {
        Socio socio = socioDAO.obtenerCompleto(id);
        socio.setActivo(true);
        socioDAO.actualizar(socio);
    }

    public List<Socio> obtenerTodos() {
        return socioDAO.obtenerTodos();
    }
}
