# Resumen: Conceptos de Spring Boot

Resumen de todo lo cubierto en este proyecto, con referencias a los archivos donde
se ve cada concepto en práctica. Pensado como contraparte de `angular-practice/CONCEPTOS.md`.

---

## 1. Arranque de la app

```
TaskManagerApplication.main() → @SpringBootApplication → Spring escanea componentes
```

- **`TaskManagerApplication`** — entry point. `@SpringBootApplication` = combinación de:
  - `@Configuration` — la clase puede declarar beans.
  - `@EnableAutoConfiguration` — Spring Boot configura solo cosas según las dependencias del `pom.xml` (ej. ve `spring-boot-starter-web` → levanta Tomcat embebido; ve `h2` + `data-jpa` → configura un `DataSource`).
  - `@ComponentScan` — escanea el paquete `com.practice.taskmanager` y subpaquetes buscando clases anotadas (`@Component`, `@Service`, `@Repository`, `@RestController`) para registrarlas como **beans**.
- Un **bean** es simplemente un objeto cuyo ciclo de vida (creación, inyección de dependencias, destrucción) lo maneja el contenedor de Spring (el "Application Context"), no tu código con `new`.

---

## 2. Capas: Controller → Service → Repository

Flujo de una request `POST /api/tasks`:

```
HTTP request
   ↓
Controller   (recibe JSON, valida forma, traduce a/desde HTTP)
   ↓
Service      (logica de negocio: reglas, orquestacion)
   ↓
Repository   (acceso a datos: SQL generado por Hibernate)
   ↓
H2 (en memoria)
```

Cada capa solo conoce a la de abajo **a través de una interface** (`TaskService`,
`TaskRepository`). Ver sección 8 (SOLID) para por qué importa esto.

---

## 3. Entity — [`model/Task.java`](src/main/java/com/practice/taskmanager/model/Task.java)

- `@Entity` — esta clase mapea a una tabla (`task`, por defecto el nombre de la clase en minúsculas).
- `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` — clave primaria autoincremental, generada por la DB.
- `@Enumerated(EnumType.STRING)` — el enum `Priority` se guarda como texto (`"HIGH"`) en vez de como índice numérico (más legible y estable si reordenás el enum).
- Constructor vacío **obligatorio**: JPA/Hibernate lo usa por reflexión para reconstruir objetos al leer filas de la DB.
- La entidad es un objeto Java normal (POJO) con getters/setters — Hibernate la convierte en filas y viceversa ("ORM": Object-Relational Mapping).

---

## 4. Repository — [`repository/TaskRepository.java`](src/main/java/com/practice/taskmanager/repository/TaskRepository.java)

```java
public interface TaskRepository extends JpaRepository<Task, Long> { ... }
```

- Es **solo una interface** — no hay clase `TaskRepositoryImpl` escrita a mano.
- Spring Data JPA genera una implementación en tiempo de ejecución (un proxy) que traduce los métodos a SQL via Hibernate.
- `JpaRepository<Task, Long>` ya trae `save`, `findById`, `findAll`, `deleteById`, `existsById`, `count`, etc.
- **Query methods**: métodos como `findByCompleted(boolean)` o `findByPriority(Priority)` — Spring Data parsea el *nombre* del método (`findBy` + nombre de campo) y genera el `WHERE` correspondiente. No se escribe SQL.

---

## 5. JPA / Hibernate — la relación entre las dos

- **JPA** (Java Persistence API) es una *especificación* (un conjunto de interfaces y anotaciones: `@Entity`, `@Id`, `EntityManager`, etc.) — define el "contrato".
- **Hibernate** es la *implementación* más usada de esa especificación — el motor real que traduce objetos Java a SQL.
- `spring-boot-starter-data-jpa` trae ambos: la API (JPA) + la implementación (Hibernate) + Spring Data (la capa que genera los repositories).
- `spring.jpa.hibernate.ddl-auto=update` (en `application.properties`) — Hibernate lee las clases `@Entity` y crea/ajusta las tablas automáticamente. Útil para practicar; en proyectos reales se usan migraciones versionadas (Flyway/Liquibase) en vez de `update`.
- `spring.jpa.show-sql=true` — imprime el SQL real que genera Hibernate en la consola. Útil para entender qué está pasando "debajo".
- **No hay conexión a una DB externa**: H2 corre embebida en memoria (`jdbc:h2:mem:taskdb`), se crea vacía en cada arranque. La teoría de JPA/Hibernate es idéntica si mañana esto fuera Postgres/MySQL — solo cambiaría la configuración del `DataSource`.

