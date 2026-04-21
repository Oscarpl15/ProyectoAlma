package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.PersonaAutorizadaDAO;
import com.practicasalma.proyectoalma.model.PersonaAutorizada;

import java.util.List;

/**
 * Lógica de negocio para la gestión de personas autorizadas a recoger alumnos.
 * <p>
 * Capa fina sobre {@link com.practicasalma.proyectoalma.dao.PersonaAutorizadaDAO}.
 * Las personas autorizadas se vinculan a un alumno concreto desde {@code FichaAlumnoController}.
 * </p>
 */
public class PersonaAutorizadaService {

    private final PersonaAutorizadaDAO personaAutorizadaDAO = new PersonaAutorizadaDAO();

    public List<PersonaAutorizada> obtenerTodos() {
        return personaAutorizadaDAO.obtenerTodos();
    }
}
