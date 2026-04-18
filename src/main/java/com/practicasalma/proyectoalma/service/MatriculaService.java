package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.MatriculaDAO;
import com.practicasalma.proyectoalma.model.Matricula;

public class MatriculaService {

    private final MatriculaDAO matriculaDAO = new MatriculaDAO();

    public void guardar(Matricula matricula) throws Exception {
        matriculaDAO.guardar(matricula);
    }

    public void actualizar(Long id, String curso, String grupoAsignado, boolean esRepeticion) throws Exception {
        matriculaDAO.actualizar(id, curso, grupoAsignado, esRepeticion);
    }
}