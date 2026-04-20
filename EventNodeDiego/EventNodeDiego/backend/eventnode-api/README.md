# EventNode API — Guía del backend (estudio y defensa)

API REST con **Spring Boot 3.5.x**, **Java 21**, **Spring Data JPA**, **MySQL**, **Spring Security + JWT**, correo (**Spring Mail**), reportes (**JasperReports**) para diplomas, y tareas programadas (**`@Scheduled`**).

Este documento resume **estructura de carpetas**, **qué hace cada capa**, **seguridad por URL**, **conceptos de teoría** y **cómo razonar si alguien cambia el código** (por ejemplo en revisión o examen).

---

## 1. Cómo arrancar el proyecto

1. **MySQL** accesible (en `application.properties` el ejemplo usa `localhost:3307`, base `event_node`).
2. Desde esta carpeta (`eventnode-api`):

   ```bash
   mvn spring-boot:run
   ```

3. Puerto por defecto: **8080** (`server.port`).

**Perfil `dev`:** existe `application-dev.properties` con `ddl-auto=validate` y variables de entorno opcionales `DB_USER` / `DB_PASSWORD`. Para activarlo: `--spring.profiles.active=dev` (o variable de entorno equivalente).

**Datos iniciales:** `POST /api/seed/init` crea roles y un usuario **SUPERADMIN** (ver `SeedController`). Ese endpoint está **público** en `SecurityConfig`: en producción habría que protegerlo o eliminarlo.

---

## 2. Estructura de directorios (mapa mental)

```
eventnode-api/
├── pom.xml                          # Dependencias Maven y versión de Spring Boot / Java
├── README.md                        # Esta guía
└── src/
    ├── main/
    │   ├── java/com/eventnode/eventnodeapi/
    │   │   ├── EventnodeApiApplication.java   # Punto de entrada + @EnableScheduling
    │   │   ├── config/                        # Spring Security (filtros, CORS, reglas HTTP)
    │   │   ├── controllers/                   # Capa REST: HTTP → servicios
    │   │   ├── services/                      # Lógica de negocio y transacciones
    │   │   ├── repositories/                  # Acceso a datos (Spring Data JPA)
    │   │   ├── models/                          # Entidades JPA ↔ tablas MySQL
    │   │   ├── dtos/                            # Objetos de entrada/salida API (no siempre entidades)
    │   │   ├── security/                        # JWT, filtro, UserDetailsService
    │   │   └── schedulers/                      # Jobs periódicos (@Scheduled)
    │   └── resources/
    │       ├── application.properties           # Config principal (BD, mail, JPA, puerto)
    │       └── application-dev.properties       # Perfil desarrollo / validación DDL
    └── test/java/...                            # Pruebas unitarias / integración
```

**Carpeta `target/`:** salida de compilación (`.class`, JAR). **No se edita**; se regenera con `mvn compile` o `mvn package`.

---

## 3. Flujo típico de una petición

1. **Tomcat** (embebido) recibe HTTP.
2. **`SecurityFilterChain`** (`SecurityConfig`): CORS, sin sesión (stateless), reglas `permitAll` / `hasRole` / `authenticated`.
3. **`JwtAuthenticationFilter`:** si viene header `Authorization: Bearer <token>`, valida JWT y rellena el **contexto de seguridad** con el usuario y sus roles.
4. **`@RestController`:** mapea ruta y método HTTP, valida `@Valid` en DTOs si aplica, llama al **servicio**.
5. **Servicio:** reglas de negocio, `@Transactional` donde corresponde.
6. **Repositorio:** consultas JPA (`findBy…`, `save`, etc.).
7. **Respuesta:** JSON (`ResponseEntity`, mapas o DTOs).

---

## 4. Paquetes y archivos (qué es cada cosa)

### 4.1 Raíz

| Archivo | Función |
|--------|---------|
| `EventnodeApiApplication.java` | Clase `main`; `@SpringBootApplication` escanea componentes; `@EnableScheduling` activa el scheduler de eventos. |

### 4.2 `config/`

| Archivo | Función |
|--------|---------|
| `SecurityConfig.java` | Define **PasswordEncoder** (BCrypt), **CORS**, **AuthenticationManager**, cadena de filtros: desactiva CSRF (típico con JWT), sin form login, **STATELESS**, reglas por URL y método HTTP, inserta `JwtAuthenticationFilter` antes del filtro de usuario/contraseña. |

