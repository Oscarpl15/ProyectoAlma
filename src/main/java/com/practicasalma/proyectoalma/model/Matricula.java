package com.practicasalma.proyectoalma.model;

import com.practicasalma.proyectoalma.util.UtilFecha;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entidad JPA que representa la inscripción de un alumno en un curso académico concreto.
 * <p>
 * Cada matrícula pertenece a exactamente un {@link Alumno} y registra el año académico
 * (formato {@code "AAAA/AAAA+1"}), el nivel educativo (p. ej., "3º Primaria"),
 * si es repetición y el grupo asignado (p. ej., "G1 Lunes").
 * Un alumno puede tener varias matrículas (una por curso académico en que estuvo activo).
 * </p>
 */
@Entity
@Table(name = "matriculas")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anyo_academico", nullable = false)
    private String anyoAcademico;

    @Column(nullable = false)
    private String curso;

    @Column(name = "es_repeticion", nullable = false)
    private Boolean esRepeticion = false;

    @Column(name = "grupo_asignado")
    private String grupoAsignado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    public Matricula() {}

    public Matricula(String curso, Alumno alumno) {
        this.curso = curso;
        this.alumno = alumno;
        this.anyoAcademico = UtilFecha.calcularCursoAcademico();
    }

    // Getters y Setters

    public Long getId() { return id; }

    public String getAnyoAcademico() { return anyoAcademico; }
    public void setAnyoAcademico(String anyoAcademico) { this.anyoAcademico = anyoAcademico; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    public Boolean getEsRepeticion() { return esRepeticion; }
    public void setEsRepeticion(Boolean esRepeticion) { this.esRepeticion = esRepeticion; }

    public String getGrupoAsignado() {return grupoAsignado;}
    public void setGrupoAsignado(String grupoAsignado) {this.grupoAsignado = grupoAsignado;}

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

}
