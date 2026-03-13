package com.practicasalma.proyectoalma.model;

import com.practicasalma.proyectoalma.util.UtilFecha;
import jakarta.persistence.*;

@Entity
@Table(name = "asignaciones_personal")
public class AsignacionPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //"2025-2026"
    @Column(name = "anyo_academico", nullable = false)
    private String anyoAcademico;

    // "Apoyo 3º Primaria", "Refuerzo Lectura", "Taller de Verano"  queda pendiente de preguntar a Lidia
    @Column(name = "grupo_asignado", nullable = false)
    private String grupoAsignado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id") // No le ponemos nullable = false para que uno pueda estar vacío
    private Docente docente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voluntario_id")
    private Voluntario voluntario;

    // Constructor vacío para Hibernate
    public AsignacionPersonal() {}

    // Constructor para cuando se asigna a un DOCENTE
    public AsignacionPersonal( String grupoAsignado, Docente docente) {
        this.anyoAcademico = UtilFecha.calcularCursoAcademico();
        this.grupoAsignado = grupoAsignado;
        this.docente = docente;
        this.voluntario = null; // Nos aseguramos de que el otro quede vacío
    }

    // Constructor para cuando se asigna a un VOLUNTARIO
    public AsignacionPersonal( String grupoAsignado, Voluntario voluntario) {
        this.anyoAcademico = UtilFecha.calcularCursoAcademico();
        this.grupoAsignado = grupoAsignado;
        this.voluntario = voluntario;
        this.docente = null;
    }

    // Getters y Setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getAnyoAcademico() { return anyoAcademico; }
    public void setAnyoAcademico(String anyoAcademico) { this.anyoAcademico = anyoAcademico; }

    public String getGrupoAsignado() { return grupoAsignado; }
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }

    public Docente getDocente() {return docente;}
    public void setDocente(Docente docente) {this.docente = docente;}

    public Voluntario getVoluntario() {return voluntario;}
    public void setVoluntario(Voluntario voluntario) {this.voluntario = voluntario;}
}
