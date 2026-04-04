package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;

@Entity
@Table(name = "personas_autorizadas")
public class PersonaAutorizada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    @Column(name = "documento_identidad", nullable = false)
    private String documentoIdentidad;

    @Column
    private String telefono;

    @Column(nullable = false)
    private String relacion; // Ej: "Padre", "Abuela", "Vecino"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    public PersonaAutorizada() {}

    public PersonaAutorizada(String nombre, String apellidos, String documentoIdentidad, String telefono, String relacion) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.documentoIdentidad = documentoIdentidad;
        this.telefono = telefono;
        this.relacion = relacion;
    }

    // Getters y Setters
    public Integer getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getRelacion() { return relacion; }
    public void setRelacion(String relacion) { this.relacion = relacion; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }
}
