package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa a una persona autorizada a recoger a uno o varios alumnos.
 * <p>
 * Extiende {@link Persona} añadiendo el campo {@code relacion} (p. ej., "Padre", "Abuela").
 * La asociación con alumnos es N:M gestionada desde el lado de {@link Alumno}
 * mediante la tabla {@code alumno_persona_autorizada}.
 * </p>
 */
@Entity
@Table(name = "personas_autorizadas")
public class PersonaAutorizada extends Persona {

    @Column(nullable = false)
    private String relacion;

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

    public String getRelacion() { return relacion; }
    public void setRelacion(String relacion) { this.relacion = relacion; }

    public List<Alumno> getAlumnos() { return alumnos; }
}
