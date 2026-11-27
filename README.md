# ESFOCAR - Sistema de Evaluación Docente por Rúbricas

Aplicación móvil nativa para Android desarrollada en **Kotlin** y **Jetpack Compose** bajo el patrón de arquitectura **MVVM**, diseñada para apoyar a los docentes de la Escuela de Formación de Carabineros (**ESFOCAR**) en la creación de rúbricas, evaluación de estudiantes con cálculo automático bajo la escala de notas chilena y gestión de documentos PDF, operando con arquitectura *offline-first* sincronizada con microservicios.

## Integrantes

* **Vicente Vidal**

* **Christian Mora**

## Funcionalidades Principales

* **Autenticación y Registro Docente:** Inicio de sesión y creación de cuentas institucionales con persistencia de sesión local (`SharedPreferences`) y validación remota con respaldo offline en base de datos local (`Room`).

* **Gestión de Plantillas de Rúbricas:** Creación, edición y eliminación de rúbricas personalizadas con múltiples criterios y puntajes configurables por nivel de logro (*No Logrado*, *Medianamente Logrado* y *Logrado*).

* **Evaluación de Alumnos y Cálculo de Notas:** Evaluación interactiva en vivo utilizando la escala de calificación chilena ($1.00$ a $7.00$) con un $60\%$ de exigencia por defecto.

* **Generación y Visor de Actas en PDF:** Generación automática de actas de evaluación en formato PDF (`PdfDocument`), visualización integrada dentro de la aplicación (`PdfRenderer`), exportación al almacenamiento del dispositivo y carga de documentos PDF externos.

* **Sincronización Offline-First:** Almacenamiento local inmediato en SQLite mediante **Room** y sincronización bidireccional automática y manual con microservicios Spring Boot en la nube (Neon PostgreSQL).

* **Consulta de Feriados Nacionales:** Banner informativo en el menú principal que consulta los próximos feriados oficiales de Chile para planificación académica.

## Tecnologías y Arquitectura

* **Lenguaje:** Kotlin

* **UI:** Jetpack Compose + Material Design 3

* **Arquitectura:** MVVM (Model - View - ViewModel) + Repository Pattern

* **Persistencia Local:** Room Database (`esfocar_database`) + SharedPreferences

* **Consumo de APIs:** Retrofit 2 + OkHttp + Gson

* **Asincronía:** Kotlin Coroutines & StateFlow

## Endpoints Utilizados

### 1. API Externa (Nager.Date - Feriados Públicos)

* **Base URL:** `https://date.nager.at/`

* **Descripción:** Obtiene los próximos días festivos oficiales en Chile (`CL`) para mostrarlos en el panel docente.

| Método | Endpoint | Parámetros | Descripción | 
| ----- | ----- | ----- | ----- | 
| `GET` | `/api/v3/NextPublicHolidays/{countryCode}` | `countryCode = "CL"` | Obtiene la lista de los próximos feriados públicos de Chile. | 

### 2. Microservicio de Usuarios / Profesores

* **Base URL Local:** `http://127.0.0.1:8081/` *(Nota: usar `http://10.0.2.2:8081/` en emulador Android sin port-forwarding)*

* **Descripción:** Gestiona el registro y autenticación de los docentes.

| Método | Endpoint | Cuerpo / Parámetros | Descripción | 
| ----- | ----- | ----- | ----- | 
| `POST` | `/api/v1/profesores/login` | `Body`: `{ "correo", "password" }` | Valida las credenciales e inicia sesión del profesor. | 
| `POST` | `/api/v1/profesores/registro` | `Body`: `Profesor` (`correo`, `password`, `nombre`, `apellidos`) | Registra un nuevo docente en el sistema remoto. | 

### 3. Microservicio de Rúbricas y Evaluaciones

* **Base URL Local:** `http://127.0.0.1:8082/` *(Nota: usar `http://10.0.2.2:8082/` en emulador Android sin port-forwarding)*

* **Descripción:** Gestiona el CRUD y sincronización de plantillas de rúbricas y evaluaciones realizadas.

#### Plantillas de Rúbricas

| Método | Endpoint | Cuerpo / Parámetros | Descripción | 
| ----- | ----- | ----- | ----- | 
| `GET` | `/api/v1/plantillas` | `Query`: `?correo={correo}` | Obtiene todas las plantillas creadas por el docente. | 
| `POST` | `/api/v1/plantillas` | `Body`: `PlantillaRubrica` | Crea una nueva plantilla de rúbrica en el servidor. | 
| `PUT` | `/api/v1/plantillas/{id}` | `Path`: `id` | `Body`: `PlantillaRubrica` | Actualiza una plantilla existente. | 
| `DELETE` | `/api/v1/plantillas/{id}` | `Path`: `id` | Elimina una plantilla por su identificador. | 

#### Evaluaciones de Alumnos

| Método | Endpoint | Cuerpo / Parámetros | Descripción | 
| ----- | ----- | ----- | ----- | 
| `GET` | `/api/v1/evaluaciones` | `Query`: `?correo={correo}` | Obtiene el historial de evaluaciones del docente. | 
| `POST` | `/api/v1/evaluaciones` | `Body`: `EvaluacionRubrica` | Registra una nueva evaluación de estudiante. | 
| `PUT` | `/api/v1/evaluaciones/{id}` | `Path`: `id` | `Body`: `EvaluacionRubrica` | Actualiza los puntajes o datos de una evaluación. | 
| `DELETE` | `/api/v1/evaluaciones/{id}` | `Path`: `id` | Elimina una evaluación registrada. | 

## Pasos para Ejecutar el Proyecto

### Prerrequisitos

* **Android Studio** (Ladybug o superior recomendado).

* **JDK 17** o superior.

* **SDK de Android** (API mínima 24 / Target API 34+).

* Microservicios de **Usuarios** (puerto `8081`) y **Rúbricas** (puerto `8082`) en ejecución (opcional para probar en modo *offline*, obligatorio para sincronización remota).

### Instrucciones de Ejecución

1. **Clonar el repositorio:**

   ```
   git clone https://github.com/tu-usuario/ESFOCAR.git
   cd ESFOCAR
   
   ```

2. **Levantar los microservicios Backend (Opcional / Recomendado):**

   * Asegúrate de tener corriendo el microservicio de Usuarios en el puerto `8081` y el microservicio de Rúbricas en el puerto `8082`.

3. **Configurar la red para el Emulador o Dispositivo Físico:**

   * El cliente Retrofit apunta por defecto a `http://127.0.0.1:8081/` y `http://127.0.0.1:8082/`.

   * Si ejecutas la app en un **emulador o dispositivo conectado por USB**, ejecuta los siguientes comandos `adb` en tu terminal para redirigir los puertos de tu PC al dispositivo:

     ```
     adb reverse tcp:8081 tcp:8081
     adb reverse tcp:8082 tcp:8082
     
     ```

   * *(Alternativa)*: Cambia `127.0.0.1` por `10.0.2.2` en `RetrofitClient.kt` si utilizas el emulador estándar de Android Studio.

4. **Abrir y compilar en Android Studio:**

   * Abre el proyecto en Android Studio.

   * Espera a que **Gradle** descargue todas las dependencias y sincronice el proyecto.

   * Selecciona un emulador o dispositivo Android y presiona **Run (`Shift + F10`)**.
