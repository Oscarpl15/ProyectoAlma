package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.SocioDAO;
import com.practicasalma.proyectoalma.model.Socio;

import java.util.List;

public class SocioService {

    private final SocioDAO socioDAO = new SocioDAO();

    public void guardarSocio(Socio socio) throws Exception {
        socioDAO.guardar(socio);
    }

    public void actualizarSocio(Socio socio) throws Exception {
        socioDAO.actualizar(socio);
    }

    public List<Socio> obtenerTodos() {
        return socioDAO.obtenerTodos();
    }
}
