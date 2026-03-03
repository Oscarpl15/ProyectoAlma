package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alumnos")
public class Alumno extends Persona {

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    // Rutas específicas para el alumno (OneDrive)
    @Column(name = "ruta_foto_perfil")
    private String rutaFotoPerfil;

    // Uso de Imagen
    @Column(name = "auto_imagen", nullable = false)
    private Boolean autorizaImagen = false;

    @Column(name = "ruta_doc_imagen")
    private String rutaDocImagen;

    // Salidas Extraescolares
    @Column(name = "auto_salidas", nullable = false)
    private Boolean autorizaSalidas = false;

    @Column(name = "ruta_doc_salidas")
    private String rutaDocSalidas;

    // Recogida por Terceros
    @Column(name = "auto_irseSolo", nullable = false)
    private Boolean autorizaRecogida = false;

    @Column(name = "ruta_doc_irseSolo")
    private String rutaDocRecogida;

    //Otros datos
    @Column(name = "colegio")
    private String colegio;

    @Column(name = "seguimiento_servicios_sociales")
    private Boolean seguimientoServiciosSociales = false;

    @Column(name = "seguimiento_saf")
    private Boolean seguimientoSaf = false;

    // Para el cálculo matemático de la baja automática
    @Column(name = "num_repeticiones_previas", nullable = false)
    private Integer numRepeticionesPrevias = 0;

    // Información descriptiva en la ficha del alumno
    @Column(name = "detalle_cursos_repetidos")
    private String detalleCursosRepetidos;

    // Relación Uno a Muchos con Matricula
    // mappedBy indica que la clave foránea está en la clase Matricula (atributo "alumno")
    // cascade = CascadeType.ALL y orphanRemoval = true aseguran que si borras un alumno, se borran sus matrículas
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matricula> matriculas = new ArrayList<>();

    // Relación Muchos a Muchos con PadreTutor
    // @JoinTable fuerza a crear la tabla intermedia "alumno_tutor"
    @ManyToMany
    @JoinTable(
            name = "alumno_tutor",
            joinColumns = @JoinColumn(name = "alumno_id"),
            inverseJoinColumns = @JoinColumn(name = "tutor_id")
    )
    private List<Tutor> tutores = new ArrayList<>();

    // Relación recursiva Muchos a Muchos (Hermanos/Primos)
    // @JoinTable fuerza a crear la tabla intermedia "alumno_familiar" uniendo dos IDs de la misma tabla
    @ManyToMany
    @JoinTable(
            name = "alumno_familiar",
            joinColumns = @JoinColumn(name = "alumno_id"),
            inverseJoinColumns = @JoinColumn(name = "familiar_id")
    )
    private List<Alumno> familiares = new ArrayList<>();

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PeriodoActividad> periodosActividad = new ArrayList<>();

    // Constructor vacío obligatorio para Hibernate
    public Alumno() {
        super(); 
    }

    //Constructor normal
    public Alumno(String nombre, String apellidos, String direccion, LocalDate fechaNacimiento) {
        super(nombre, apellidos, direccion);
        this.fechaNacimiento = fechaNacimiento;
    }

    //Getters y Setters

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getRutaFotoPerfil() { return rutaFotoPerfil; }
    public void setRutaFotoPerfil(String rutaFotoPerfil) { this.rutaFotoPerfil = rutaFotoPerfil; }

    public Boolean getAutorizaImagen() { return autorizaImagen; }
    public void setAutorizaImagen(Boolean autorizaImagen) { this.autorizaImagen = autorizaImagen; }

    public String getRutaDocImagen() { return rutaDocImagen; }
    public void setRutaDocImagen(String rutaDocImagen) { this.rutaDocImagen = rutaDocImagen; }

    public Boolean getAutorizaSalidas() { return autorizaSalidas; }
    public void setAutorizaSalidas(Boolean autorizaSalidas) { this.autorizaSalidas = autorizaSalidas; }

    public String getRutaDocSalidas() { return rutaDocSalidas; }
    public void setRutaDocSalidas(String rutaDocSalidas) { this.rutaDocSalidas = rutaDocSalidas; }

    public Boolean getAutorizaRecogida() { return autorizaRecogida; }
    public void setAutorizaRecogida(Boolean autorizaRecogida) { this.autorizaRecogida = autorizaRecogida; }

    public String getRutaDocRecogida() {return rutaDocRecogida;}
    public void setRutaDocRecogida(String rutaDocRecogida) { this.rutaDocRecogida = rutaDocRecogida; }

    public List<Matricula> getMatriculas() { return matriculas; }

    public List<Tutor> getTutores() { return tutores; }

    public List<Alumno> getFamiliares() { return familiares; }
    public void setFamiliares(List<Alumno> familiares) { this.familiares = familiares; }

    public String getColegio() {return colegio;}
    public void setColegio(String colegio) {this.colegio = colegio;}

    public Boolean getSeguimientoServiciosSociales() {return seguimientoServiciosSociales;}
    public void setSeguimientoServiciosSociales(Boolean seguimientoServiciosSociales) {this.seguimientoServiciosSociales = seguimientoServiciosSociales;}

    public Boolean getSeguimientoSaf() {return seguimientoSaf;}
    public void setSeguimientoSaf(Boolean seguimientoSaf) {this.seguimientoSaf = seguimientoSaf;}

    public List<PeriodoActividad> getPeriodosActividad() {return periodosActividad;}

    // Métodos para añadir o eliminar tutores o matrículas a un alumno

    public void addTutor(Tutor tutor) {
        this.tutores.add(tutor);
    }

    public void removeTutor(Tutor tutor) {
        this.tutores.remove(tutor);
    }

    public void addMatricula(Matricula matricula) {
        this.matriculas.add(matricula);
        matricula.setAlumno(this); // Mantiene la sincronización bidireccional
    }

    public void removeMatricula(Matricula matricula) {
        this.matriculas.remove(matricula);
        matricula.setAlumno(null); // Obligatorio para que Hibernate entienda la desvinculación
    }

    // Metodos para añadir o eliminar un nuevo periodo con sincronización bidireccional
    public void addPeriodo(PeriodoActividad periodo) {
        this.periodosActividad.add(periodo);
        periodo.setAlumno(this);
    }

    public void removePeriodo(PeriodoActividad periodo) {
        this.periodosActividad.remove(periodo);
        periodo.setAlumno(null);
    }

}
