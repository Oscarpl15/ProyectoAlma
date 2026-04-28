package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.service.GestorMatriculas;
import com.practicasalma.proyectoalma.service.MatriculaService;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controlador JavaFX de la ficha de detalle de una matrícula ({@code fichaMatricula-view.fxml}).
 * <p>
 * Permite editar los campos de una matrícula existente: curso, grupo asignado y si es repetición.
 * Se abre como modal desde {@link FichaAlumnoController}.
 * </p>
 */
public class FichaMatriculaController {

    @FXML private TextField txtAlumno;
    @FXML private TextField txtAnyoAcademico;
    @FXML private TextField txtCurso;
    @FXML private TextField txtGrupoLectura;
    @FXML private ComboBox<String> comboGrupo;
    @FXML private CheckBox chkRepetidor;

    @FXML private Button btnEditar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelarEdicion;
    @FXML private Button btnCerrar;

    private Matricula matriculaActual;
    private String cursoOriginal;
    private boolean repeticionOriginal;

    private final MatriculaService matriculaService = new MatriculaService();

    @FXML
    public void initialize() {
        comboGrupo.getItems().addAll(GestorConfig.getGrupos());
    }

    public void setMatricula(Matricula matricula, String nombreAlumno) {
        this.matriculaActual = matricula;
        this.cursoOriginal = matricula.getCurso();
        this.repeticionOriginal = Boolean.TRUE.equals(matricula.getEsRepeticion());
        cargarDatosEnVista(nombreAlumno);
        cambiarModoEdicion(false);
        Platform.runLater(() -> btnEditar.requestFocus());
    }

    private void cargarDatosEnVista(String nombreAlumno) {
        txtAlumno.setText(nombreAlumno != null ? nombreAlumno : "");
        String anyo = matriculaActual.getAnyoAcademico() != null ? matriculaActual.getAnyoAcademico().replace("-", "/") : "";
        txtAnyoAcademico.setText(anyo);
        txtCurso.setText(cursoOriginal != null ? cursoOriginal : "");
        String grupoActual = matriculaActual.getGrupoAsignado();
        txtGrupoLectura.setText(grupoActual != null ? grupoActual : "Sin grupo");
        if (grupoActual != null && !comboGrupo.getItems().contains(grupoActual)) {
            comboGrupo.getItems().add(0, grupoActual);
        }
        comboGrupo.setValue(grupoActual);
        chkRepetidor.setSelected(repeticionOriginal);
        actualizarVistaCheckbox(chkRepetidor, "Es repetición", false);
    }

    private void cambiarModoEdicion(boolean editable) {
        txtGrupoLectura.setVisible(!editable);
        txtGrupoLectura.setManaged(!editable);
        comboGrupo.setVisible(editable);
        comboGrupo.setManaged(editable);

        actualizarVistaCheckbox(chkRepetidor, "Es repetición", editable);

        btnEditar.setVisible(!editable);
        btnEditar.setManaged(!editable);
        btnCerrar.setVisible(!editable);
        btnCerrar.setManaged(!editable);
        btnGuardar.setVisible(editable);
        btnGuardar.setManaged(editable);
        btnCancelarEdicion.setVisible(editable);
        btnCancelarEdicion.setManaged(editable);
    }

    private void actualizarVistaCheckbox(CheckBox chk, String textoBase, boolean editable) {
        chk.setDisable(!editable);
        chk.getStyleClass().removeAll("check-lectura-true", "check-lectura-false");
        if (!editable) {
            // Repetidor: Sí = rojo (malo), No = verde (bien) — lógica invertida
            if (chk.isSelected()) {
                chk.setText("✘  Sí");
                chk.getStyleClass().add("check-lectura-false");
            } else {
                chk.setText("✔  No");
                chk.getStyleClass().add("check-lectura-true");
            }
        } else {
            chk.setText(textoBase);
        }
    }

    @FXML
    private void activarEdicion() {
        cambiarModoEdicion(true);
    }

    @FXML
    private void cancelarEdicion() {
        chkRepetidor.setSelected(repeticionOriginal);
        comboGrupo.setValue(matriculaActual.getGrupoAsignado());
        cambiarModoEdicion(false);
    }

    @FXML
    private void guardarDatos() {
        boolean isRepetidor = chkRepetidor.isSelected();
        String nuevoCurso = cursoOriginal;

        if (!repeticionOriginal && isRepetidor) {
            String anterior = GestorMatriculas.cursoAnterior(cursoOriginal);
            if (anterior == null) {
                FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Sin curso anterior",
                        "No se puede restar curso: el alumno ya está en el primer curso.");
                return;
            }
            nuevoCurso = anterior;
        } else if (repeticionOriginal && !isRepetidor) {
            String siguiente = GestorMatriculas.siguienteCurso(cursoOriginal);
            if (siguiente == null) {
                FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Sin curso siguiente",
                        "No se puede avanzar curso: el alumno ya está en el último curso.");
                return;
            }
            nuevoCurso = siguiente;
        }

        String grupo = comboGrupo.getValue();

        try {
            matriculaService.actualizar(matriculaActual.getId(), nuevoCurso, grupo, isRepetidor);

            matriculaActual.setCurso(nuevoCurso);
            matriculaActual.setGrupoAsignado(grupo);
            matriculaActual.setEsRepeticion(isRepetidor);
            cursoOriginal = nuevoCurso;
            repeticionOriginal = isRepetidor;

            txtCurso.setText(nuevoCurso);
            txtGrupoLectura.setText(grupo != null ? grupo : "Sin grupo");

            cambiarModoEdicion(false);
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar",
                    "No se pudieron guardar los cambios: " + e.getMessage());
        }
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
}