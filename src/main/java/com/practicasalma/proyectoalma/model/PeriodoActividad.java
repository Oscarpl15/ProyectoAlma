package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "periodos_actividad")
public class PeriodoActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDate fechaAlta;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    @Column(name = "motivo_baja")
    private String motivoBaja;

    // Claves Foráneas (Solo se llenará una por registro)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id")
    private Socio socio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voluntario_id")
    private Voluntario voluntario;

    // Constructor vacío (Obligatorio para Hibernate)
    public PeriodoActividad() {}

    // Constructores sobrecargados

    public PeriodoActividad(LocalDate fechaAlta, Alumno alumno) {
        this.fechaAlta = fechaAlta;
        this.alumno = alumno;
    }

    public PeriodoActividad(LocalDate fechaAlta, Socio socio) {
        this.fechaAlta = fechaAlta;
        this.socio = socio;
    }

    public PeriodoActividad(LocalDate fechaAlta, Docente docente) {
        this.fechaAlta = fechaAlta;
        this.docente = docente;
    }

    public PeriodoActividad(LocalDate fechaAlta, Voluntario voluntario) {
        this.fechaAlta = fechaAlta;
        this.voluntario = voluntario;
    }

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }

    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }

    public String getMotivoBaja() { return motivoBaja; }
    public void setMotivoBaja(String motivoBaja) { this.motivoBaja = motivoBaja; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public Voluntario getVoluntario() { return voluntario; }
    public void setVoluntario(Voluntario voluntario) { this.voluntario = voluntario; }
}
