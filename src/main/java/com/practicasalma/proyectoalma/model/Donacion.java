package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "donaciones")
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private Double importe;

    // Para diferenciar si es el pago de su cuota habitual o un extra
    @Column(name = "es_puntual", nullable = false)
    private Boolean esPuntual = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    public Donacion() {}

    public Donacion(LocalDate fecha, Double importe, Boolean esPuntual, Socio socio) {
        this.fecha = fecha;
        this.importe = importe;
        this.esPuntual = esPuntual;
        this.socio = socio;
    }

    // ... Getters y Setters ...


    public Integer getId() { return id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Double getImporte() { return importe; }
    public void setImporte(Double importe) { this.importe = importe; }

    public Boolean getEsPuntual() { return esPuntual; }
    public void setEsPuntual(Boolean esPuntual) { this.esPuntual = esPuntual; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }
}
