package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tutores")
public class Tutor extends Persona {

    // Lado inverso de la relación Muchos a Muchos con Alumno.
    // "mappedBy" le dice a Hibernate que busque la variable "tutores" en la clase Alumno
    // para saber cómo está configurada la tabla intermedia.
    @ManyToMany(mappedBy = "tutores")
    private List<Alumno> alumnos = new ArrayList<>();

    // Constructor vacío obligatorio para Hibernate
    public Tutor() {
        super();
    }

    // Constructor para instanciar desde el controlador JavaFX
    public Tutor(String nombre, String apellidos, String direccion, String dni) {
        super(nombre, apellidos, direccion);
        this.setDni(dni); //Esto solo si fuese obligatorio el dni en tutor, si no lo es lo quitariamos.
    }

    // Getters y setters

    public List<Alumno> getAlumnos() { return alumnos; }

    // Métodos de sincronización bidireccional

    public void addAlumno(Alumno alumno) {
        this.alumnos.add(alumno);
        // Mantiene la coherencia en memoria: si añado un alumno al tutor, el tutor se añade al alumno
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
