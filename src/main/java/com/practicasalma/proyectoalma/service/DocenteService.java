package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AsignacionPersonalDAO;
import com.practicasalma.proyectoalma.dao.DocenteDAO;
import com.practicasalma.proyectoalma.model.AsignacionPersonal;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.util.UtilFecha;

import java.util.List;

public class DocenteService {

    private final DocenteDAO docenteDAO = new DocenteDAO();

    public void guardarDocente(Docente docente) throws Exception {
        String anyo = UtilFecha.calcularCursoAcademicoPersonal();
        boolean yaExiste = docente.getHistorialAsignaciones().stream()
                .anyMatch(a -> anyo.equals(a.getAnyoAcademico())
                        || anyo.replace("/", "-").equals(a.getAnyoAcademico()));
        if (!yaExiste) {
            AsignacionPersonal asig = new AsignacionPersonal(docente, anyo);
            docente.addAsignacion(asig);
        }
        docenteDAO.guardar(docente);
    }

    public Docente obtenerCompleto(Long id) {
        return docenteDAO.obtenerCompleto(id);
    }

    public void actualizarDocente(Docente docente) throws Exception {
        docenteDAO.actualizar(docente);
    }

    public void actualizarActivo(Long id, boolean activo) throws Exception {
        docenteDAO.actualizarActivo(id, activo);
    }

    public void registrarContinuacion(Docente docente, String anyoAcademico) throws Exception {
        AsignacionPersonalDAO asignacionDAO = new AsignacionPersonalDAO();
        asignacionDAO.guardar(new AsignacionPersonal(docente, anyoAcademico));
    }

    public void darDeBaja(Long id) throws Exception {
        docenteDAO.actualizarActivo(id, false);
    }

    public void darDeAlta(Long id) throws Exception {
        docenteDAO.actualizarActivo(id, true);
        Docente docente = docenteDAO.obtenerCompleto(id);
        String anyo = UtilFecha.calcularCursoAcademicoPersonal();
        boolean yaExiste = docente.getHistorialAsignaciones() != null &&
                docente.getHistorialAsignaciones().stream()
                        .anyMatch(a -> anyo.equals(a.getAnyoAcademico()));
        if (!yaExiste) {
            registrarContinuacion(docente, anyo);
        }
    }

    public List<Docente> obtenerTodos() {
        return docenteDAO.obtenerTodos();
    }
}
