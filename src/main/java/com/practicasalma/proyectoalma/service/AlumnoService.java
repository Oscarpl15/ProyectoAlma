package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AlumnoDAO;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.util.Validador;

import java.time.LocalDate;
import java.util.List;

public class AlumnoService {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

    private void validarAlumno(Alumno alumno) throws Exception {
        if (alumno.getFechaNacimiento() != null && alumno.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new Exception("La fecha de nacimiento no puede ser futura.");
        }
        String dni = alumno.getDocumentoIdentidad();
        if (dni != null && !dni.isBlank() && !Validador.esDni(dni) && !Validador.esNie(dni)) {
            throw new Exception("El DNI/NIE introducido no es válido. Revisa las letras y números.");
        }
        String telefono = alumno.getTelefono();
        if (telefono != null && !telefono.isBlank() && !Validador.esTelefonoValido(telefono)) {
            throw new Exception("El teléfono no es válido. Debe tener 9 dígitos.");
        }
    }

    public void matricularNuevoAlumno(Alumno alumno, String curso, String grupo) throws Exception {
        validarAlumno(alumno);

        // Aplicamos la regla de negocio: Un alumno nuevo necesita una matrícula
        Matricula matricula = new Matricula(curso.trim(), alumno);
        if (grupo != null) {
            matricula.setGrupoAsignado(grupo.trim());
        }
        alumno.addMatricula(matricula);

        // Mandamos a guardar
        alumnoDAO.guardar(alumno);
    }

    public void actualizarAlumno(Alumno alumno) throws Exception {
        validarAlumno(alumno);
        alumnoDAO.actualizar(alumno);
    }

    public List<Alumno> obtenerTodos() {
        return alumnoDAO.obtenerTodos();
    }

    public Alumno obtenerCompleto(Integer id) {
        return alumnoDAO.obtenerCompleto(id);
    }
}
