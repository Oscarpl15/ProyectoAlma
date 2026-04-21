package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa al tutor legal de uno o varios alumnos.
 * <p>
 * Extiende {@link Persona} añadiendo el campo {@code relacion} (por defecto "Tutor/a Legal").
 * Un mismo tutor puede estar vinculado a varios alumnos (p. ej., hermanos). La relación N:M
 * con {@link Alumno} es gestionada desde el lado del alumno mediante la tabla {@code alumno_tutor}.
 * </p>
 */
@Entity
@Table(name = "tutores")
public class Tutor extends Persona {

    @Column(name = "relacion", nullable = false)
    private String relacion = "Tutor/a Legal";

    @ManyToMany(mappedBy = "tutores")
    private List<Alumno> alumnos = new ArrayList<>();

    public Tutor() {
        super();
    }

    public Tutor(String nombre, String apellidos, String direccion, String dni) {
        super(nombre, apellidos, direccion);
        this.setDocumentoIdentidad(dni);
    }

    public String getRelacion() { return relacion; }
    public void setRelacion(String relacion) { this.relacion = relacion; }

    public List<Alumno> getAlumnos() { return alumnos; }

    public void addAlumno(Alumno alumno) {
        this.alumnos.add(alumno);
        if (!alumno.getTutores().contains(this)) {
            alumno.getTutores().add(this);
        }
    }

    public void removeAlumno(Alumno alumno) {
        this.alumnos.remove(alumno);
        if (alumno.getTutores().contains(this)) {
            alumno.getTutores().remove(this);
        }
    }
}
