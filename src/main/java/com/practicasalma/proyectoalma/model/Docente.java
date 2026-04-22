package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa a un docente (educador remunerado) de la fundación.
 * <p>
 * Añade a {@link Persona}: fecha de nacimiento, certificados legales obligatorios
 * (certificado de delitos sexuales, LOPD), titulación y disponibilidad.
 * </p>
 * <p>
 * Relaciones:
 * <ul>
 *   <li>{@code historialAsignaciones} — 1:N con {@link AsignacionPersonal}; un registro por curso académico.</li>
 *   <li>{@code periodosActividad} — 1:N con {@link PeriodoActividad}.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "docentes")
public class Docente extends Persona {

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    // Certificado de Delitos Sexuales
    @Column(name = "cert_delitos_sexuales", nullable = false)
    private Boolean autoDelitosSexuales = false;

    @Column(name = "ruta_doc_delitos")
    private String rutaDocDelitosSexuales;

    // Ley de Protección de Datos
    @Column(name = "cert_proteccion_datos", nullable = false)
    private Boolean autoProteccionDatos = false;

    @Column(name = "ruta_doc_leypd")
    private String rutaDocProteccionDatos;

    private String titulacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AsignacionPersonal> historialAsignaciones = new ArrayList<>();

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PeriodoActividad> periodosActividad = new ArrayList<>();


    // Constructor vacío obligatorio para Hibernate
    public Docente() {
        super();
    }

    // Constructor para instanciar desde el controlador JavaFX
    public Docente(String nombre, String apellidos, String direccion, String dni,String telefono, String correo, LocalDate fechaNacimiento) {
        super(nombre, apellidos, direccion);
        this.setDocumentoIdentidad(dni);
        this.setTelefono(telefono);
        this.setCorreo(correo);
        this.fechaNacimiento = fechaNacimiento;
    }

    // Getters y Setters
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public Boolean getAutoDelitosSexuales() { return autoDelitosSexuales; }
    public void setAutoDelitosSexuales(Boolean autoDelitosSexuales) { this.autoDelitosSexuales = autoDelitosSexuales; }

    public String getRutaDocDelitosSexuales() { return rutaDocDelitosSexuales; }
    public void setRutaDocDelitosSexuales(String rutaDocDelitosSexuales) { this.rutaDocDelitosSexuales = rutaDocDelitosSexuales; }

    public Boolean getAutoProteccionDatos() { return autoProteccionDatos; }
    public void setAutoProteccionDatos(Boolean autoProteccionDatos) { this.autoProteccionDatos = autoProteccionDatos; }

    public String getRutaDocProteccionDatos() { return rutaDocProteccionDatos; }
    public void setRutaDocProteccionDatos(String rutaDocProteccionDatos) { this.rutaDocProteccionDatos = rutaDocProteccionDatos; }

    public String getTitulacion() {return titulacion;}
    public void setTitulacion(String titulacion) {this.titulacion = titulacion;}

    public Boolean getActivo() {return activo;}
    public void setActivo(Boolean activo) {this.activo = activo;}

    public List<AsignacionPersonal> getHistorialAsignaciones() {return historialAsignaciones;}

    public List<PeriodoActividad> getPeriodosActividad() {return periodosActividad;}

    //  Metodo añadir y eliminar con sincronización bidireccional
    public void addAsignacion(AsignacionPersonal asignacion) {
        this.historialAsignaciones.add(asignacion);
        asignacion.setDocente(this);
    }

    public void removeAsignacion(AsignacionPersonal asignacion) {
        this.historialAsignaciones.remove(asignacion);
        asignacion.setDocente(null);
    }

    // Metodos para añadir o eliminar un nuevo periodo con sincronización bidireccional
    public void addPeriodo(PeriodoActividad periodo) {
        this.periodosActividad.add(periodo);
        periodo.setDocente(this);
    }

    public void removePeriodo(PeriodoActividad periodo) {
        this.periodosActividad.remove(periodo);
        periodo.setDocente(null);
    }
}
