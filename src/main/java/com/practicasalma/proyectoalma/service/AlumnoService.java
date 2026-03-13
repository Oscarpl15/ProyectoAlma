package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AlumnoDAO;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;

public class AlumnoService {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

    // Este es el metodo que llamará el controlador
    public void matricularNuevoAlumno(Alumno alumno, String curso) throws Exception {

        // Aquí en el futuro puedes añadir validaciones de negocio:
        // Ej: if (alumnoDAO.existeDni(alumno.getDocumentoIdentidad())) throw new EntidadDuplicadaException();

        // Aplicamos la regla de negocio: Un alumno nuevo necesita una matrícula
        Matricula matricula = new Matricula(curso.trim(), alumno);
        alumno.addMatricula(matricula);

        // Mandamos a guardar
        alumnoDAO.guardar(alumno);
    }
}