**Importante para exámenes:** en Spring Security 6 el orden de las reglas importa: la **primera** que coincide gana. Si algo “debería ser admin” pero solo pide login, revisa si cayó en `.anyRequest().authenticated()`.

### 4.3 `security/`

| Archivo | Función |
|--------|---------|
| `JwtTokenProvider.java` | Genera y valida JWT (firma HMAC, secreto `app.jwt-secret` o default en código; expiración `app.jwt-expiration-milliseconds` o 7 días). **Subject del token = correo** del usuario. |
| `JwtAuthenticationFilter.java` | `OncePerRequestFilter`: extrae Bearer, valida, carga `UserDetails` y pone `Authentication` en `SecurityContextHolder`. |
| `CustomUserDetailsService.java` | Implementa `UserDetailsService`: busca usuario por **correo**, asigna autoridad `ROLE_` + nombre del rol en BD, y usa `enabled` según estado `ACTIVO`, y bloqueo temporal `bloqueadoHasta`. |

**Teoría:** Spring espera nombres de rol como `ROLE_ADMIN` en `GrantedAuthority`; en código se usa `ROLE_` + `usuario.getRol().getNombre()` (ej. `ROLE_ALUMNO`). Los métodos `hasRole("ALUMNO")` de Spring **añaden** el prefijo `ROLE_` internamente.

### 4.4 `controllers/` (REST)

| Controlador | Base path | Rol de la lógica |
|-------------|-----------|-------------------|
| `AuthController` | `/api/auth` | Login, flujo recuperación contraseña (código por correo). |
| `AlumnoController` | `/api/alumnos` | Registro público de alumno. |
| `UsuarioController` | `/api/usuarios` | Listado admin, alta admin, perfil GET/PUT. |
| `CategoriaController` | `/api/categorias` | CRUD categorías (lectura pública; escritura admin). |
| `EventoController` | `/api/eventos` | CRUD eventos, organizadores embebidos en este controlador, cancelar, etc. |
| `PreCheckinController` | `/api/precheckin` | Inscripción/cancelación alumno; consultas por evento (admin). |
| `AsistenciaController` | `/api/asistencias` | Registro asistencia (QR/manual), listados, conteo, PATCH estado. |
| `DiplomaController` | `/api/diplomas` | Plantillas Jasper (Base64 en BD), emisión, preview, descarga. |
| `SeedController` | `/api/seed` | Inicialización de roles y superadmin. |

Los controladores **no** deberían contener reglas de negocio largas; delegan en **servicios**.

### 4.5 `services/`

| Servicio | Responsabilidad típica |
|----------|-------------------------|
| `AuthService` | Login: valida bloqueo, estado, contraseña BCrypt o legado texto plano (migra a BCrypt), intentos fallidos, bloqueo 15 min tras 3 fallos, genera JWT, enriquece respuesta con datos de `Alumno` si aplica. |
| `PasswordRecoveryService` | Código en campo `recoverPassword`, envío por correo, verificación y restablecimiento con BCrypt. |
| `UsuarioService` | Perfiles, reglas de actualización, listados. |
| `AlumnoService` | Registro alumno + usuario + rol. |
| `CategoriaService` | Operaciones de categorías. |
| `EventoService` | Crear/editar/cancelar eventos, consultas, **reactivar** (lógica en servicio). |
| `PreCheckinService` | Inscripciones, cupos, estados. |
| `AsistenciaService` | Registro asistencia, tolerancias, estados. |
| `DiplomaService` | JasperReports, almacenamiento plantilla, emisión, envío correo si aplica. |

### 4.6 `repositories/`

Interfaces `extends JpaRepository` o `CrudRepository`: Spring genera implementaciones. Métodos `findBy…` derivan queries por convención de nombres. Aquí vive el **acceso a datos**, no la lógica de negocio.

### 4.7 `models/` (entidades JPA)

