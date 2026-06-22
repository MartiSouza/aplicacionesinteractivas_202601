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
│       ├── controller/    → AuthController, ClienteController, CreditoController, CobranzaController, DashboardController, AdminController
│       ├── dto/
│       │   ├── request/   → ClienteRequest, CreditoRequest, CobranzaRequest, LoginRequest, RegisterRequest, PermisosRequest
│       │   └── response/  → ClienteResponse, CreditoResponse, CuotaResponse, CobranzaResponse, AuthResponse, DashboardResumenResponse, CreditosPorEstadoResponse, UsuarioResponse
│       ├── exception/     → ResourceNotFoundException, BusinessException, GlobalExceptionHandler
│       ├── model/         → Cliente, Credito, Cuota, CuotaId, Cobranza, Usuario, Rol
│       ├── repository/    → ClienteRepository, CreditoRepository, CuotaRepository, CobranzaRepository, UsuarioRepository
│       ├── security/      → JwtUtil, JwtAuthFilter, UserDetailsServiceImpl
│       └── service/
│           ├── ClienteService / ClienteServiceImpl
│           ├── CreditoService / CreditoServiceImpl
│           ├── CobranzaService / CobranzaServiceImpl
│           ├── UsuarioService / UsuarioServiceImpl
│           └── DashboardService / DashboardServiceImpl
└── frontend/              → Proyecto React + Vite
    └── src/
  ├── api/           → apiClient.js, auth.js, admin.js, clientes.js, creditos.js, cobranzas.js, dashboard.js
        ├── components/    → Navbar.jsx, PrivateRoute.jsx
        ├── store/
        │   ├── index.js                  → configureStore (combina reducers)
        │   └── slices/
    │       ├── authSlice.js          → login/register thunks + logout + usuario autenticado con permisos
  │       ├── clientesSlice.js      → fetchClientes + add/edit/removeCliente
    │       ├── creditosSlice.js      → fetchCreditosPorCliente + addCredito + anularCredito
  │       ├── cobranzasSlice.js     → fetchCobranzasPorCredito + addCobranza + anularCobranza
    │       ├── permisosSlice.js      → fetchUsuariosPermisos + updatePermisosUsuario
  │       └── dashboardSlice.js     → fetchDashboardResumen
  ├── test/          → setupTests.js
  └── pages/         → Login.jsx, Register.jsx, Clientes.jsx, Creditos.jsx, Cobranzas.jsx, Dashboard.jsx, GestorPermisos.jsx
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
| anulado | boolean | Baja lógica del crédito |

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
| fechaCobranza | LocalDate | Fecha de registración de la cobranza |
| anulada | boolean | Baja lógica de la cobranza |

### Usuario
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long (PK, auto) | Identificador |
| username | String (unique) | Nombre de usuario |
| password | String (BCrypt) | Contraseña encriptada |
| rol | Enum (ADMIN/USER) | Rol del usuario |
| puedeAnularCredito | boolean | Permiso específico para anular créditos |
| puedeAnularCobranza | boolean | Permiso específico para anular cobranzas |

---

## API REST

### Autenticación (pública)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/register` | Registrar usuario, devuelve token JWT |
| POST | `/api/auth/login` | Iniciar sesión, devuelve token JWT |

El login y el registro devuelven además el rol y los permisos del usuario autenticado para que el frontend pueda decidir qué acciones mostrar.

### Administración de permisos (solo ADMIN)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/admin/usuarios` | Lista usuarios con rol `USER` y sus permisos actuales |
| PUT | `/api/admin/usuarios/{id}/permisos` | Actualiza `puedeAnularCredito` y `puedeAnularCobranza` |

### Clientes (requiere JWT)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/clientes` | Crear cliente |
| GET | `/api/clientes` | Listar todos |
| GET | `/api/clientes/{dni}` | Buscar por DNI |
| PUT | `/api/clientes/{dni}` | Editar cliente |
| DELETE | `/api/clientes/{dni}` | Eliminar cliente si no tiene créditos asociados |

### Créditos (requiere JWT)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/creditos` | Crear crédito (genera cuotas automáticamente) |
| GET | `/api/creditos/{id}` | Buscar por ID (incluye cuotas con estado pagada/pendiente) |
| GET | `/api/creditos/cliente/{dni}` | Créditos de un cliente |
| DELETE | `/api/creditos/{id}` | Anular crédito por baja lógica si no tiene cobranzas registradas |

### Cobranzas (requiere JWT)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/cobranzas` | Registrar pago de una cuota |
| GET | `/api/cobranzas/credito/{idCredito}` | Cobranzas de un crédito |
| DELETE | `/api/cobranzas/{id}` | Anular cobranza del día por baja lógica |

