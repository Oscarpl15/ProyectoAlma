package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.VoluntarioDAO;
import com.practicasalma.proyectoalma.model.Voluntario;

import java.util.List;

public class VoluntarioService {

    private final VoluntarioDAO voluntarioDAO = new VoluntarioDAO();

    public void guardarVoluntario(Voluntario voluntario) throws Exception {
        voluntarioDAO.guardar(voluntario);
    }

    public void actualizarVoluntario(Voluntario voluntario) throws Exception {
        voluntarioDAO.actualizar(voluntario);
    }

    public List<Voluntario> obtenerTodos() {
        return voluntarioDAO.obtenerTodos();
    }
}
