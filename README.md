# TP Ejemplo — Aplicaciones Interactivas UADE

Sistema de créditos y cobranzas desarrollado como ejemplo didáctico para la materia
**Aplicaciones Interactivas (3.4.082)** de la UADE.

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Java 21 + Spring Boot 3.4.3 |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | H2 (en memoria) |
| Seguridad | Spring Security + JWT (jjwt 0.12.6) |
| Build | Maven |
| Frontend | React 18 + Vite 7 |
| Routing | React Router v7 |
| Estado global | Redux Toolkit + React-Redux |

---

## Estructura del proyecto

```
tpejemplo/
├── backend/               → Proyecto Spring Boot (Maven)
│   └── src/main/java/com/uade/tpejemplo/
│       ├── config/        → SecurityConfig (JWT + stateless)
│       ├── controller/    → AuthController, ClienteController, CreditoController, CobranzaController, DashboardController
│       ├── dto/
│       │   ├── request/   → ClienteRequest, CreditoRequest, CobranzaRequest, LoginRequest, RegisterRequest
│       │   └── response/  → ClienteResponse, CreditoResponse, CuotaResponse, CobranzaResponse, AuthResponse, DashboardResumenResponse, CreditosPorEstadoResponse
│       ├── exception/     → ResourceNotFoundException, BusinessException, GlobalExceptionHandler
│       ├── model/         → Cliente, Credito, Cuota, CuotaId, Cobranza, Usuario, Rol
│       ├── repository/    → ClienteRepository, CreditoRepository, CuotaRepository, CobranzaRepository, UsuarioRepository
│       ├── security/      → JwtUtil, JwtAuthFilter, UserDetailsServiceImpl
│       └── service/
│           ├── ClienteService / ClienteServiceImpl
│           ├── CreditoService / CreditoServiceImpl
│           ├── CobranzaService / CobranzaServiceImpl
│           └── DashboardService / DashboardServiceImpl
└── frontend/              → Proyecto React + Vite
    └── src/
        ├── api/           → apiClient.js, auth.js, clientes.js, creditos.js, cobranzas.js
        ├── components/    → Navbar.jsx, PrivateRoute.jsx
        ├── store/
        │   ├── index.js                  → configureStore (combina reducers)
        │   └── slices/
        │       ├── authSlice.js          → login/register thunks + logout
        │       ├── clientesSlice.js      → fetchClientes + addCliente
        │       ├── creditosSlice.js      → fetchCreditosPorCliente + addCredito
        │       └── cobranzasSlice.js     → fetchCobranzasPorCredito + addCobranza
        └── pages/         → Login.jsx, Register.jsx, Clientes.jsx, Creditos.jsx, Cobranzas.jsx
```

---

## Modelo de datos

### Cliente
| Campo | Tipo | Descripción |
|-------|------|-------------|
| dni | String (PK) | DNI del cliente |
| nombre | String | Nombre completo |

### Credito
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long (PK, auto) | Identificador |
| cliente | FK → Cliente | Dueño del crédito |
| deudaOriginal | BigDecimal | Monto total del crédito |
| fecha | LocalDate | Fecha de otorgamiento |
| importeCuota | BigDecimal | Valor de cada cuota |
| cantidadCuotas | Integer | Número de cuotas |

### Cuota
| Campo | Tipo | Descripción |
|-------|------|-------------|
| idCredito + idCuota | PK compuesta (@EmbeddedId) | Clave compuesta |
| credito | FK → Credito | Crédito al que pertenece |
| fechaVencimiento | LocalDate | Vencimiento mensual auto-generado |

> Al crear un crédito se generan automáticamente N cuotas con vencimiento mensual.

### Cobranza
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long (PK, auto) | Identificador |
| cuota | FK → Cuota | Cuota que se está pagando |
| importe | BigDecimal | Importe cobrado |

### Usuario
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long (PK, auto) | Identificador |
| username | String (unique) | Nombre de usuario |
| password | String (BCrypt) | Contraseña encriptada |
| rol | Enum (ADMIN/USER) | Rol del usuario |

---

## API REST

### Autenticación (pública)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/register` | Registrar usuario, devuelve token JWT |
| POST | `/api/auth/login` | Iniciar sesión, devuelve token JWT |

### Clientes (requiere JWT)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/clientes` | Crear cliente |
| GET | `/api/clientes` | Listar todos |
| GET | `/api/clientes/{dni}` | Buscar por DNI |

### Créditos (requiere JWT)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/creditos` | Crear crédito (genera cuotas automáticamente) |
| GET | `/api/creditos/{id}` | Buscar por ID (incluye cuotas con estado pagada/pendiente) |
| GET | `/api/creditos/cliente/{dni}` | Créditos de un cliente |