### Dashboard (requiere JWT)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/dashboard/resumen` | Devuelve métricas agregadas del sistema |
| GET | `/api/dashboard/creditos-por-estado` | Devuelve la distribución de créditos por estado |

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

## Frontend – Entrega 2

### Descripción general

En esta segunda entrega del frontend se integró el módulo 5, Dashboard de estadísticas, respetando la arquitectura y las tecnologías definidas por el proyecto base. La implementación se realizó con React, React Router y Redux Toolkit, manteniendo la comunicación con el backend mediante la API REST existente y reutilizando el esquema de autenticación con JWT ya provisto por el sistema.

El dashboard no se resolvió como una entidad propia ni como un CRUD independiente, ya que funcionalmente representa una vista de métricas calculadas a partir de información existente del sistema. Por ese motivo, el frontend se limita a autenticarse, consumir el endpoint correspondiente y visualizar los indicadores devueltos por el backend.

### Autenticación y acceso protegido

Se reutilizó el flujo de autenticación ya implementado en el proyecto base. El login existente continúa siendo el punto de entrada al sistema y, una vez autenticado el usuario, el token JWT se utiliza para acceder a las pantallas protegidas.

Para preservar la seguridad y la coherencia con la arquitectura original, se mantuvo el uso de rutas protegidas mediante `PrivateRoute`. De esta forma, tanto el acceso al dashboard como al resto de los módulos autenticados depende de una sesión válida. Además, se incorporó el manejo de sesión expirada ante respuestas `401`, limpiando la sesión local y redirigiendo nuevamente al login.

### Módulo Dashboard de estadísticas

Se agregó una nueva pantalla accesible mediante la ruta `/dashboard`, integrada dentro de la navegación autenticada del frontend. Esta pantalla consume el endpoint `GET /api/dashboard/resumen` y muestra las métricas principales del sistema en una vista resumida y clara.

Las métricas visualizadas incluyen:

- total de créditos
- monto total prestado
- monto total cobrado
- porcentaje de recupero
- cantidad de créditos activos
- cantidad de créditos en mora
- distribución de créditos por estado

Además de la visualización de datos, la pantalla contempla estados básicos de carga y error, de modo que el comportamiento sea consistente frente a demoras o fallas en la respuesta del backend.

### Integración con la arquitectura existente

La incorporación del dashboard se realizó sin modificar la estructura general del frontend. Se respetó el patrón ya utilizado por el proyecto, organizado por dominio en tres capas: acceso a API, slice de Redux Toolkit y página de visualización.

En este sentido, el módulo Dashboard se integró siguiendo el esquema `api + slice + page`, reutilizando el `apiClient` existente para las llamadas HTTP y el store global para la gestión del estado. No fue necesario rehacer el login, el router ni la estructura base del proyecto.

### Alta, baja y modificación sobre entidades reales

Dado que el dashboard no admite ABM propio por no ser una entidad persistente, las operaciones de alta, baja y modificación se resolvieron sobre una entidad real del sistema efectivamente expuesta para su gestión: Cliente.

En el frontend quedó disponible la gestión básica de clientes, incluyendo:

- alta de cliente
- edición de cliente
- eliminación de cliente

La baja de cliente respeta una regla de negocio validada en backend: no se permite eliminar un cliente si posee créditos asociados. De esta forma, la aplicación conserva integridad funcional y evita operaciones inconsistentes sobre datos relacionados.

### Testing del frontend

Se agregó una base mínima de testing para validar el módulo incorporado, manteniendo una solución simple y acorde al proyecto. Para ello se utilizó Vitest, integrado con la configuración actual del frontend basada en Vite.

Se implementaron pruebas sobre:

- `dashboardSlice`
- `Dashboard.jsx`

Estas pruebas validan los escenarios principales del módulo:

- estado de carga
- manejo de error
- render correcto de métricas con datos disponibles

### Cierre

La Entrega 2 del frontend quedó resuelta manteniendo la arquitectura del sistema base, sin alterar el stack tecnológico ni introducir un CRUD artificial para el dashboard. El módulo principal implementado fue el Dashboard de estadísticas como vista protegida de métricas, complementado con operaciones de alta, modificación y baja sobre la entidad real Cliente para reforzar el cumplimiento de la consigna general sobre entidades efectivamente gestionables del sistema.

---

## Entrega 3 – Estado global, permisos y anulaciones

### Alcance de la entrega

En esta tercera entrega se completó la integración del estado global con Redux Toolkit y se incorporó el manejo de roles y permisos sobre funcionalidades sensibles del sistema. El trabajo se apoyó sobre la arquitectura ya existente y mantuvo el mismo stack tecnológico en backend y frontend.

El foco principal de esta etapa quedó en tres frentes:

- consolidar el estado global de autenticación y del módulo Dashboard
- incorporar un gestor de permisos visible solo para usuarios `ADMIN`
- resolver la anulación lógica de créditos y cobranzas con reglas de negocio y validación desde backend

