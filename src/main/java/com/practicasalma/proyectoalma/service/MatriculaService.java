package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.MatriculaDAO;
import com.practicasalma.proyectoalma.model.Matricula;

/**
 * Lógica de negocio para la gestión de matrículas.
 * <p>
 * Capa fina sobre {@link com.practicasalma.proyectoalma.dao.MatriculaDAO} que sirve
 * de punto de entrada para los controladores. Si en el futuro se añaden validaciones
 * o reglas de negocio sobre matrículas, se implementarán aquí.
 * </p>
 */
public class MatriculaService {

    private final MatriculaDAO matriculaDAO = new MatriculaDAO();

    public void guardar(Matricula matricula) {
        matriculaDAO.guardar(matricula);
    }

    public void actualizar(Long id, String curso, String grupoAsignado, boolean esRepeticion) {
        matriculaDAO.actualizar(id, curso, grupoAsignado, esRepeticion);
    }
}
