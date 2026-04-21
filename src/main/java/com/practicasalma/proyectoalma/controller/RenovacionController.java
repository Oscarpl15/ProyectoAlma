package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.Launcher;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.model.Voluntario;
import com.practicasalma.proyectoalma.service.AlumnoService;
import com.practicasalma.proyectoalma.service.DocenteService;
import com.practicasalma.proyectoalma.service.GestorAsignaciones;
import com.practicasalma.proyectoalma.service.GestorMatriculas;
import com.practicasalma.proyectoalma.service.VoluntarioService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Controlador JavaFX del diálogo de renovación de inicio de curso ({@code renovacion-view.fxml}).
 * <p>
 * Se presenta automáticamente al abrir la aplicación durante el periodo de inicio de curso
 * (julio-septiembre). Muestra las listas de alumnos en 6º Primaria y del personal (docentes
 * y voluntarios) que necesitan confirmación manual para renovar su matrícula/asignación.
 * El usuario puede confirmar, posponer o ignorar cada caso.
 * </p>
 */
public class RenovacionController {

    @FXML private Label lblTitulo;
    @FXML private VBox contenedor;

    private Stage stage;
    private GestorMatriculas.DatosDialogo datosSexto;
    private GestorAsignaciones.DatosPersonal datosPersonal;

    private final DocenteService docenteService = new DocenteService();
    private final VoluntarioService voluntarioService = new VoluntarioService();

    // Llamado desde mostrar() antes de showAndWait()
    void inicializar(GestorMatriculas.DatosDialogo datosSexto,
                     GestorAsignaciones.DatosPersonal datosPersonal,
                     Stage stage) {
        this.stage = stage;
        this.datosSexto = datosSexto;
        this.datosPersonal = datosPersonal;

        String anyo = datosPersonal != null ? datosPersonal.anyoNuevo
                : datosSexto != null ? datosSexto.anyoAcademico : "";
        lblTitulo.setText("Inicio de curso " + anyo);

        // Sección alumnos 6º
        if (datosSexto != null && !datosSexto.isEmpty()) {
            Label tituloSexto = new Label("Alumnos que finalizan 6º Primaria — ¿repiten o causan baja?");
            tituloSexto.getStyleClass().add("titulo-dialogo-gestion");
            contenedor.getChildren().add(tituloSexto);
            for (Alumno a : datosSexto.alumnosSexto) {
                contenedor.getChildren().add(construirFilaAlumno(a));
            }
        }

        // Separador entre secciones
        boolean haySexto = datosSexto != null && !datosSexto.isEmpty();
        boolean hayPersonal = datosPersonal != null && !datosPersonal.isEmpty();
        if (haySexto && hayPersonal) {
            contenedor.getChildren().add(new Separator());
        }

        // Sección docentes y voluntarios
        if (hayPersonal) {
            Label tituloPersonal = new Label("Docentes y voluntarios — ¿continúan en " + anyo + "?");
            tituloPersonal.getStyleClass().add("titulo-dialogo-gestion");
            contenedor.getChildren().add(tituloPersonal);
            for (Docente d : datosPersonal.docentes) {
                contenedor.getChildren().add(construirFilaDocente(d));
            }
            for (Voluntario v : datosPersonal.voluntarios) {
                contenedor.getChildren().add(construirFilaVoluntario(v));
            }
        }
    }

    // --- Filas ---

    private HBox construirFilaAlumno(Alumno alumno) {
        HBox fila = new HBox(12);
        fila.setAlignment(Pos.CENTER_LEFT);

        Label nombre = new Label(nombreCompleto(alumno.getNombre(), alumno.getApellidos()));
        nombre.setPrefWidth(210);

        Label badge = new Label("6º Primaria");
        badge.getStyleClass().add("etiqueta-tipo-persona");
        badge.setPrefWidth(90);

        Button btnRepite = new Button("Sí repite");
        btnRepite.getStyleClass().add("boton-accion");

        Button btnBaja = new Button("No repite");
        btnBaja.getStyleClass().add("boton-cancelar");

        Button btnPostponer = new Button("Postponer");
        btnPostponer.getStyleClass().add("boton-secundario");

        btnRepite.setOnAction(e -> {
            try {
                Matricula m = new Matricula("6º Primaria", alumno);
                m.setAnyoAcademico(datosSexto.anyoAcademico);
                m.setEsRepeticion(true);
                datosSexto.matriculaDAO.guardar(m);
            } catch (Exception ex) {
                System.err.println("Error al guardar matrícula de repetición: " + ex.getMessage());
            }
            quitarFila(fila);
        });

        btnBaja.setOnAction(e -> {
            try {
                datosSexto.alumnoDAO.actualizarActivo(alumno.getId(), false);
            } catch (Exception ex) {
                System.err.println("Error al dar de baja alumno: " + ex.getMessage());
            }
            quitarFila(fila);
        });

        btnPostponer.setOnAction(e -> {
            datosSexto.postponed.setProperty(
                    String.valueOf(alumno.getId()), LocalDate.now().plusDays(7).toString());
            GestorMatriculas.guardarPostponed(datosSexto.postponed);
            quitarFila(fila);
        });

        fila.getChildren().addAll(nombre, badge, btnRepite, btnBaja, btnPostponer);
        return fila;
    }