### Permisos incorporados

Se agregaron dos permisos específicos a la entidad `Usuario`:

- `puedeAnularCredito`
- `puedeAnularCobranza`

Ambos permisos viajan también en la respuesta de login y registro, de modo que el frontend pueda decidir si muestra o no las acciones de anulación según el usuario autenticado.

### Gestor de permisos

Se incorporó un módulo administrativo para la gestión de permisos de usuarios con rol `USER`.

En backend se agregaron:

- `AdminController`
- `UsuarioService` y `UsuarioServiceImpl`
- `UsuarioResponse`
- `PermisosRequest`

La seguridad del módulo se resuelve con `@PreAuthorize("hasRole('ADMIN')")` y `@EnableMethodSecurity`, dejando el acceso restringido exclusivamente a administradores.

En frontend se agregó una pantalla específica `GestorPermisos.jsx`, conectada mediante `permisosSlice.js` y `api/admin.js`. El acceso a esta ruta se protege con `PrivateRoute` extendido por rol, y el link de navegación solo aparece cuando el usuario autenticado tiene `rol === 'ADMIN'`.

Desde esa pantalla el administrador puede:

- ver los usuarios `USER`
- consultar el estado actual de sus permisos
- activar o desactivar permisos con actualización inmediata al backend

### Anulación de créditos

La anulación de créditos se resolvió con baja lógica sobre la entidad `Credito`, agregando el campo `anulado`.

Se incorporó el endpoint:

- `DELETE /api/creditos/{id}`

Reglas implementadas:

- solo puede ejecutarlo un usuario autenticado con `puedeAnularCredito = true`
- no se elimina el crédito de la base de datos
- si el crédito tiene cobranzas registradas sobre cualquiera de sus cuotas, la anulación se rechaza con `BusinessException`

En frontend, la página de créditos muestra el botón `Anular` solo si el usuario tiene permiso. Cuando el crédito queda anulado:

- se oculta el botón
- se muestra una marca visual `ANULADO`
- el bloque queda diferenciado para reflejar la baja lógica

### Anulación de cobranzas

La anulación de cobranzas también se resolvió con baja lógica, agregando a la entidad `Cobranza` los campos `fechaCobranza` y `anulada`.

Se incorporó el endpoint:

- `DELETE /api/cobranzas/{id}`

Reglas implementadas:

- solo puede ejecutarlo un usuario autenticado con `puedeAnularCobranza = true`
- solo se pueden anular cobranzas registradas el mismo día
- no se elimina físicamente la cobranza
- una cobranza anulada deja de contarse como pago válido del sistema

En frontend, la página de cobranzas muestra el botón `Anular` únicamente cuando el usuario tiene permiso y la cobranza sigue vigente. Luego de anularla:

- la fila permanece visible
- se muestra el estado `ANULADA`
- desaparece la acción de anular

### Estado global con Redux Toolkit

Durante esta entrega se consolidó el uso de Redux Toolkit para centralizar la lógica del frontend.

Slices involucrados:

- `authSlice` → login, logout, registro, token, rol y permisos del usuario autenticado
- `dashboardSlice` → resumen y distribución por estado del módulo principal elegido
- `permisosSlice` → usuarios gestionables y actualización de permisos
- `creditosSlice` → listado, alta y anulación de créditos
- `cobranzasSlice` → listado, alta y anulación de cobranzas

Todas las operaciones asíncronas se resolvieron con `createAsyncThunk` y contemplan estados `pending`, `fulfilled` y `rejected`.

### Comportamiento del dashboard frente a anulaciones

El dashboard continúa calculándose a partir de entidades reales del sistema, sin entidad propia.

Para esta entrega se mantuvo el criterio de excluir del resumen operativo:

- créditos anulados
- cobranzas anuladas

De esta forma, las métricas reflejan únicamente créditos vigentes y cobranzas válidas al momento de consultar el resumen.

### Validaciones realizadas

Se validaron tanto los flujos funcionales como los casos de negocio más importantes:

- actualización de permisos desde administrador
- ocultamiento de acciones para usuarios sin permiso
- anulación correcta de créditos sin cobranzas registradas
- rechazo de anulación de créditos con cobranzas
- anulación correcta de cobranzas del día
- rechazo de anulación de cobranzas fuera de fecha
- actualización del dashboard luego de operaciones de anulación

Además, se agregaron tests de integración para backend sobre los endpoints de anulación de crédito y cobranza.

---

## Seguridad JWT

El flujo de autenticación es:

```
1. POST /api/auth/register  →  { token, id, username, rol, puedeAnularCredito, puedeAnularCobranza }
2. POST /api/auth/login     →  { token, id, username, rol, puedeAnularCredito, puedeAnularCobranza }
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
