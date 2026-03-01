package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;

@Entity
@Table(name = "socios")
public class Socio extends Persona {

    @Column(name = "cuota")
    private Double cuota;

    // "Puntual", "Mensual", "Trimestral", "Anual"
    @Column(name = "periodicidad")
    private String periodicidad;

    // Constructor vacío obligatorio para Hibernate
    public Socio() {
        super();
    }

    // Constructor para instanciar desde el controlador JavaFX
    public Socio(String nombre, String apellidos, String direccion, String dni) {
        super(nombre, apellidos, direccion);
        this.setDni(dni);
    }

    // Getters y Setters

    public Double getCuota() { return cuota; }
    public void setCuota(Double cuota) { this.cuota = cuota; }

    public String getPeriodicidad() { return periodicidad; }
    public void setPeriodicidad(String periodicidad) { this.periodicidad = periodicidad; }
}
