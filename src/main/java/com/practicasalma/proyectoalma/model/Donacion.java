package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA que registra una donación realizada por un {@link Socio}.
 * <p>
 * Almacena la fecha, el importe (con dos decimales) y la forma de donación
 * (transferencia, efectivo, etc.). La relación con el socio es N:1.
 * El historial de donaciones se usa para calcular el importe total en el
 * certificado de donaciones.
 * </p>
 */
@Entity
@Table(name = "donaciones")
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @Column(name = "forma_donacion")
    private String formaDonacion; // Ej: "Domiciliación", "Transferencia", "Efectivo"

    // "Puntual", "Mensual", "Trimestral", "Anual"
    @Column(name = "periodicidad")
    private String periodicidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    public Donacion() {}

    public Donacion(LocalDate fecha, BigDecimal importe, String periodicidad, Socio socio) {
        this.fecha = fecha;
        this.importe = importe;
        this.periodicidad = periodicidad;
        this.socio = socio;
    }

    public Long getId() { return id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }

    public String getPeriodicidad() { return periodicidad; }
    public void setPeriodicidad(String periodicidad) { this.periodicidad = periodicidad; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }

    public String getFormaDonacion() { return formaDonacion; }
    public void setFormaDonacion(String formaDonacion) { this.formaDonacion = formaDonacion; }
}
