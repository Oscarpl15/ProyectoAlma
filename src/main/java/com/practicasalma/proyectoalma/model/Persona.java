package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase base abstracta de todas las personas del sistema.
 * <p>
 * Define los campos comunes: identificación ({@code documentoIdentidad}, {@code tipoDocumento}),
 * datos de contacto, dirección y estado activo/baja. Cada subclase ({@link Alumno},
 * {@link Docente}, {@link Socio}, {@link Voluntario}) genera su propia tabla en SQLite
 * gracias a la estrategia {@code TABLE_PER_CLASS} implícita de {@code @MappedSuperclass}.
 * </p>
 * <p>
 * Nota: {@code documentoIdentidad} tiene restricción {@code unique = true} a nivel de tabla,
 * por lo que el mismo DNI puede existir en tablas distintas (un Socio y un Docente pueden
 * compartir DNI sin infringir la restricción).
 * </p>
 */
@MappedSuperclass
public abstract class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    @Column(name = "documento_identidad", unique = true)
    private String documentoIdentidad;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    private String direccion;
    private String ciudad;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    private String telefono;
    private String correo;

    @Column(name = "fecha_alta")
    private LocalDate fechaAlta;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public Persona() {
        this.fechaAlta = LocalDate.now();
    }

    public Persona(String nombre, String apellidos, String direccion) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.direccion = direccion;
        this.fechaAlta = LocalDate.now();
    }

    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public LocalDate getFechaAlta() { return fechaAlta; }

    public LocalDate getFechaBaja() { return fechaBaja; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getDocumentoIdentidad() {return documentoIdentidad;}
    public void setDocumentoIdentidad(String documentoIdentidad) {this.documentoIdentidad = documentoIdentidad;}

    public String getTipoDocumento() {return tipoDocumento;}
    public void setTipoDocumento(String tipoDocumento) {this.tipoDocumento = tipoDocumento;}

    public String getCiudad() {return ciudad;}
    public void setCiudad(String ciudad) {this.ciudad = ciudad;}

    public String getCodigoPostal() {return codigoPostal;}
    public void setCodigoPostal(String codigoPostal) {this.codigoPostal = codigoPostal;}

    @Column
    private String nacionalidad;

    @Column
    private String genero;

    @Column(name = "ruta_foto_perfil")
    private String rutaFotoPerfil;

    public String getNacionalidad() {return nacionalidad;}
    public void setNacionalidad(String nacionalidad) {this.nacionalidad = nacionalidad;}

    public String getGenero() {return genero;}
    public void setGenero(String genero) {this.genero = genero;}

    public String getRutaFotoPerfil() {return rutaFotoPerfil;}
    public void setRutaFotoPerfil(String rutaFotoPerfil) {this.rutaFotoPerfil = rutaFotoPerfil;}
}