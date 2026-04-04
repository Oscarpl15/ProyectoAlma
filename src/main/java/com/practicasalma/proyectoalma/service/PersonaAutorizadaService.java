package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.PersonaAutorizadaDAO;
import com.practicasalma.proyectoalma.model.PersonaAutorizada;

import java.util.List;

public class PersonaAutorizadaService {

    private final PersonaAutorizadaDAO personaAutorizadaDAO = new PersonaAutorizadaDAO();

    public List<PersonaAutorizada> obtenerTodos() {
        return personaAutorizadaDAO.obtenerTodos();
    }
}
