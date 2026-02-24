package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Alumno;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

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

    private Alumno alumnoActual; // Para saber si estamos editando uno existente

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
        } else {
            // Estamos CREANDO UNO NUEVO
            btnGuardar.setText("Guardar Nueva Ficha");
            // Limpiar campos si hiciera falta
        }
    }

    // --- ACCIONES DE LOS BOTONES ---

    @FXML
    private void seleccionarFoto() {
        System.out.println("Abrir explorador de archivos para buscar foto...");
        // Aquí usaremos FileChooser más adelante
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
