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

    @Column(name = "auto_uso_datos")
    private Boolean autorizaUsoDatos = false;

    @Column(name = "auto_actividades")
    private Boolean autorizaActividades = false;

    @Column(name = "auto_comunicaciones")
    private Boolean autorizaComunicaciones = false;

    @Column(name = "auto_imagen")
    private Boolean autorizaImagen = false;

    @Column(name = "auto_irse_solo")
    private Boolean autorizaIrseSolo = false;

    @Column(name = "ruta_doc_autoriza")
    private String rutaDocAutoriza;


    //Otros datos
    @Column(name = "colegio")
    private String colegio;

    @Column(name = "seguimiento_servicios_sociales") //check
    private Boolean seguimientoServiciosSociales = false;

    @Column(name = "seguimiento_saf")  // check
    private Boolean seguimientoSaf = false;

    @Column(name = "derivacion_ss")
    private Boolean derivacionSS = false;

    @Column(name = "derivacion_saf")
    private Boolean derivacionSaf = false;

    @Column(name = "derivacion_eoep")
    private Boolean derivacionEoep = false;

    @Column(name = "derivacion_colegio")
    private Boolean derivacionColegio = false;

    @Column(name = "derivacion_otro")
    private Boolean derivacionOtro = false;

    @Column(name = "derivado_por")
    private String derivadoPor;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

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
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "alumno_persona_autorizada",
            joinColumns = @JoinColumn(name = "alumno_id"),
            inverseJoinColumns = @JoinColumn(name = "persona_autorizada_id")
    )
    private List<PersonaAutorizada> autorizadaRecoger = new ArrayList<>();

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

    public Boolean getAutorizaUsoDatos() {return autorizaUsoDatos;}
    public void setAutorizaUsoDatos(Boolean autorizaUsoDatos) {this.autorizaUsoDatos = autorizaUsoDatos;}

    public Boolean getAutorizaActividades() {return autorizaActividades;}
    public void setAutorizaActividades(Boolean autorizaActividades) {this.autorizaActividades = autorizaActividades;}

    public Boolean getAutorizaComunicaciones() {return autorizaComunicaciones;}
    public void setAutorizaComunicaciones(Boolean autorizaComunicaciones) {this.autorizaComunicaciones = autorizaComunicaciones;}

    public Boolean getAutorizaIrseSolo() {return autorizaIrseSolo;}
    public void setAutorizaIrseSolo(Boolean autorizaIrseSolo) {this.autorizaIrseSolo = autorizaIrseSolo;}

    public String getRutaDocAutoriza() {return rutaDocAutoriza;}
    public void setRutaDocAutoriza(String rutaDocAutoriza) {this.rutaDocAutoriza = rutaDocAutoriza;}

    public Boolean getActivo() {return activo;}
    public void setActivo(Boolean activo) {this.activo = activo;}

    public Integer getNumRepeticionesPrevias() {return numRepeticionesPrevias;}
    public void setNumRepeticionesPrevias(Integer numRepeticionesPrevias) {this.numRepeticionesPrevias = numRepeticionesPrevias;}

    public String getDetalleCursosRepetidos() {return detalleCursosRepetidos;}
    public void setDetalleCursosRepetidos(String detalleCursosRepetidos) {this.detalleCursosRepetidos = detalleCursosRepetidos;}

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

    public Boolean getDerivacionSS() {return derivacionSS;}
    public void setDerivacionSS(Boolean derivacionSS) {this.derivacionSS = derivacionSS;}

    public Boolean getDerivacionSaf() {return derivacionSaf;}
    public void setDerivacionSaf(Boolean derivacionSaf) {this.derivacionSaf = derivacionSaf;}

    public Boolean getDerivacionEoep() {return derivacionEoep;}
    public void setDerivacionEoep(Boolean derivacionEoep) {this.derivacionEoep = derivacionEoep;}

    public Boolean getDerivacionColegio() {return derivacionColegio;}
    public void setDerivacionColegio(Boolean derivacionColegio) {this.derivacionColegio = derivacionColegio;}

    public Boolean getDerivacionOtro() {return derivacionOtro;}
    public void setDerivacionOtro(Boolean derivacionOtro) {this.derivacionOtro = derivacionOtro;}

    public String getDerivadoPor() {return derivadoPor;}
    public void setDerivadoPor(String derivadoPor) {this.derivadoPor = derivadoPor;}

    public List<PeriodoActividad> getPeriodosActividad() {return periodosActividad;}

    public List<PersonaAutorizada> getAutorizadaRecoger() {return autorizadaRecoger;}

    public void addPersonaAutorizada(PersonaAutorizada pa) {
        this.autorizadaRecoger.add(pa);
        if (!pa.getAlumnos().contains(this)) {
            pa.getAlumnos().add(this);
        }
    }

    public void removePersonaAutorizada(PersonaAutorizada pa) {
        this.autorizadaRecoger.remove(pa);
        pa.getAlumnos().remove(this);
    }

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
