# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Compilar
mvn clean compile

# Ejecutar la aplicación
mvn javafx:run

# Ejecutar tests
mvn test

# Empaquetar
mvn clean package
```

La clase de entrada real es `Launcher` (JavaFX Application). `MainLauncher` es un wrapper para evitar problemas con módulos JavaFX. `HelloApplication` es solo para pruebas de inicialización de BD.

## Stack tecnológico

- **Java 21** con **JavaFX 21.0.6** (UI con FXML)
- **Hibernate 6 / JPA** para ORM
- **SQLite** como base de datos (`datos/bbdd_FundacionAlma.db`)
- **AtlantaFX** (tema PrimerLight) para estilos visuales
- **OpenPDF** para generación de certificados PDF
- **Jakarta Mail** para envío de emails por SMTP Gmail
- **Maven** como herramienta de build

## Arquitectura

Aplicación de escritorio JavaFX con arquitectura en capas:

```
Controller (FXML/JavaFX) → Service (lógica negocio) → DAO (acceso datos) → Hibernate/JPA → SQLite
```

- **`controller/`** — Controladores JavaFX vinculados a vistas FXML. `MainController` gestiona la navegación por pestañas y carga los sub-controladores.
- **`model/`** — Entidades JPA. `Persona` es `@MappedSuperclass` base de `Alumno`, `Socio`, `Docente`, `Voluntario` y `Tutor`.
- **`service/`** — Lógica de negocio: `AlumnoService` (CRUD y matrículas), `GeneradorPdfService` (certificados de donación), `GestorCorreo` (emails).
- **`dao/`** — `AlumnoDAO` usa la Criteria API de JPA para consultas dinámicas.
- **`util/GestorBBDD.java`** — Singleton que expone el `EntityManagerFactory`. Punto central de acceso a JPA.

## Modelo de datos clave

- `Alumno` tiene: matrículas (`Matricula` 1→N), tutores (`Tutor` N→M via `alumno_tutor`), familiares (autojoin N→M via `alumno_familiar`), periodos de actividad y personas autorizadas.
- `Socio` tiene: donaciones (`Donacion` 1→N) y periodos de actividad.
- `persistence.xml` usa `hbm2ddl.auto=update`, por lo que Hibernate crea/altera tablas automáticamente al arrancar.

## Configuración

- **Base de datos:** ruta relativa `datos/bbdd_FundacionAlma.db` (relativa al directorio de trabajo al ejecutar)
- **Email:** credenciales hardcodeadas en `GestorCorreo.java` como placeholders — deben configurarse con credenciales reales de Gmail (App Password) antes de usar
- **CSS:** color corporativo principal `#2E5374` en `src/main/resources/.../css/estilos.css`
