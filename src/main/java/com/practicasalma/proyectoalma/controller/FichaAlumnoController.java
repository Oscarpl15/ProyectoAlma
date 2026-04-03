package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Alumno;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FichaAlumnoController {

    @FXML private ImageView imgFoto;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtColegio;
    @FXML private TextField txtDerivado;

    @FXML private CheckBox chkAutoDatos;
    @FXML private CheckBox chkAutoImagen;
    @FXML private CheckBox chkAutoActividades;
    @FXML private CheckBox chkAutoComunicaciones;
    @FXML private CheckBox chkAutoIrseSolo;

    // Botones de acción
    @FXML private Button btnCambiarFoto;
    @FXML private Button btnEditar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelarEdicion;
    @FXML private Button btnCerrar;

    private Alumno alumnoActual;
    private String rutaFotoSeleccionada = null;

    @FXML
    public void initialize() {
        // Nada aquí, lo delegamos a setAlumno que es quien recibe los datos reales
    }

    public void setAlumno(Alumno alumno) {
        this.alumnoActual = alumno;
        cargarDatosEnVista();

        // Forzamos el modo lectura al abrir la ficha
        cambiarModoEdicion(false);

    }

    private void cargarDatosEnVista() {
        if (alumnoActual != null) {
            txtNombre.setText(alumnoActual.getNombre() != null ? alumnoActual.getNombre() : "");
            txtApellidos.setText(alumnoActual.getApellidos() != null ? alumnoActual.getApellidos() : "");
            txtDni.setText(alumnoActual.getDocumentoIdentidad() != null ? alumnoActual.getDocumentoIdentidad() : "");
            txtDireccion.setText(alumnoActual.getDireccion() != null ? alumnoActual.getDireccion() : "");
            txtTelefono.setText(alumnoActual.getTelefono() != null ? alumnoActual.getTelefono() : "");
            txtColegio.setText(alumnoActual.getColegio() != null ? alumnoActual.getColegio() : "");
            txtDerivado.setText(alumnoActual.getDerivadoPor() != null ? alumnoActual.getDerivadoPor() : "");
            dpFechaNacimiento.setValue(alumnoActual.getFechaNacimiento());

            chkAutoDatos.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaUsoDatos()));
            chkAutoImagen.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaImagen()));
            chkAutoActividades.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaActividades()));
            chkAutoComunicaciones.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaComunicaciones()));
            chkAutoIrseSolo.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaIrseSolo()));

            String rutaAlumno = alumnoActual.getRutaFotoPerfil();
            if (rutaAlumno != null && !rutaAlumno.trim().isEmpty()) {
                try {
                    if (rutaAlumno.startsWith("/")) {
                        imgFoto.setImage(new Image(getClass().getResource(rutaAlumno).toExternalForm()));
                    } else if (rutaAlumno.startsWith("file:") || rutaAlumno.startsWith("http")) {
                        imgFoto.setImage(new Image(rutaAlumno));
                    } else {
                        imgFoto.setImage(new Image(new File(rutaAlumno).toURI().toString()));
                    }
                } catch (Exception e) {
                    System.out.println("Error al cargar la imagen: " + rutaAlumno);
                }
            } else {
                cargarFotoPorDefecto();
            }
        }
    }

    private void cargarFotoPorDefecto() {
        try {
            imgFoto.setImage(new Image(getClass().getResource("/com/practicasalma/proyectoalma/assets/default.png").toExternalForm()));
        } catch (Exception e) {
            System.out.println("No se encontró imagen por defecto.");
        }
    }

    // --- CONTROL DE LECTURA/EDICIÓN ---

    private void cambiarModoEdicion(boolean editable) {
        // TextFields y DatePicker
        txtNombre.setEditable(editable);
        txtApellidos.setEditable(editable);
        txtDni.setEditable(editable);
        txtDireccion.setEditable(editable);
        txtTelefono.setEditable(editable);
        txtColegio.setEditable(editable);
        txtDerivado.setEditable(editable);
        dpFechaNacimiento.setDisable(!editable);

        // MAGIA 2: Ocultar y comprimir el botón de Cambiar Foto si no estamos editando
        btnCambiarFoto.setVisible(editable);
        btnCambiarFoto.setManaged(editable);

        // Visibilidad de Botones Inferiores
        btnEditar.setVisible(!editable);
        btnEditar.setManaged(!editable);
        btnCerrar.setVisible(!editable);
        btnCerrar.setManaged(!editable);

        btnGuardar.setVisible(editable);
        btnGuardar.setManaged(editable);
        btnCancelarEdicion.setVisible(editable);
        btnCancelarEdicion.setManaged(editable);

        // MAGIA 3: Transformar el diseño de los CheckBoxes delegando la lógica visual al CSS
        actualizarVistaCheckbox(chkAutoDatos, "Protección Datos", editable);
        actualizarVistaCheckbox(chkAutoImagen, "Uso Imagen", editable);
        actualizarVistaCheckbox(chkAutoActividades, "Actividades", editable);
        actualizarVistaCheckbox(chkAutoComunicaciones, "Comunicaciones", editable);
        actualizarVistaCheckbox(chkAutoIrseSolo, "Autoriza Irse Solo", editable);

        if (!editable) {
            Platform.runLater(() -> btnEditar.requestFocus());
        }
    }

    private void actualizarVistaCheckbox(CheckBox chk, String textoBase, boolean editable) {
        chk.setDisable(!editable);

        // Limpiamos las clases CSS previamente añadidas
        chk.getStyleClass().removeAll("check-lectura-true", "check-lectura-false");

        if (!editable) {
            // Modo Lectura: Añadimos iconos de texto y aplicamos la clase CSS correspondiente
            if (chk.isSelected()) {
                chk.setText("✔  " + textoBase);
                chk.getStyleClass().add("check-lectura-true");
            } else {
                chk.setText("✘  " + textoBase);
                chk.getStyleClass().add("check-lectura-false");
            }
        } else {
            // Modo Edición: Restauramos el texto original y la caja aparecerá sola al quitarle el disabled
            chk.setText(textoBase);
        }
    }

    @FXML
    private void activarEdicion(ActionEvent event) {
        cambiarModoEdicion(true);
    }

    @FXML
    private void cancelarEdicion(ActionEvent event) {
        cargarDatosEnVista(); // Restaura los datos
        cambiarModoEdicion(false); // Vuelve a bloquear
    }

    @FXML
    private void guardarDatos() {
        System.out.println("Guardando datos del formulario en BD...");
        // Aquí llamaremos al AlumnoService para hacer el update real en base de datos.
        cambiarModoEdicion(false);
    }

    @FXML
    private void seleccionarFoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona foto del alumno");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado != null) {
            rutaFotoSeleccionada = archivoSeleccionado.getAbsolutePath();
            imgFoto.setImage(new Image(archivoSeleccionado.toURI().toString()));
        }
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
}