### Cobranzas (requiere JWT)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/cobranzas` | Registrar pago de una cuota |
| GET | `/api/cobranzas/credito/{idCredito}` | Cobranzas de un crédito |

### Dashboard (requiere JWT)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/dashboard/resumen` | Devuelve métricas agregadas del sistema |

---

## Módulo 5: Dashboard de estadísticas

### Descripción del módulo

Para la Entrega 1 del módulo 5 se implementó un dashboard de estadísticas orientado a exponer un resumen general del estado de los créditos y cobranzas del sistema.

La resolución se apoyó en el modelo real ya existente, sin crear una entidad nueva ni persistir información específica del dashboard.

Según la aclaración realizada por el profesor para este módulo, la Entrega 1 del Dashboard se resuelve sin CRUD propio del dashboard y únicamente con backend orientado a consultas y métricas.

### Objetivo funcional

El objetivo del módulo es permitir la consulta de métricas agregadas del negocio mediante un endpoint protegido con JWT.

El resumen expone:

- total de créditos
- monto total prestado
- monto total cobrado
- porcentaje de recupero
- cantidad de créditos activos
- cantidad de créditos en mora
- créditos por estado

### Modelo de datos utilizado

El dashboard se calcula a partir de estas entidades del proyecto:

- Cliente
- Credito
- Cuota
- Cobranza

Relaciones utilizadas:

- Cliente 1 --- N Credito
- Credito 1 --- N Cuota
- Cobranza N --- 1 Cuota

### Decisión de diseño

Se decidió implementar el dashboard como un módulo de métricas calculadas y no como una entidad persistente.

Esta decisión también responde a la indicación específica del profesor para el módulo Dashboard: en esta entrega no correspondía implementar altas, bajas ni modificaciones del dashboard, sino exponer endpoints de consulta sobre métricas del sistema.

Por ese motivo:

- no se creó entidad Dashboard
- no se creó tabla Dashboard
- no se creó DashboardRepository
- no se implementó CRUD del dashboard

Los componentes agregados para este módulo fueron:

- DashboardController
- DashboardService
- DashboardServiceImpl
- DashboardResumenResponse
- CreditosPorEstadoResponse

Los repositories se extendieron de forma mínima en los puntos necesarios:

- CreditoRepository
- CobranzaRepository

### Criterio de estados

El estado de cada crédito se determina al momento de consultar el resumen:

- FINALIZADO: todas las cuotas pagadas
- EN_MORA: existe al menos una cuota vencida e impaga
- ACTIVO: no está finalizado y tampoco tiene cuotas vencidas impagas

### Endpoints implementados y probados

En esta entrega se validaron estos endpoints reales del proyecto:

- POST `/api/auth/login`
- GET `/api/dashboard/resumen`

#### Login

Request:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "martina.f",
  "password": "123456"
}
```

Response exitosa:

```json
{
  "token": "JWT_TOKEN",
  "username": "martina.f",
  "rol": "USER"
}
```

#### Resumen del dashboard

Request:

```http
GET /api/dashboard/resumen
Authorization: Bearer JWT_TOKEN
```

Response con base vacía:

```json
{
  "totalCreditos": 0,
  "montoTotalPrestado": 0,
  "montoTotalCobrado": 0,
  "porcentajeRecupero": 0,
  "cantidadCreditosActivos": 0,
  "cantidadCreditosEnMora": 0,
  "creditosPorEstado": []
}
```

Response con datos reales cargados:

```json
{
  "totalCreditos": 1,
  "montoTotalPrestado": 200000.00,
  "montoTotalCobrado": 23000.00,
  "porcentajeRecupero": 11.50,
  "cantidadCreditosActivos": 0,
  "cantidadCreditosEnMora": 1,
  "creditosPorEstado": [
    {
      "estado": "ACTIVO",
      "cantidad": 0
    },
    {
      "estado": "EN_MORA",
      "cantidad": 1
    },
    {
      "estado": "FINALIZADO",
      "cantidad": 0
    }
  ]
}
```

### Cómo probar el dashboard con JWT

1. Autenticarse con `POST /api/auth/login`
2. Copiar el token JWT de la respuesta
3. Llamar `GET /api/dashboard/resumen` con header `Authorization: Bearer <token>`

Ejemplo con `curl`:

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"martina.f","password":"123456"}'
```

```bash
curl -i http://localhost:8080/api/dashboard/resumen \
  -H "Authorization: Bearer JWT_TOKEN"
```

