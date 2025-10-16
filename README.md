# App Hospital Baca Ortiz – Sistema Integral de Gestión Hospitalaria

## Descripción General
App Hospital Baca Ortiz es una aplicación de escritorio desarrollada en Java (Swing y JavaFX) con conexión a SQL Server mediante JDBC.  
Está diseñada para optimizar la administración médica, clínica y administrativa de un hospital pediátrico.  
El proyecto forma parte del Proyecto Integrador de la Facultad de Ciencias Técnicas de la Universidad Internacional del Ecuador, desarrollado por Katty Yautibug, Diego Ruiz y Gabriel Minda, bajo la tutoría de los ingenieros Marcela Venegas y Ronie Martínez.

---

## Objetivos del Proyecto
- Implementar una base de datos normalizada y optimizada en SQL Server.  
- Desarrollar una aplicación en Java que gestione pacientes, médicos, citas, consultas, hospitalizaciones y tratamientos.  
- Garantizar la seguridad, integridad y trazabilidad de la información médica.  
- Generar reportes, formularios y paneles visuales para la toma de decisiones.  

---

## Tecnologías Utilizadas

| Componente | Tecnología |
|-------------|-------------|
| Lenguaje principal | Java 23 |
| Interfaz gráfica | Swing y JavaFX 24 |
| Base de datos | SQL Server |
| Conexión | JDBC (Patrón Singleton) |
| IDE | IntelliJ IDEA |
| Control de versiones | Git y GitHub |
| Generación de reportes | iTextPDF / Apache POI |
| Sistema operativo | Windows |

---

## Estructura del Proyecto

```
src/
 ├─ model/             → Clases de entidad (Paciente, Medico, Cita, Consulta, etc.)
 ├─ controller/        → Controladores para CRUD y validaciones
 ├─ view/              → Interfaces gráficas (Swing / JavaFX)
 ├─ utils/             → Clase Conexion.java (JDBC Singleton)
 └─ resources/
     ├─ css/           → Estilos visuales para JavaFX
     ├─ img/           → Íconos e imágenes de interfaz
pom.xml                → Configuración Maven / dependencias
```

---

## Estructura de la Base de Datos (SQL Server)

### Entidades principales
- Paciente: Datos personales y clínicos.  
- Médico: Información profesional y especialidades.  
- Cita: Agenda médica y asignación de salas.  
- Consulta: Registro clínico detallado (motivo, diagnóstico, notas).  
- Diagnóstico, Tratamiento, Prueba, Hospitalización, Cirugía, Receta y Usuario.  

### Relaciones clave
- Un médico puede atender muchas consultas.  
- Un paciente puede tener varias citas y hospitalizaciones.  
- Cada consulta puede tener varios diagnósticos, tratamientos y pruebas clínicas.  
- Cada tratamiento puede incluir varios medicamentos.  

La base de datos se encuentra normalizada hasta la tercera forma normal (3FN), garantizando integridad referencial y minimización de redundancia.

---

## Configuración del Entorno

### Requisitos previos
- Java JDK 23 o superior  
- SQL Server 2019 o superior  
- IntelliJ IDEA o NetBeans  
- Conector JDBC de SQL Server  
- Git instalado  

### Configuración de la conexión JDBC
Editar la clase `Conexion.java` con las credenciales del servidor:

```java
String url = "jdbc:sqlserver://localhost:1433;databaseName=BDD";
String user = "proyecto_bdd";
String password = "root";
```

### Importar la Base de Datos
Ejecutar los scripts SQL de los anexos:
- Anexo Tablas.pdf → Estructura de tablas  
- Anexo Consultas.pdf → Consultas de validación y reportes  
- Anexo Uso de SQL Server.pdf → Configuración de backups automáticos  

---

## Módulos del Sistema

| Módulo | Descripción |
|--------|--------------|
| Pacientes | Registro, edición y búsqueda de pacientes. |
| Médicos | Gestión de personal médico y sus especialidades. |
| Citas | Programación y seguimiento de citas médicas. |
| Consultas | Registro de diagnósticos, tratamientos y pruebas. |
| Hospitalizaciones y Cirugías | Control de estancias y procedimientos quirúrgicos. |
| Usuarios y Roles | Control de acceso (administrador y médico). |
| Reportes PDF | Exportación de reportes estadísticos. |

---

## Consultas SQL Destacadas
- Próximas 10 citas programadas.  
- Diagnósticos y tratamientos por paciente.  
- Pruebas de laboratorio pendientes de resultado.  
- Hospitalizaciones activas y médicos responsables.  
- Número de médicos por especialidad.  

(Ver detalles en el documento Anexo Consultas.pdf)

---

## Seguridad y Validación
- Validación de datos en la clase `DynamicForm.java`.  
- Encriptación de contraseñas mediante `password_hash`.  
- Control de acceso mediante roles definidos.  
- Manejo de errores en tiempo real y validación visual.  

---

## Equipo de Desarrollo

| Rol | Integrante |
|------|-------------|
| Diseño de Base de Datos | Katty Yautibug |
| Lógica y Conexión JDBC | Diego Ruiz |
| Interfaz JavaFX/Swing | Gabriel Minda |
| Tutoría técnica | Ing. Marcela Venegas / Ing. Ronie Martínez |

---

## Licencia
Este proyecto académico se desarrolla bajo la licencia MIT, con fines educativos y de mejora continua para la Universidad Internacional del Ecuador (UIDE).

---

## Anexos del Proyecto
- Anexo BBD Inicial.pdf  
- Anexo Tablas.pdf  
- Anexo Consultas.pdf  
- Anexo Uso de SQL Server.pdf  
- Proyecto Integrador Final.pdf  

---

## Próximas Mejoras
- Implementar conexión remota con SQL Azure.  
- Sistema de notificaciones por correo (JavaMail API).  
- Dashboard estadístico en tiempo real (JavaFX Charts).  
- Integración con módulo de facturación electrónica.  

---

Repositorio oficial: [https://github.com/MasterPieceDR/App-Hospital-Beta](https://github.com/MasterPieceDR/App-Hospital-Beta)  
Última actualización: Octubre 2025  
Universidad Internacional del Ecuador – Ingeniería en Sistemas
