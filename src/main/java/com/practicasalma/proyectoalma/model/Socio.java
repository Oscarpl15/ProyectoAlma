package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "socios")
public class Socio extends Persona {

    // "Física", "Empresa" o "Asociación"
    @Column(name = "tipo_entidad")
    private String tipoEntidad;

    @Column(name = "cuota")
    private Double cuota;

    // "Puntual", "Mensual", "Trimestral", "Anual"
    @Column(name = "periodicidad")
    private String periodicidad;

    // Relación Uno a Muchos: Un socio puede tener un historial de muchas donaciones.
    // mappedBy = "socio" le dice a Hibernate que la relación la controla la clase Donacion.
    @OneToMany(mappedBy = "socio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Donacion> donaciones = new ArrayList<>();

    // Constructor vacío obligatorio para Hibernate
    public Socio() {
        super();
    }

    // Constructor para socios ESPORÁDICOS (sin cuota fija ni periodicidad)
    public Socio(String nombre, String apellidos, String direccion, String dni, String tipoEntidad) {
        super(nombre, apellidos, direccion);
        this.setDni(dni);
        this.tipoEntidad = tipoEntidad;
        this.cuota = 0.0; 
        this.periodicidad = "Ninguna";
    }

    //Constructor para socios con cuota periodica
    public Socio(String nombre, String apellidos, String direccion, String dni,
                 String tipoEntidad, Double cuotaFija, String periodicidad) {
        super(nombre, apellidos, direccion);
        this.setDni(dni); // El DNI sigue siendo obligatorio
        this.tipoEntidad = tipoEntidad;
        this.cuota = cuotaFija;
        this.periodicidad = periodicidad;
    }

    public Socio(String nombre, String apellidos, String direccion, String dni,  Double cuota) {
        super(nombre, apellidos, direccion);
        this.cuota = cuota;
    }

    // Getters y Setters

    public Double getCuota() { return cuota; }
    public void setCuota(Double cuota) { this.cuota = cuota; }

    public String getPeriodicidad() { return periodicidad; }
    public void setPeriodicidad(String periodicidad) { this.periodicidad = periodicidad; }

    //  Métodos de sincronización bidireccional

    public void addDonacion(Donacion donacion) {
        this.donaciones.add(donacion);
        // Sincronizamos el otro lado de la relación en memoria
        donacion.setSocio(this);
    }

    public void removeDonacion(Donacion donacion) {
        this.donaciones.remove(donacion);
        donacion.setSocio(null);
    }
}
