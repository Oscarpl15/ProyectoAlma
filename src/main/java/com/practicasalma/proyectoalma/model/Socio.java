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

    @Column(name = "cuenta_bancaria")
    private String cuentaBancaria;

    @Column(name = "cuota")
    private Double cuota;

    // "Puntual", "Mensual", "Trimestral", "Anual"
    @Column(name = "periodicidad")
    private String periodicidad;

    // Relación Uno a Muchos: Un socio puede tener un historial de muchas donaciones.
    // mappedBy = "socio" le dice a Hibernate que la relación la controla la clase Donacion.
    @OneToMany(mappedBy = "socio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Donacion> donaciones = new ArrayList<>();

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PeriodoActividad> periodosActividad = new ArrayList<>();

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
        this.setDni(dni);
        this.cuota = cuota;
    }

    // Getters y Setters

    public Double getCuota() { return cuota; }
    public void setCuota(Double cuota) { this.cuota = cuota; }

    public String getPeriodicidad() { return periodicidad; }
    public void setPeriodicidad(String periodicidad) { this.periodicidad = periodicidad; }

    public String getTipoEntidad() {return tipoEntidad;}
    public void setTipoEntidad(String tipoEntidad) {this.tipoEntidad = tipoEntidad;}

    public String getCuentaBancaria() {return cuentaBancaria;}
    public void setCuentaBancaria(String cuentaBancaria) {this.cuentaBancaria = cuentaBancaria;}

    public List<Donacion> getDonaciones() {return donaciones;}

    public List<PeriodoActividad> getPeriodosActividad() {return periodosActividad;}


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

    // Metodos para añadir o eliminar un nuevo periodo con sincronización bidireccional
    public void addPeriodo(PeriodoActividad periodo) {
        this.periodosActividad.add(periodo);
        periodo.setSocio(this);
    }

    public void removePeriodo(PeriodoActividad periodo) {
        this.periodosActividad.remove(periodo);
        periodo.setSocio(null);
    }
}
