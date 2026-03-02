package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Entity
@Table(name = "alumnos")
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellidos;
    private String rutaFoto = "/com/practicasalma/proyectoalma/assets/default.png";
    public Alumno() {} // Hibernate necesita un constructor vacío

    public Alumno(String nombre){
        this.nombre = nombre;
        this.rutaFoto = rutaFoto; // Inicializo
    }


    public String getRutaFoto() {
        return rutaFoto;
    }

    public void setRutaFoto(String rutaFoto) {
        this.rutaFoto = rutaFoto;
    }
}