| Entidad | Idea |
|---------|------|
| `Usuario` | Credenciales, estado, intentos, bloqueo, relación `ManyToOne` con `Rol`. |
| `Rol` | Nombres como `ALUMNO`, `ADMINISTRADOR`, `SUPERADMIN`. |
| `Alumno` | Datos académicos; `@OneToOne` + `@MapsId` con `Usuario` (misma PK `id_usuario`). |
| `Administrador` | Extensión de admin vinculada a `Usuario`. |
| `Categoria`, `Evento`, `Organizador` | Dominio del evento; `Evento` tiene estados alineados al scheduler. |
| `PreCheckin` | Inscripción previa a un evento. |
| `Asistencia` | Registro de asistencia real. |
| `Diploma`, `DiplomaEmitido` | Plantilla y emisiones por usuario/evento. |

**Teoría:** `ddl-auto=update` en `application.properties` hace que Hibernate **ajuste** el esquema al arrancar (útil en desarrollo; en producción suele usarse `validate` + migraciones Flyway/Liquibase).

### 4.8 `dtos/`

Objetos para **entrada/salida HTTP** (`LoginRequest`, `EventoCreateRequest`, `PerfilResponse`, etc.), con anotaciones de **Bean Validation** (`jakarta.validation`) donde corresponda. Separar DTO de entidad evita filtrar campos sensibles y acoplar el API al modelo de persistencia.

### 4.9 `schedulers/`

| Archivo | Función |
|--------|---------|
| `EventoScheduler.java` | Cada **60 s** recorre eventos: `PRÓXIMO` → `ACTIVO` si ya empezó; `ACTIVO` → `FINALIZADO` si ya terminó la `fechaFin`. |

---

## 5. Seguridad: matriz práctica (según `SecurityConfig`)

Interpretación **a alto nivel**; siempre verifica el archivo fuente ante un cambio del profesor.

| Área | Comportamiento típico |
|------|------------------------|
| `/api/auth/**` | Público. |
| `POST /api/alumnos/registro` | Público. |
| `POST /api/seed/init` | Público (riesgo en producción). |
| `GET /api/categorias` | Público. |
| `POST/PUT/DELETE /api/categorias...` | `ADMINISTRADOR` o `SUPERADMIN`. |
| `GET /api/eventos/**` | Público (incluye listado, detalle, organizadores GET, etc.). |
| `POST /api/eventos/crear`, `PUT/DELETE /api/eventos/**`, cancelar/reactivar | Admin / superadmin (rutas explícitas). |
| PreCheckin inscribir/cancelar/usuario | Rol `ALUMNO`. |
| PreCheckin por evento (admin) | Admin / superadmin. |
| `PATCH /api/asistencias/*/estado` | Admin / superadmin. |
| Usuarios | Varias reglas: listado y alta admin; perfil autenticado. |
| **Resto** | `.anyRequest().authenticated()` → hace falta JWT válido (cualquier rol salvo reglas más específicas). |

### 5.1 Cosas que debes poder explicar (y revisar en código)

1. **CSRF desactivado:** normal con APIs JWT stateless; el token va en header, no en cookies de sesión clásicas.
2. **CORS:** orígenes permitidos (front en `localhost:5173`, etc.). Si el front cambia de puerto, hay que actualizar `CorsConfigurationSource`.
3. **JWT:** no es “encriptación”; es **firma** (integridad). Quien tenga el secreto puede emitir tokens. En producción el secreto debe ser largo, aleatorio y en variable de entorno.
4. **Contraseñas:** BCrypt en `PasswordEncoder`; el login aún contempla contraseñas antiguas sin `$2` para migrar.
5. **Bloqueo de cuenta:** tras intentos fallidos y ventana `bloqueadoHasta`.

### 5.2 Detalle a vigilar (inconsistencias útiles para examen)

- En `EventoService` existe **`reactivarEvento`**, y en seguridad hay regla para `POST /api/eventos/*/reactivar`, pero **puede no haber** un `@PostMapping` correspondiente en `EventoController`. Si el profesor pregunta “por qué no funciona reactivar”, la respuesta es: **revisar si el endpoint está expuesto y llama al servicio**.
- Algunos `POST` bajo `/api/eventos/...` que **no** están listados explícitamente como `crear`, `cancelar` o `reactivar` pueden caer en **`authenticated()`** y no en “solo admin”. Conviene leer el orden exacto de `requestMatchers` y probar con Postman.

---

## 6. Dependencias clave (`pom.xml`)

