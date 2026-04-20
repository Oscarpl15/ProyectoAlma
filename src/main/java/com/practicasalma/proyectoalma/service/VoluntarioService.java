package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AsignacionPersonalDAO;
import com.practicasalma.proyectoalma.dao.VoluntarioDAO;
import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Voluntario;
import com.practicasalma.proyectoalma.util.UtilFecha;

import java.util.List;

public class VoluntarioService {

    private final VoluntarioDAO voluntarioDAO = new VoluntarioDAO();

    public void guardarVoluntario(Voluntario voluntario) throws Exception {
        String anyo = UtilFecha.calcularCursoAcademicoPersonal();
        boolean yaExiste = voluntario.getHistorialAsignaciones().stream()
                .anyMatch(a -> anyo.equals(a.getAnyoAcademico())
                        || anyo.replace("/", "-").equals(a.getAnyoAcademico()));
        if (!yaExiste) {
            AsignacionPersonal asig = new AsignacionPersonal(voluntario, anyo);
            voluntario.addAsignacion(asig);
        }
        voluntarioDAO.guardar(voluntario);
    }

    public Voluntario obtenerCompleto(Long id) {
        return voluntarioDAO.obtenerCompleto(id);
    }

    public void actualizarVoluntario(Voluntario voluntario) throws Exception {
        voluntarioDAO.actualizar(voluntario);
    }

    public void actualizarActivo(Long id, boolean activo) throws Exception {
        voluntarioDAO.actualizarActivo(id, activo);
    }

    public void registrarContinuacion(Voluntario voluntario, String anyoAcademico) throws Exception {
        AsignacionPersonalDAO asignacionDAO = new AsignacionPersonalDAO();
        asignacionDAO.guardar(new AsignacionPersonal(voluntario, anyoAcademico));
    }

    public List<Voluntario> obtenerTodos() {
        return voluntarioDAO.obtenerTodos();
    }
}
