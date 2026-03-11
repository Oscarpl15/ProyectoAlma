package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;

@Entity
@Table(name = "personas_autorizadas")
public class PersonaAutorizada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String documentoIdentidad;

    @Column(name = "tipo_documento", nullable = false)
    private String tipoDocumento; //DNI, NIE o Pasaporte

    @Column(nullable = false)
    private String relacion; // Ej: "Abuelo", "Tía", "Vecina"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    // Getters y Setters

    public Integer getId() {return id;}

    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getDocumentoIdentidad() {return documentoIdentidad;}
    public void setDocumentoIdentidad(String documentoIdentidad) {this.documentoIdentidad = documentoIdentidad;}

    public String getTipoDocumento() {return tipoDocumento;}
    public void setTipoDocumento(String tipoDocumento) {this.tipoDocumento = tipoDocumento;}

    public String getRelacion() {return relacion;}
    public void setRelacion(String relacion) {this.relacion = relacion;}

    public Alumno getAlumno() {return alumno;}
    public void setAlumno(Alumno alumno) {this.alumno = alumno;}
}
