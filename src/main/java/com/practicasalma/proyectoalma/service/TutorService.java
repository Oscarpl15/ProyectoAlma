package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.TutorDAO;
import com.practicasalma.proyectoalma.model.Tutor;

import java.util.List;

public class TutorService {

    private final TutorDAO tutorDAO = new TutorDAO();

    public void guardarTutor(Tutor tutor) throws Exception {
        tutorDAO.guardar(tutor);
    }

    public List<Tutor> obtenerTodos() {
        return tutorDAO.obtenerTodos();
    }
}