---

## 6. Service — [`service/TaskService.java`](src/main/java/com/practice/taskmanager/service/TaskService.java) + [`TaskServiceImpl.java`](src/main/java/com/practice/taskmanager/service/TaskServiceImpl.java)

- `TaskService` es la interface — define **qué** se puede hacer (`findAll`, `create`, `complete`, ...).
- `TaskServiceImpl` es la implementación — define **cómo**: contiene la lógica de negocio (ej. `delete` verifica que exista antes de borrar; `complete` setea `completed = true`).
- `@Service` — marca la clase como bean de la capa de negocio (semánticamente igual que `@Component`, pero comunica intención).
- Acá vive la traducción entre `Task` (entidad) y `TaskRequest`/`TaskResponse` (DTOs) — el Service es el límite entre "mundo de la DB" y "mundo de la API".

---

## 7. Controller — [`controller/TaskController.java`](src/main/java/com/practice/taskmanager/controller/TaskController.java)

- `@RestController` = `@Controller` + `@ResponseBody` — cada método devuelve el body de la respuesta (Spring lo serializa a JSON con Jackson automáticamente), no el nombre de una vista HTML.
- `@RequestMapping("/api/tasks")` — prefijo común para todos los endpoints de la clase.
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping` — mapean métodos HTTP a métodos Java.
- `@PathVariable` — extrae partes de la URL (`/api/tasks/{id}` → `Long id`).
- `@RequestParam(required = false)` — query params opcionales (`?completed=true`).
- `@RequestBody` — deserializa el JSON del body a un objeto Java (`TaskRequest`).
- `@Valid` — dispara las validaciones de `jakarta.validation` (`@NotBlank`, `@NotNull`) declaradas en el DTO. Si fallan, Spring lanza `MethodArgumentNotValidException` *antes* de que el método del controller se ejecute.
- `ResponseEntity<T>` — permite controlar el código de estado HTTP explícitamente (`201 Created`, `204 No Content`), además del body.

---

## 8. Dependency Injection (DI) e Inversion of Control (IoC)

```java
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {  // <- constructor injection
        this.taskRepository = taskRepository;
    }
}
```

- **IoC (Inversion of Control)**: en vez de que `TaskServiceImpl` decida *cuándo y cómo* crear su `TaskRepository` (`new ...`), delega esa decisión al contenedor de Spring. El control se "invierte": el framework construye y conecta los objetos.
- **DI (Dependency Injection)**: el mecanismo concreto — Spring ve que `TaskServiceImpl` necesita un `TaskRepository` en su constructor, encuentra (o crea) el bean correspondiente, y lo pasa automáticamente.
- **Constructor injection** (la usada aquí) es la forma recomendada:
  - Permite que las dependencias sean `final` → inmutabilidad.
  - El objeto nunca existe "a medias" (sin sus dependencias).
  - Facilita testing: en `TaskServiceImplTest` se hace `new TaskServiceImpl(mockRepository)` a mano, sin Spring.
- Cadena completa en este proyecto: `TaskController` recibe `TaskService` (interface) → Spring inyecta `TaskServiceImpl` → que recibe `TaskRepository` (interface) → Spring inyecta el proxy generado por Spring Data JPA.
- Nadie en esta cadena escribe `new TaskServiceImpl()` ni `new TaskRepository()`. Eso es DI en acción.

---

## 9. Principios SOLID — aplicados en este proyecto

### S — Single Responsibility Principle
Cada clase tiene **una sola razón para cambiar**:
- `Task` — solo representa los datos de una tarea (mapeo a tabla).
- `TaskRepository` — solo acceso a datos.
- `TaskServiceImpl` — solo lógica de negocio.
- `TaskController` — solo traducción HTTP ↔ Service.
- `GlobalExceptionHandler` — solo manejo de errores, separado de la lógica normal.

Si cambia el formato de la API (ej. agregar paginación), se toca el Controller.
Si cambia una regla de negocio (ej. "no se puede completar una task ya completada"), se toca el Service. Cada cambio tiene un único lugar "natural".

### O — Open/Closed Principle
El sistema está abierto a *extensión* pero cerrado a *modificación*:
- Se puede agregar una nueva implementación de `TaskService` (ej. `CachedTaskServiceImpl`) sin modificar `TaskController` — solo registrar el nuevo bean.
- `GlobalExceptionHandler` permite agregar nuevos `@ExceptionHandler` para nuevas excepciones sin tocar los controllers que las lanzan.

### L — Liskov Substitution Principle
Cualquier implementación de `TaskService` debe poder sustituir a otra sin romper el comportamiento esperado por `TaskController`:
- `TaskController` solo conoce el contrato `TaskService` (los métodos y qué garantizan: `findById` devuelve una `TaskResponse` o lanza `TaskNotFoundException`).
- Si una implementación alternativa devolviera `null` en vez de lanzar la excepción, rompería el contrato — no sería un sustituto válido aunque compile.
- En testing: `TaskServiceImplTest` reemplaza `TaskRepository` real por un `@Mock` — funciona porque el mock respeta el mismo contrato (`JpaRepository`/`TaskRepository`).

### I — Interface Segregation Principle
Las interfaces son específicas del cliente que las usa, no "una interface gigante para todo":
- `TaskService` expone solo las operaciones que el `TaskController` de tasks necesita — no hay métodos de, por ejemplo, gestión de usuarios mezclados ahí.
- `TaskRepository` extiende `JpaRepository<Task, Long>` y agrega *solo* los query methods específicos de `Task` (`findByCompleted`, `findByPriority`), no operaciones genéricas de todas las entidades del sistema.

### D — Dependency Inversion Principle
Los módulos de alto nivel no dependen de detalles de bajo nivel, ambos dependen de abstracciones:
- `TaskController` (alto nivel) depende de `TaskService` (abstracción), no de `TaskServiceImpl` (detalle).
- `TaskServiceImpl` (alto nivel respecto a la DB) depende de `TaskRepository` (abstracción), no de Hibernate/H2 directamente.
- Esto es lo que hace posible el DI de la sección 8 y el testing con mocks: se puede "enchufar" cualquier implementación que cumpla el contrato.

---

## 10. Testing

Dos niveles, ambos en este proyecto:

### Unitario — [`TaskServiceImplTest`](src/test/java/com/practice/taskmanager/service/TaskServiceImplTest.java)
- No levanta Spring (`@ExtendWith(MockitoExtension.class)`, sin `@SpringBootTest`).
- `@Mock` crea un `TaskRepository` falso; se inyecta a mano via `new TaskServiceImpl(mockRepo)`.
- Rápido, aislado — prueba *solo* la lógica de `TaskServiceImpl` (ej. que `delete` valide existencia antes de borrar).
- Posible **porque** `TaskServiceImpl` depende de la interface `TaskRepository` (DIP).

### Integración — [`TaskControllerTest`](src/test/java/com/practice/taskmanager/controller/TaskControllerTest.java)
- `@SpringBootTest` levanta el contexto completo de Spring (todas las capas reales + H2 en memoria).
- `@AutoConfigureMockMvc` + `MockMvc` simulan requests HTTP sin abrir un puerto real.
- Prueba el flujo end-to-end: JSON de entrada → validación (`@Valid`) → Service → Repository → Hibernate → H2 → JSON de salida.
- Más lento que el unitario, pero detecta problemas de integración (mapeo JPA, serialización, validación) que un mock no puede detectar.

---

## Estructura del proyecto (referencia)

```
src/main/java/com/practice/taskmanager/
├── TaskManagerApplication.java   — entry point (@SpringBootApplication)
├── model/
│   ├── Task.java                 — @Entity
│   └── Priority.java             — enum (LOW/MEDIUM/HIGH)
├── repository/
│   └── TaskRepository.java       — interface extends JpaRepository
├── service/
│   ├── TaskService.java          — interface (contrato)
│   └── TaskServiceImpl.java      — @Service, logica de negocio + DI
├── controller/
│   └── TaskController.java       — @RestController, endpoints REST
├── dto/
│   ├── TaskRequest.java          — record + @Valid (@NotBlank, @NotNull)
│   └── TaskResponse.java         — record, mapea desde Task
└── exception/
    ├── TaskNotFoundException.java
    └── GlobalExceptionHandler.java  — @RestControllerAdvice
```
