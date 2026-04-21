package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.TutorDAO;
import com.practicasalma.proyectoalma.model.Tutor;

import java.util.List;

/**
 * Lógica de negocio para la gestión de tutores legales de los alumnos.
 * <p>
 * Capa fina sobre {@link com.practicasalma.proyectoalma.dao.TutorDAO}.
 * Un tutor puede estar vinculado a varios alumnos (hermanos, etc.) mediante
 * la tabla {@code alumno_tutor}.
 * </p>
 */
public class TutorService {

    private final TutorDAO tutorDAO = new TutorDAO();

    public void guardarTutor(Tutor tutor) {
        tutorDAO.guardar(tutor);
    }

    public List<Tutor> obtenerTodos() {
        return tutorDAO.obtenerTodos();
    }
}
