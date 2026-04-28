package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.dao.AlumnoDAO;
import com.practicasalma.proyectoalma.util.UtilFecha;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la ventana de gestión de grupos ({@code gestionarGrupos-view.fxml}).
 * <p>
 * Permite a la directora renombrar y eliminar grupos existentes, y añadir nuevos.
 * Al guardar, propaga los cambios de nombre a las matrículas del curso académico
 * actual, y deja sin grupo a los alumnos cuyo grupo haya sido eliminado.
 * </p>
 */
public class GestionarGruposController {

    @FXML private ListView<String> listaGrupos;

    private final ObservableList<String> items = FXCollections.observableArrayList();
    // Nombre original en BD por posición; null si el grupo fue añadido en esta sesión.
    private final List<String> originales = new ArrayList<>();
    // Nombres originales de grupos eliminados durante la sesión (para actualizar BD al guardar).
    private final List<String> eliminados = new ArrayList<>();

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

    @FXML
    public void initialize() {
        listaGrupos.setItems(items);
        listaGrupos.setEditable(true);
        listaGrupos.setCellFactory(lv -> new CeldaGrupo());

        List<String> grupos = GestorConfig.getGrupos();
        items.addAll(grupos);
        originales.addAll(grupos);

        // Cuando el commit llega desde la celda, actualizar el item en la lista
        listaGrupos.setOnEditCommit(e -> items.set(e.getIndex(), e.getNewValue()));

        ContextMenu cm = new ContextMenu();
        MenuItem itemEliminar = new MenuItem("Eliminar");
        itemEliminar.setOnAction(e -> eliminarSeleccionado());
        cm.getItems().add(itemEliminar);
        listaGrupos.setContextMenu(cm);
    }

    @FXML
    private void anadirGrupo() {
        String nuevoNombre = "Nuevo grupo";
        items.add(nuevoNombre);
        originales.add(null);
        int idx = items.size() - 1;
        listaGrupos.getSelectionModel().select(idx);
        listaGrupos.scrollTo(idx);
        listaGrupos.edit(idx);
    }

    @FXML
    private void eliminarSeleccionado() {
        int idx = listaGrupos.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;
        String original = originales.get(idx);
        if (original != null) {
            eliminados.add(original);
        }
        items.remove(idx);
        originales.remove(idx);
    }

    @FXML
    private void guardarCambios() {
        // Renombrados: sin filtro de año para que el nuevo nombre sea consistente en toda la historia
        for (int i = 0; i < items.size(); i++) {
            String original = originales.get(i);
            String actual = items.get(i);
            if (original != null && !original.equals(actual)) {
                alumnoDAO.renombrarGrupoEnTodasLasMatriculas(original, actual);
            }
        }

        // Eliminados: solo el curso actual; las matrículas históricas conservan el nombre
        String anyo = UtilFecha.calcularCursoAcademico();
        for (String eliminado : eliminados) {
            alumnoDAO.eliminarGrupoEnCursoActual(eliminado, anyo);
        }

        GestorConfig.setGrupos(new ArrayList<>(items));

        Stage stage = (Stage) listaGrupos.getScene().getWindow();
        stage.close();
    }

    // ── CELDA PERSONALIZADA ───────────────────────────────────────────────────

    private class CeldaGrupo extends ListCell<String> {

        private TextField textField;

        @Override
        public void startEdit() {
            super.startEdit();
            textField = new TextField(getItem() != null ? getItem() : "");
            textField.getStyleClass().add("campo-edicion-lista");
            textField.setOnAction(e -> commitEdit(textField.getText()));
            // Confirmar al perder el foco en lugar de cancelar
            textField.focusedProperty().addListener((obs, anterior, enfocado) -> {
                if (!enfocado && isEditing()) {
                    commitEdit(textField.getText());
                }
            });
            setText(null);
            setGraphic(textField);
            textField.requestFocus();
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else if (isEditing()) {
                if (textField != null) textField.setText(item);
                setText(null);
                setGraphic(textField);
            } else {
                setText(item);
                setGraphic(null);
            }
        }
    }
}