Validación realizada en el proyecto:

- login correcto con obtención de token JWT
- acceso denegado al dashboard sin token
- respuesta `200 OK` al dashboard con token válido
- respuesta correcta tanto con base vacía como con datos reales cargados
- lógica del dashboard validada además con tests de integración del service

### Manejo básico de errores y comportamiento con base vacía

- sin autenticación, el endpoint del dashboard rechaza la solicitud
- con base vacía, el endpoint responde con ceros y lista vacía, sin devolver valores nulos

### Métrica no implementada en esta entrega

No se implementó la métrica de cobranzas por mes.

La razón es que el modelo actual de `Cobranza` no guarda una fecha real de cobranza, por lo que esa estadística no puede calcularse de forma consistente sin cambiar el alcance del proyecto.

### Conclusión breve del módulo

La Entrega 1 del módulo 5 quedó resuelta como un dashboard de métricas calculadas a partir del modelo existente. La solución mantiene la arquitectura real del proyecto, reutiliza la seguridad JWT ya implementada y expone un único endpoint protegido para consultar el resumen estadístico del sistema.

---

## Seguridad JWT

El flujo de autenticación es:

```
1. POST /api/auth/register  →  { token, username, rol }
2. POST /api/auth/login     →  { token, username, rol }
3. Resto de endpoints       →  Header: Authorization: Bearer <token>
```

- Token firmado con HMAC-SHA384
- Expiración: 24 horas
- Sesión stateless (sin HttpSession)
- Contraseñas encriptadas con BCrypt

---

## Manejo de errores

Todos los errores devuelven un `ErrorResponse` uniforme:

```json
{
  "status": 400,
  "error": "Error de negocio",
  "mensajes": ["La cuota 1 del crédito 1 ya fue pagada"],
  "timestamp": "2026-03-03T14:00:00"
}
```

Excepciones manejadas por `@RestControllerAdvice`:
- `ResourceNotFoundException` → 404
- `BusinessException` → 400 (reglas de negocio)
- `MethodArgumentNotValidException` → 400 (validaciones `@Valid`)
- `Exception` genérica → 500

---

## Frontend React + Redux

### Redux store

El estado global está dividido en 4 slices:

| Slice | Estado | Acciones |
|-------|--------|----------|
| `auth` | `user`, `loading`, `error` | `loginThunk`, `registerThunk`, `logout` |
| `clientes` | `lista`, `loading`, `error` | `fetchClientes`, `addCliente` |
| `creditos` | `lista`, `loading`, `error` | `fetchCreditosPorCliente`, `addCredito`, `clearCreditos` |
| `cobranzas` | `lista`, `loading`, `error` | `fetchCobranzasPorCredito`, `addCobranza`, `clearCobranzas` |

Cada operación asíncrona usa `createAsyncThunk`, que maneja automáticamente los estados `pending / fulfilled / rejected`.

### Otros conceptos del frontend

- **PrivateRoute** redirige a `/login` si `state.auth.user` es null
- **Navbar** despacha `logout()` y limpia `localStorage`
- **apiClient.js** centraliza todas las llamadas fetch con el header `Authorization: Bearer <token>`
- El proxy de Vite redirige `/api/*` → `localhost:8080` (evita CORS en desarrollo)

### Páginas
| Ruta | Componente | Acceso |
|------|-----------|--------|
| `/login` | Login.jsx | Público |
| `/register` | Register.jsx | Público |
| `/clientes` | Clientes.jsx | Privado |
| `/creditos` | Creditos.jsx | Privado |
| `/cobranzas` | Cobranzas.jsx | Privado |

---

## Cómo correr el proyecto

### Backend
```bash
cd backend
mvn spring-boot:run
# Corre en http://localhost:8080
# Consola H2: http://localhost:8080/h2-console
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# Corre en http://localhost:5173
```

---

## Temas de la materia cubiertos

| Unidad | Tema | Implementado en |
|--------|------|----------------|
| I | Spring Boot, arquitectura, estructura de proyectos | Toda la capa backend |
| II | Hibernate/JPA, entidades, repositorios | `model/`, `repository/` |
| II | Seguridad con JWT | `security/`, `config/SecurityConfig` |
| III | React + Vite, componentes, props | `pages/`, `components/` |
| III | React Hooks (`useState`, `useEffect`) | Todas las páginas |
| III | React Router | `App.jsx`, `PrivateRoute` |
| IV | Fetch, consumo de API | `api/` |
| IV | Renderizado condicional | Estados de carga y error en cada página |
| V | Redux I y II: acciones, reducers, store, thunks | `store/slices/`, `store/index.js` |
