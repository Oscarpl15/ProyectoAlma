package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "matriculas")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Formato: "2025-2026"
    @Column(name = "anyo_academico", nullable = false)
    private String anyoAcademico;

    // Formato: "3º Primaria"
    @Column(nullable = false)
    private String curso;

    @Column(name = "es_repeticion", nullable = false)
    private Boolean esRepeticion = false;

    // Relación Muchos a Uno con Alumno
    // FetchType.LAZY optimiza la memoria: no hace una consulta extra para traer todos los
    // datos del alumno a menos que llames explícitamente a matricula.getAlumno()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    // Constructor vacío obligatorio
    public Matricula() {}

    // Constructor con campos obligatorios
    public Matricula(String curso, Alumno alumno) {
        this.curso = curso;
        this.alumno = alumno;
        this.anyoAcademico = calcularAnyoAcademico();
    }

    // Getters y Setters

    public Integer getId() { return id; }

    public String getAnyoAcademico() { return anyoAcademico; }
    public void setAnyoAcademico(String anyoAcademico) { this.anyoAcademico = anyoAcademico; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    public Boolean getEsRepeticion() { return esRepeticion; }
    public void setEsRepeticion(Boolean esRepeticion) { this.esRepeticion = esRepeticion; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    //Metodo para automatizar el curso academico
    private String calcularAnyoAcademico() {
        LocalDate hoy = LocalDate.now();
        int anyoActual = hoy.getYear();
        int mesActual = hoy.getMonthValue();

        if (mesActual >= 6) {
            // De junio (6) a diciembre (12)
            return anyoActual + "-" + (anyoActual + 1);
        } else {
            // De enero (1) a mayo (5)
            return (anyoActual - 1) + "-" + anyoActual;
        }
    }
}
