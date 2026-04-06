package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.DocenteDAO;
import com.practicasalma.proyectoalma.model.Docente;

import java.util.List;

public class DocenteService {

    private final DocenteDAO docenteDAO = new DocenteDAO();

    public void guardarDocente(Docente docente) throws Exception {
        docenteDAO.guardar(docente);
    }

    public Docente obtenerCompleto(Long id) {
        return docenteDAO.obtenerCompleto(id);
    }

    public void actualizarDocente(Docente docente) throws Exception {
        docenteDAO.actualizar(docente);
    }

    public List<Docente> obtenerTodos() {
        return docenteDAO.obtenerTodos();
    }
}
