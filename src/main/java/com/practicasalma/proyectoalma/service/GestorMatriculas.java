package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AlumnoDAO;
import com.practicasalma.proyectoalma.dao.MatriculaDAO;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class GestorMatriculas {

    private static final List<String> CURSOS = List.of(
            "1º Infantil", "2º Infantil", "3º Infantil",
            "1º Primaria", "2º Primaria", "3º Primaria",
            "4º Primaria", "5º Primaria", "6º Primaria"
    );

    private static final String RUTA_POSTPONED = "datos/postponed_matriculas.properties";

    public static void ejecutar() {
        if (!esPeriodoGeneracion()) return;

        String anyoActual = calcularAnyoAcademico();
        AlumnoDAO alumnoDAO = new AlumnoDAO();
        MatriculaDAO matriculaDAO = new MatriculaDAO();

        List<Alumno> alumnos = alumnoDAO.obtenerTodos();

        List<Alumno> sinMatricula = alumnos.stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .filter(a -> !yaExisteMatricula(a, anyoActual))
                .collect(Collectors.toList());

        if (sinMatricula.isEmpty()) return;

        Properties postponed = cargarPostponed();
        LocalDate hoy = LocalDate.now();

        List<Alumno> enSexto = sinMatricula.stream()
                .filter(a -> "6º Primaria".equals(obtenerUltimoCurso(a)))
                .filter(a -> {
                    String fechaStr = postponed.getProperty(String.valueOf(a.getId()));
                    if (fechaStr == null) return true;
                    LocalDate fechaPostponer = LocalDate.parse(fechaStr);
                    if (hoy.isBefore(fechaPostponer)) return false;
                    postponed.remove(String.valueOf(a.getId()));
                    guardarPostponed(postponed);
                    return true;
                })
                .collect(Collectors.toList());

        List<Alumno> resto = sinMatricula.stream()
                .filter(a -> !"6º Primaria".equals(obtenerUltimoCurso(a)))
                .collect(Collectors.toList());

        for (Alumno a : resto) {
            String cursoActual = obtenerUltimoCurso(a);
            String nuevoCurso = cursoActual == null ? CURSOS.get(0) : siguienteCurso(cursoActual);
            if (nuevoCurso == null) continue;
            try {
                Matricula m = new Matricula(nuevoCurso, a);
                m.setAnyoAcademico(anyoActual);
                matriculaDAO.guardar(m);
            } catch (Exception e) {
                System.err.println("Error al generar matrícula para " + a.getNombre() + ": " + e.getMessage());
            }
        }

        if (!enSexto.isEmpty()) {
            mostrarDialogoSexto(enSexto, anyoActual, matriculaDAO, alumnoDAO, postponed);
        }
    }

    public static boolean esPeriodoGeneracion() {
        LocalDate hoy = LocalDate.now();
        int mes = hoy.getMonthValue();
        int dia = hoy.getDayOfMonth();
        //return mes == 7 || mes == 8 || (mes == 9 && dia <= 10);
        return mes == 4;
    }

    public static String calcularAnyoAcademico() {
        LocalDate hoy = LocalDate.now();
        int anyo = hoy.getYear();
        int mes = hoy.getMonthValue();
        //if (mes >= 6) {
            return anyo + "/" + (anyo + 1);
        //}
        //return (anyo - 1) + "/" + anyo;
    }

    public static boolean yaExisteMatricula(Alumno alumno, String anyoAcademico) {
        if (alumno.getMatriculas() == null) return false;
        String alternativo = anyoAcademico.replace("/", "-");
        return alumno.getMatriculas().stream().anyMatch(m ->
                anyoAcademico.equals(m.getAnyoAcademico()) ||
                alternativo.equals(m.getAnyoAcademico())
        );
    }

    public static String siguienteCurso(String curso) {
        int idx = CURSOS.indexOf(curso);
        if (idx < 0 || idx >= CURSOS.size() - 1) return null;
        return CURSOS.get(idx + 1);
    }

    public static String cursoAnterior(String curso) {
        int idx = CURSOS.indexOf(curso);
        if (idx <= 0) return null;
        return CURSOS.get(idx - 1);
    }

    private static String obtenerUltimoCurso(Alumno alumno) {
        if (alumno.getMatriculas() == null || alumno.getMatriculas().isEmpty()) return null;
        return alumno.getMatriculas().get(alumno.getMatriculas().size() - 1).getCurso();
    }

    private static Properties cargarPostponed() {
        Properties props = new Properties();
        File f = new File(RUTA_POSTPONED);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                props.load(fis);
            } catch (Exception e) {
                System.err.println("Error cargando postponed: " + e.getMessage());
            }
        }
        return props;
    }

    private static void guardarPostponed(Properties props) {
        try {
            new File("datos").mkdirs();
            try (FileOutputStream fos = new FileOutputStream(RUTA_POSTPONED)) {
                props.store(fos, null);
            }
        } catch (Exception e) {
            System.err.println("Error guardando postponed: " + e.getMessage());
        }
    }

    private static void mostrarDialogoSexto(List<Alumno> alumnos, String anyoAcademico,
                                            MatriculaDAO matriculaDAO, AlumnoDAO alumnoDAO,
                                            Properties postponed) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Alumnos finalizando 6º Primaria");

        VBox listaAlumnos = new VBox(10);
        listaAlumnos.setPadding(new Insets(15));

        Label titulo = new Label("Los siguientes alumnos terminan 6º Primaria. ¿Repiten o causan baja?");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        listaAlumnos.getChildren().add(titulo);

        for (Alumno alumno : alumnos) {
            HBox fila = construirFilaAlumno(alumno, anyoAcademico, matriculaDAO, alumnoDAO,
                    listaAlumnos, stage, postponed);
            listaAlumnos.getChildren().add(fila);
        }

        Scene scene = new Scene(listaAlumnos, 600, Math.min(80 + alumnos.size() * 50, 500));
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static HBox construirFilaAlumno(Alumno alumno, String anyoAcademico,
                                            MatriculaDAO matriculaDAO, AlumnoDAO alumnoDAO,
                                            VBox lista, Stage stage, Properties postponed) {
        HBox fila = new HBox(15);
        fila.setAlignment(Pos.CENTER_LEFT);

        String nombreCompleto = (alumno.getNombre() != null ? alumno.getNombre() : "") +
                " " + (alumno.getApellidos() != null ? alumno.getApellidos() : "");
        Label nombre = new Label(nombreCompleto.trim());
        nombre.setPrefWidth(220);

        Button btnRepite = new Button("Sí repite");
        Button btnBaja = new Button("No repite");
        Button btnPostponer = new Button("Postponer");

        btnRepite.setMinWidth(Button.USE_PREF_SIZE);
        btnBaja.setMinWidth(Button.USE_PREF_SIZE);
        btnPostponer.setMinWidth(Button.USE_PREF_SIZE);

        btnRepite.setOnAction(e -> {
            try {
                Matricula m = new Matricula("6º Primaria", alumno);
                m.setAnyoAcademico(anyoAcademico);
                m.setEsRepeticion(true);
                matriculaDAO.guardar(m);
            } catch (Exception ex) {
                System.err.println("Error al guardar matrícula de repetición: " + ex.getMessage());
            }
            lista.getChildren().remove(fila);
            cerrarSiVacio(lista, stage);
        });

        btnBaja.setOnAction(e -> {
            try {
                alumnoDAO.actualizarActivo(alumno.getId(), false);
            } catch (Exception ex) {
                System.err.println("Error al dar de baja alumno: " + ex.getMessage());
            }
            lista.getChildren().remove(fila);
            cerrarSiVacio(lista, stage);
        });

        btnPostponer.setOnAction(e -> {
            String fechaPostponer = LocalDate.now().plusDays(7).toString();
            postponed.setProperty(String.valueOf(alumno.getId()), fechaPostponer);
            guardarPostponed(postponed);
            lista.getChildren().remove(fila);
            cerrarSiVacio(lista, stage);
        });

        fila.getChildren().addAll(nombre, btnRepite, btnBaja, btnPostponer);
        return fila;
    }

    private static void cerrarSiVacio(VBox lista, Stage stage) {
        long filas = lista.getChildren().stream()
                .filter(n -> n instanceof HBox)
                .count();
        if (filas == 0) stage.close();
    }
}