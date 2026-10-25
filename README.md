# springboot-practice

Proyecto de práctica para reforzar Spring Boot — una API REST de "Task Manager"
(mismo dominio que `angular-practice`, para que el modelo mental de datos sea
familiar). Ver [CONCEPTOS.md](CONCEPTOS.md) para el resumen teórico.

## Requisitos

- Java 17
- Maven (`mvn`). Si no lo tenés instalado: `brew install maven`.

## Correr la app

```bash
mvn spring-boot:run
```

Levanta en `http://localhost:8080`. La base de datos es H2 **en memoria**:
se crea vacía en cada arranque, no requiere instalación.

Consola H2 (opcional, para ver las tablas): `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:taskdb`
- user: `sa`, password: (vacío)

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/tasks` | Lista todas las tasks |
| GET | `/api/tasks?completed=true` | Filtra por estado |
| GET | `/api/tasks/{id}` | Busca una task por id |
| POST | `/api/tasks` | Crea una task — body: `{"title": "...", "priority": "LOW\|MEDIUM\|HIGH"}` |
| PUT | `/api/tasks/{id}` | Actualiza title/priority |
| PATCH | `/api/tasks/{id}/complete` | Marca como completada |
| DELETE | `/api/tasks/{id}` | Elimina |

### Probar con curl

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Aprender Spring Boot","priority":"HIGH"}'

curl http://localhost:8080/api/tasks

curl -X PATCH http://localhost:8080/api/tasks/1/complete
```

## Tests

```bash
mvn test
```

- `TaskServiceImplTest` — unitario, con `@Mock` (Mockito) del repository.
- `TaskControllerTest` — integración, con `@SpringBootTest` + `MockMvc`.

## Estructura

```
src/main/java/com/practice/taskmanager/
├── model/        — @Entity Task, enum Priority
├── repository/   — TaskRepository (interface, extends JpaRepository)
├── service/      — TaskService (interface) + TaskServiceImpl
├── controller/   — TaskController (@RestController)
├── dto/          — TaskRequest / TaskResponse
└── exception/    — TaskNotFoundException + GlobalExceptionHandler
```
