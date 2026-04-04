package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "personas_autorizadas")
public class PersonaAutorizada extends Persona {

    @Column(nullable = false)
    private String relacion; // Ej: "Padre", "Abuela", "Vecino"

    // Lado inverso de la relación con Alumno (la tabla intermedia la gestiona Alumno)
    @ManyToMany(mappedBy = "autorizadaRecoger")
    private List<Alumno> alumnos = new ArrayList<>();

    public PersonaAutorizada() {
        super();
    }

    public PersonaAutorizada(String nombre, String apellidos, String documentoIdentidad, String telefono, String relacion) {
        super();
        this.setNombre(nombre);
        this.setApellidos(apellidos);
        this.setDocumentoIdentidad(documentoIdentidad);
        this.setTelefono(telefono);
        this.relacion = relacion;
    }

    // Getters y Setters

    public String getRelacion() { return relacion; }
    public void setRelacion(String relacion) { this.relacion = relacion; }

    public List<Alumno> getAlumnos() { return alumnos; }
}
