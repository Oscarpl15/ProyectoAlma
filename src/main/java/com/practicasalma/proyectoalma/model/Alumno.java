package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa a un alumno de la fundación.
 * <p>
 * Extiende {@link Persona} añadiendo datos propios del alumno:
 * fecha de nacimiento, autorizaciones parentales (imagen, actividades, irse solo, etc.),
 * información de seguimiento social (SAF, SS) y derivaciones.
 * </p>
 * <p>
 * Relaciones:
 * <ul>
 *   <li>{@code matriculas} — 1:N con {@link Matricula}, cada curso académico tiene una entrada.</li>
 *   <li>{@code tutores} — N:M con {@link Tutor} (tabla {@code alumno_tutor}).</li>
 *   <li>{@code familiares} — N:M recursiva (tabla {@code alumno_familiar}); hermanos o primos.</li>
 *   <li>{@code autorizadaRecoger} — N:M con {@link PersonaAutorizada} (tabla {@code alumno_persona_autorizada}).</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "alumnos")
public class Alumno extends Persona {

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

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


    @Column(name = "colegio")
    private String colegio;

    @Column(name = "seguimiento_servicios_sociales")
    private Boolean seguimientoServiciosSociales = false;

    @Column(name = "seguimiento_saf")
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

    @Column(name = "num_repeticiones_previas", nullable = false)
    private Integer numRepeticionesPrevias = 0;

    @Column(name = "detalle_cursos_repetidos")
    private String detalleCursosRepetidos;

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matricula> matriculas = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "alumno_tutor",
            joinColumns = @JoinColumn(name = "alumno_id"),
            inverseJoinColumns = @JoinColumn(name = "tutor_id")
    )
    private List<Tutor> tutores = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "alumno_familiar",
            joinColumns = @JoinColumn(name = "alumno_id"),
            inverseJoinColumns = @JoinColumn(name = "familiar_id")
    )
    private List<Alumno> familiares = new ArrayList<>();

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PeriodoActividad> periodosActividad = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "alumno_persona_autorizada",
            joinColumns = @JoinColumn(name = "alumno_id"),
            inverseJoinColumns = @JoinColumn(name = "persona_autorizada_id")
    )
    private List<PersonaAutorizada> autorizadaRecoger = new ArrayList<>();

    public Alumno() {
        super();
    }

    public Alumno(String nombre, String apellidos, String direccion, LocalDate fechaNacimiento) {
        super(nombre, apellidos, direccion);
        this.fechaNacimiento = fechaNacimiento;
    }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

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

    public Integer getNumRepeticionesPrevias() {return numRepeticionesPrevias;}
    public void setNumRepeticionesPrevias(Integer numRepeticionesPrevias) {this.numRepeticionesPrevias = numRepeticionesPrevias;}

    public String getDetalleCursosRepetidos() {return detalleCursosRepetidos;}
    public void setDetalleCursosRepetidos(String detalleCursosRepetidos) {this.detalleCursosRepetidos = detalleCursosRepetidos;}

    public List<Matricula> getMatriculas() { return matriculas; }

    public Matricula getUltimaMatricula() {
        if (matriculas == null || matriculas.isEmpty()) return null;
        return matriculas.get(matriculas.size() - 1);
    }

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
        if (!this.autorizadaRecoger.contains(pa)) {
            this.autorizadaRecoger.add(pa);
        }
    }

    public void removePersonaAutorizada(PersonaAutorizada pa) {
        this.autorizadaRecoger.remove(pa);
    }

    public void addTutor(Tutor tutor) {
        this.tutores.add(tutor);
    }

    public void removeTutor(Tutor tutor) {
        this.tutores.remove(tutor);
    }

    public void addMatricula(Matricula matricula) {
        this.matriculas.add(matricula);
        matricula.setAlumno(this);
    }

    public void removeMatricula(Matricula matricula) {
        this.matriculas.remove(matricula);
        matricula.setAlumno(null);
    }

    public void addPeriodo(PeriodoActividad periodo) {
        this.periodosActividad.add(periodo);
        periodo.setAlumno(this);
    }

    public void removePeriodo(PeriodoActividad periodo) {
        this.periodosActividad.remove(periodo);
        periodo.setAlumno(null);
    }

}
