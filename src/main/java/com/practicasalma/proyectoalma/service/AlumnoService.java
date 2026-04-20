package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AlumnoDAO;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.util.UtilFecha;
import com.practicasalma.proyectoalma.util.Validador;

import java.time.LocalDate;
import java.util.List;

public class AlumnoService {

    private static final List<String> CURSOS_PRIMARIA = List.of(
            "1º Infantil", "2º Infantil", "3º Infantil",
            "1º Primaria", "2º Primaria", "3º Primaria",
            "4º Primaria", "5º Primaria", "6º Primaria"
    );

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

    public Alumno obtenerCompleto(Long id) {
        return alumnoDAO.obtenerCompleto(id);
    }

    public void darDeBaja(Long id) throws Exception {
        Alumno alumno = alumnoDAO.obtenerCompleto(id);
        alumno.setActivo(false);
        alumnoDAO.actualizar(alumno);
    }

    public void darDeAlta(Long id) throws Exception {
        Alumno alumno = alumnoDAO.obtenerCompleto(id);
        alumno.setActivo(true);
        String anyoNuevo = UtilFecha.calcularCursoAcademico();
        boolean yaMatriculado = alumno.getMatriculas().stream()
                .anyMatch(m -> anyoNuevo.equals(m.getAnyoAcademico()));
        if (!yaMatriculado) {
            Matricula nueva = new Matricula(calcularCursoParaAlta(alumno), alumno);
            alumno.addMatricula(nueva);
        }
        alumnoDAO.actualizar(alumno);
    }

    private String calcularCursoParaAlta(Alumno alumno) {
        List<Matricula> matriculas = alumno.getMatriculas();
        if (matriculas == null || matriculas.isEmpty()) {
            return CURSOS_PRIMARIA.get(0);
        }
        Matricula ultima = matriculas.get(matriculas.size() - 1);
        int indice = CURSOS_PRIMARIA.indexOf(ultima.getCurso());
        if (indice == -1) indice = 0;
        try {
            int anyoUltimo = Integer.parseInt(ultima.getAnyoAcademico().split("/")[0]);
            int anyoActual = Integer.parseInt(UtilFecha.calcularCursoAcademico().split("/")[0]);
            indice = Math.min(indice + Math.max(0, anyoActual - anyoUltimo), CURSOS_PRIMARIA.size() - 1);
        } catch (NumberFormatException ignored) {
            indice = Math.min(indice + 1, CURSOS_PRIMARIA.size() - 1);
        }
        return CURSOS_PRIMARIA.get(indice);
    }
}