| Dependencia | Para qué |
|-------------|----------|
| `spring-boot-starter-web` | REST, Jackson JSON. |
| `spring-boot-starter-data-jpa` | Hibernate + repositorios. |
| `spring-boot-starter-security` | Autenticación/autorización. |
| `spring-boot-starter-validation` | `@Valid`, anotaciones en DTOs. |
| `mysql-connector-j` | Driver MySQL. |
| `jjwt-*` | Creación/validación JWT. |
| `spring-boot-starter-mail` | Recuperación de contraseña y avisos. |
| `jasperreports` | PDFs de diplomas. |

---

## 7. Configuración (`application.properties`)

- **Datasource:** URL, usuario, contraseña, driver.
- **JPA:** dialecto, `show-sql`, `ddl-auto`.
- **Mail:** SMTP (p. ej. Gmail con contraseña de aplicación).
- **Multipart:** límites altos para **banners Base64**.

**Buenas prácticas (teoría / entrevista):** no versionar secretos reales; usar variables de entorno o un gestor de secretos. El archivo actual es típico de **proyecto académico**, no de producción.

---

## 8. Si el profesor modifica algo: cómo depurar

| Síntoma | Dónde mirar |
|---------|-------------|
| 401 / 403 en el front | `SecurityConfig` (¿ruta pública? ¿rol correcto?); ¿header `Authorization: Bearer`? |
| 403 solo para algunos usuarios | Nombre del rol en BD vs `hasRole`; ¿`CustomUserDetailsService` pone `ROLE_...`? |
| Token inválido o expirado | `JwtTokenProvider` (secreto, expiración); reloj del servidor. |
| Error al guardar / rollback | Servicio con `@Transactional`; excepciones no controladas. |
| Tabla o columna no existe | `ddl-auto`, dialecto MySQL, coherencia entidad ↔ BD. |
| Validación HTTP 400 | DTO y anotaciones `@NotNull`, etc.; `MethodArgumentNotValidException` en controladores. |
| CORS error en navegador | Origen no listado en `CorsConfigurationSource`. |
| Scheduler no corre | `@EnableScheduling` en la aplicación; ¿excepción silenciada en el job? |

**Herramientas:** logs de Spring, `logging.level.org.hibernate.SQL=DEBUG`, pruebas en `src/test`, Postman/curl.

---

## 9. Preguntas de teoría frecuentes (respuestas cortas)

1. **¿Qué es un REST controller?** Capa que mapea HTTP a operaciones, devuelve recursos/representaciones (JSON), sin estado de sesión en el servidor (aquí el “estado” del cliente va en JWT).
2. **¿JPA vs JDBC?** JPA abstrae el mapeo objeto-relacional; JDBC es más bajo nivel.
3. **¿Qué hace `@Transactional`?** Delimita una transacción de base de datos; si hay error, rollback (según reglas de propagación).
4. **¿Por qué DTOs?** Controlar qué entra y sale, validar, no exponer entidades completas ni contraseñas.
5. **¿JWT vs sesión?** JWT autentica cada request con un token firmado; no hace falta almacenar sesión en servidor (stateless). Revocación es más incómoda que con sesiones server-side.
6. **¿BCrypt?** Hash unidireccional adaptativo para contraseñas; incluye salt.
7. **¿Qué es el `SecurityFilterChain`?** Lista ordenada de filtros y reglas que deciden si la petición continúa y con qué autoridades.

---

## 10. Tests (`src/test/java`)

Hay pruebas de controladores, servicios, modelos y DTOs. Sirven como **documentación ejecutable** de comportamiento esperado. Comando:

```bash
mvn test
```

---

## 11. Resumen mental para defensa oral

- **Capas:** Controller → Service → Repository → Entity/DB.
- **Seguridad:** JWT en filtro + reglas declarativas en `SecurityConfig` + roles desde BD.
- **Dominio:** usuarios con roles, eventos con ciclo de vida (scheduler), inscripciones, asistencias, diplomas con Jasper.
- **Riesgos académicos a mencionar si te preguntan:** seed público, secretos en properties, endpoints que quizá solo piden `authenticated`, coherencia entre reglas de seguridad y endpoints reales (ej. reactivar).

Si mantienes esta guía al día cuando cambie el código, puedes localizar rápido **qué archivo tocar** y **qué concepto explicar** en cada parte del backend.