    private HBox construirFilaDocente(Docente docente) {
        HBox fila = new HBox(12);
        fila.setAlignment(Pos.CENTER_LEFT);

        Label nombre = new Label(nombreCompleto(docente.getNombre(), docente.getApellidos()));
        nombre.setPrefWidth(210);

        Label badge = new Label("Docente");
        badge.getStyleClass().add("etiqueta-tipo-persona");
        badge.setPrefWidth(90);

        Button btnSigue = new Button("Sigue");
        btnSigue.getStyleClass().add("boton-accion");

        Button btnNoSigue = new Button("No sigue");
        btnNoSigue.getStyleClass().add("boton-cancelar");

        Button btnPostponer = new Button("Postponer");
        btnPostponer.getStyleClass().add("boton-secundario");

        btnSigue.setOnAction(e -> {
            try {
                docenteService.registrarContinuacion(docente, datosPersonal.anyoNuevo);
            } catch (Exception ex) {
                System.err.println("Error al registrar continuación docente: " + ex.getMessage());
            }
            quitarFila(fila);
        });

        btnNoSigue.setOnAction(e -> {
            try {
                docenteService.actualizarActivo(docente.getId(), false);
            } catch (Exception ex) {
                System.err.println("Error al dar de baja docente: " + ex.getMessage());
            }
            quitarFila(fila);
        });

        btnPostponer.setOnAction(e -> {
            datosPersonal.postponed.setProperty(
                    "d_" + docente.getId(), LocalDate.now().plusWeeks(1).toString());
            GestorAsignaciones.guardarPostponed(datosPersonal.postponed);
            quitarFila(fila);
        });

        fila.getChildren().addAll(nombre, badge, btnSigue, btnNoSigue, btnPostponer);
        return fila;
    }

    private HBox construirFilaVoluntario(Voluntario voluntario) {
        HBox fila = new HBox(12);
        fila.setAlignment(Pos.CENTER_LEFT);

        Label nombre = new Label(nombreCompleto(voluntario.getNombre(), voluntario.getApellidos()));
        nombre.setPrefWidth(210);

        Label badge = new Label("Voluntario");
        badge.getStyleClass().add("etiqueta-tipo-persona");
        badge.setPrefWidth(90);

        Button btnSigue = new Button("Sigue");
        btnSigue.getStyleClass().add("boton-accion");

        Button btnNoSigue = new Button("No sigue");
        btnNoSigue.getStyleClass().add("boton-cancelar");

        Button btnPostponer = new Button("Postponer");
        btnPostponer.getStyleClass().add("boton-secundario");

        btnSigue.setOnAction(e -> {
            try {
                voluntarioService.registrarContinuacion(voluntario, datosPersonal.anyoNuevo);
            } catch (Exception ex) {
                System.err.println("Error al registrar continuación voluntario: " + ex.getMessage());
            }
            quitarFila(fila);
        });

        btnNoSigue.setOnAction(e -> {
            try {
                voluntarioService.actualizarActivo(voluntario.getId(), false);
            } catch (Exception ex) {
                System.err.println("Error al dar de baja voluntario: " + ex.getMessage());
            }
            quitarFila(fila);
        });

        btnPostponer.setOnAction(e -> {
            datosPersonal.postponed.setProperty(
                    "v_" + voluntario.getId(), LocalDate.now().plusWeeks(1).toString());
            GestorAsignaciones.guardarPostponed(datosPersonal.postponed);
            quitarFila(fila);
        });

        fila.getChildren().addAll(nombre, badge, btnSigue, btnNoSigue, btnPostponer);
        return fila;
    }

    private void quitarFila(HBox fila) {
        contenedor.getChildren().remove(fila);
        long filasRestantes = contenedor.getChildren().stream()
                .filter(n -> n instanceof HBox).count();
        if (filasRestantes == 0) stage.close();
    }

    @FXML
    private void cerrar() {
        stage.close();
    }

    private static String nombreCompleto(String nombre, String apellidos) {
        return ((nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "")).trim();
    }

    // Método estático que carga el FXML, inicializa el controlador y muestra la ventana
    public static void mostrar(GestorMatriculas.DatosDialogo datosSexto,
                                GestorAsignaciones.DatosPersonal datosPersonal) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Launcher.class.getResource("view/renovacion-view.fxml"));
            Parent root = loader.load();
            RenovacionController ctrl = loader.getController();

            String anyo = datosPersonal != null ? datosPersonal.anyoNuevo
                    : datosSexto != null ? datosSexto.anyoAcademico : "nuevo curso";

            Stage stage = new Stage();
            stage.setTitle("Inicio de curso " + anyo);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();

            try {
                stage.getIcons().add(new Image(
                        RenovacionController.class.getResourceAsStream(
                                "/com/practicasalma/proyectoalma/assets/logo.png")));
            } catch (Exception ignored) {}

            ctrl.inicializar(datosSexto, datosPersonal, stage);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error al abrir renovacion-view: " + e.getMessage());
        }
    }
}
