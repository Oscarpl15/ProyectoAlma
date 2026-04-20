package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.SocioDAO;
import com.practicasalma.proyectoalma.model.Socio;

import java.util.List;

public class SocioService {

    private final SocioDAO socioDAO = new SocioDAO();

    public void guardarSocio(Socio socio) throws Exception {
        socioDAO.guardar(socio);
    }

    public Socio obtenerCompleto(Long id) {
        return socioDAO.obtenerCompleto(id);
    }

    public void actualizarSocio(Socio socio) throws Exception {
        socioDAO.actualizar(socio);
    }

    public void darDeBaja(Long id) throws Exception {
        Socio socio = socioDAO.obtenerCompleto(id);
        socio.setActivo(false);
        socioDAO.actualizar(socio);
    }

    public void darDeAlta(Long id) throws Exception {
        Socio socio = socioDAO.obtenerCompleto(id);
        socio.setActivo(true);
        socioDAO.actualizar(socio);
    }

    public List<Socio> obtenerTodos() {
        return socioDAO.obtenerTodos();
    }
}
