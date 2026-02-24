package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Alumno;
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

    // --- Inyección de elementos del FXML ---
    @FXML private ImageView imgFoto;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtDireccion;
    @FXML private ComboBox<String> cmbCurso;
    @FXML private CheckBox chkActivo;
    @FXML private Hyperlink linkPadres;

    // Checkboxes de autorizaciones
    @FXML private CheckBox chkAuthImagen;
    @FXML private CheckBox chkAuthSalidas;
    @FXML private CheckBox chkAuthRecogida;

    @FXML private Button btnGuardar;

    private Alumno alumnoActual;
    private String rutaFotoSeleccionada = null;// Para saber si estamos editando uno existente

    @FXML
    public void initialize() {
        // Cargar el combo de cursos con datos de ejemplo
        cmbCurso.getItems().addAll("2025-2026", "2026-2027", "2027-2028");


    }

    // METODO CLAVE PARA RECIBIR DATOS (Lo usaremos mañana)
    public void setAlumno(Alumno alumno) {
        this.alumnoActual = alumno;
        if (alumno != null) {
            // Estamos EDITANDO: Rellenar campos
            txtNombre.setText(alumno.getNombre());
            txtApellidos.setText(alumno.getApellidos());
            // ... rellenar el resto ...
            btnGuardar.setText("Actualizar Datos");

            // --- CORRECCIÓN DE LA IMAGEN ---
            String rutaAlumno = alumno.getRutaFoto();

            // 1. Comprobamos que la ruta NO sea nula ni esté vacía
            if (rutaAlumno != null && !rutaAlumno.trim().isEmpty()) {
                try {
                    Image imagenAlumno;

                    // 2. Si la ruta empieza por "/", es nuestra imagen por defecto (recurso interno)
                    if (rutaAlumno.startsWith("/")) {
                        String rutaReal = getClass().getResource(rutaAlumno).toExternalForm();
                        imagenAlumno = new Image(rutaReal);
                    }
                    // 3. Si no, es una foto elegida por el usuario (archivo externo del PC)
                    else {
                        // Si ya tiene formato URI (file://...), la cargamos directo
                        if (rutaAlumno.startsWith("file:") || rutaAlumno.startsWith("http")) {
                            imagenAlumno = new Image(rutaAlumno);
                        } else {
                            // Si es una ruta normal de Windows/Mac (ej: C:\fotos\alumno.png), la convertimos
                            imagenAlumno = new Image(new java.io.File(rutaAlumno).toURI().toString());
                        }
                    }

                    // Colocamos la imagen en el visor
                    imgFoto.setImage(imagenAlumno);

                } catch (NullPointerException e) {
                    System.out.println("Error: No se encontró la imagen en los recursos internos: " + rutaAlumno);
                } catch (Exception e) {
                    System.out.println("Error al cargar la imagen externa: " + rutaAlumno);
                }
            } else {
                System.out.println("El alumno no tiene ninguna ruta de foto guardada.");
            }
        } else {
            // Estamos CREANDO UNO NUEVO
            btnGuardar.setText("Guardar Nueva Ficha");
            // Limpiar campos si hiciera falta
        }
    }

    // --- ACCIONES DE LOS BOTONES ---

    @FXML
    private void seleccionarFoto(ActionEvent event) {
        System.out.println("Abrir explorador de archivos para buscar foto...");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona foto del alumno");

        // Indicas que extensiones son validos para poder selccionar y te filtre
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg"));

        // Sirve para que solo se pueda darle click a la ventana "padre" hasta que seleciones una foto o canceles
        // y aparte guarda el archivo que hayas seleccionado
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado != null) {
            rutaFotoSeleccionada = archivoSeleccionado.getAbsolutePath();

            Image image = new Image(archivoSeleccionado.toURI().toString());
            imgFoto.setImage(image);

            System.out.println("Ruta de la foto: " + rutaFotoSeleccionada);
        }
    }

    @FXML
    private void abrirFichaPadres() {
        System.out.println("Navegando a la ficha de los padres...");
        // Aquí abriremos la otra ventana emergente más adelante
    }

    @FXML
    private void guardarDatos() {
        System.out.println("Guardando datos del formulario...");
        // Aquí irá la lógica de validar y llamar al Service/DAO
        cerrarVentana();
    }

    @FXML
    private void cerrarVentana() {
        // Obtener la ventana actual y cerrarla
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }
}
