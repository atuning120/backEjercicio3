# BackendArquitecturaSistemas_ONE - Sistema de Tickets

## 🚀 Instrucciones de Ejecución

Para ejecutar este backend de Spring Boot, utiliza el siguiente comando en la terminal desde el directorio del proyecto:

```bash
.\mvnw.cmd spring-boot:run
```

### Requisitos previos:
- Java 17 o superior
- PostgreSQL instalado y ejecutándose
- Archivo `.env` configurado con las credenciales de la base de datos

### Configuración:
1. Asegúrate de tener PostgreSQL ejecutándose en tu sistema
2. Configura las variables de entorno en el archivo `.env` (ubicado en la raíz del proyecto)
3. Ejecuta el comando de arriba
4. La aplicación estará disponible en `http://localhost:8080`

## 📚 API Endpoints

### Base URL: `http://localhost:8080`

## 🎫 Sistema de Tipos de Usuario

Este sistema maneja 3 tipos de usuarios:
1. **Cliente**: Puede ver eventos y comprar tickets
2. **Organizador**: Puede crear y gestionar sus eventos
3. **Propietario**: Puede gestionar sus locales y ver eventos programados

---

#### 🔐 Autenticación

| Método | Endpoint | Descripción | Respuesta |
|--------|----------|-------------|-----------|
| `POST` | `/auth/login` | Iniciar sesión | `200 OK` / `401 Unauthorized` |
| `POST` | `/auth/register` | Registrar nuevo usuario | `201 Created` / `409 Conflict` |

#### 👥 Usuarios

| Método | Endpoint | Descripción | Respuesta |
|--------|----------|-------------|-----------|
| `GET` | `/users` | Obtener todos los usuarios | `200 OK` |
| `GET` | `/users/{id}` | Obtener usuario por ID | `200 OK` / `404 Not Found` |
| `POST` | `/users` | Crear nuevo usuario | `201 Created` |
| `PUT` | `/users/{id}` | Actualizar usuario | `200 OK` / `404 Not Found` |
| `DELETE` | `/users/{id}` | Eliminar usuario | `204 No Content` / `404 Not Found` |

#### 🎉 Eventos

| Método | Endpoint | Descripción | Tipo de Usuario |
|--------|----------|-------------|-----------------|
| `GET` | `/events` | **CLIENTES**: Ver todos los eventos disponibles | Cliente |
| `GET` | `/events/{id}` | Ver detalles de un evento específico | Todos |
| `POST` | `/events` | **ORGANIZADORES**: Crear nuevo evento | Organizador |
| `PUT` | `/events/{id}` | **ORGANIZADORES**: Actualizar evento | Organizador |
| `DELETE` | `/events/{id}` | **ORGANIZADORES**: Eliminar evento | Organizador |
| `GET` | `/events/organizer/{organizerId}` | **ORGANIZADORES**: Ver mis eventos | Organizador |
| `GET` | `/events/spot/{spotId}` | **PROPIETARIOS**: Ver eventos en mi local | Propietario |

#### 🏢 Locales (Spots)

| Método | Endpoint | Descripción | Tipo de Usuario |
|--------|----------|-------------|-----------------|
| `GET` | `/spots` | Ver todos los locales | Todos |
| `GET` | `/spots/{id}` | Ver detalles de un local específico | Todos |
| `POST` | `/spots` | **PROPIETARIOS**: Crear nuevo local | Propietario |
| `PUT` | `/spots/{id}` | **PROPIETARIOS**: Actualizar local | Propietario |
| `DELETE` | `/spots/{id}` | **PROPIETARIOS**: Eliminar local | Propietario |
| `GET` | `/spots/owner/{ownerId}` | **PROPIETARIOS**: Ver mis locales | Propietario |

#### 🎫 Tickets

| Método | Endpoint | Descripción | Tipo de Usuario |
|--------|----------|-------------|-----------------|
| `POST` | `/tickets/purchase` | **CLIENTES**: Comprar tickets (incluye QR automático) | Cliente |
| `GET` | `/tickets/user/{userId}` | **CLIENTES**: Ver mis tickets con QR | Cliente |
| `GET` | `/tickets/event/{eventId}` | **ORGANIZADORES**: Ver tickets vendidos de mi evento | Organizador |
| `POST` | `/tickets/validate-qr` | **VALIDACIÓN**: Validar código QR de ticket | Staff/Organizador |

---

## 📋 Estructuras de Datos

### Registro de Usuario:
```json
{
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "password": "mi_contraseña",
  "role": "CLIENTE" // "ORGANIZADOR" o "PROPIETARIO"
}
```

### Crear Evento (Organizador):
```json
{
  "eventName": "Concierto de Rock",
  "organizerId": 1,
  "spotId": 1,
  "eventDate": "2024-12-25T20:00:00",
  "description": "Gran concierto de rock nacional",
  "category": "Música",
  "imageUrl": "https://example.com/imagen.jpg",
  "ticketPrice": 25000.0,
  "capacity": 500
}
```

### Crear Local (Propietario):
```json
{
  "name": "Teatro Municipal",
  "ownerId": 1,
  "location": "Av. Principal 123, Santiago"
}
```

### Comprar Ticket (Cliente):
```json
{
  "eventId": 1,
  "userId": 2,
  "quantity": 2
}
```

### Respuesta de Compra de Ticket (incluye QR):
```json
{
  "id": 1,
  "price": 25000.0,
  "eventId": 1,
  "userId": 2,
  "saleId": 1,
  "eventName": "Concierto de Rock",
  "eventDate": "2024-12-25T20:00:00",
  "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADI..."
}
```

### Validar QR Code:
```json
{
  "qrData": "TICKET_ID:1|EVENT:Concierto de Rock|USER:Juan Pérez|DATE:2024-12-25T20:00:00|VALIDATION_CODE:VAL11234"
}
```

---

## 🔄 Flujos de Trabajo por Tipo de Usuario

### 👤 Cliente:
1. **Ver eventos**: `GET /events`
2. **Ver detalles**: `GET /events/{id}`
3. **Comprar tickets**: `POST /tickets/purchase`
4. **Ver mis tickets**: `GET /tickets/user/{userId}`

### 🎭 Organizador:
1. **Crear evento**: `POST /events`
2. **Ver mis eventos**: `GET /events/organizer/{organizerId}`
3. **Actualizar evento**: `PUT /events/{id}`
4. **Ver tickets vendidos**: `GET /tickets/event/{eventId}`

### 🏢 Propietario:
1. **Crear local**: `POST /spots`
2. **Ver mis locales**: `GET /spots/owner/{ownerId}`
3. **Ver eventos en mis locales**: `GET /events/spot/{spotId}`
4. **Gestionar local**: `PUT /spots/{id}`

---

## 🔧 Ejemplos de Uso

### Registrar Organizador:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Productora Musical",
    "email": "productor@music.com",
    "password": "secret123",
    "role": "ORGANIZADOR"
  }'
```

### Crear Local (Propietario):
```bash
curl -X POST http://localhost:8080/spots \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Arena Santiago",
    "ownerId": 1,
    "location": "Las Condes, Santiago"
  }'
```

### Crear Evento (Organizador):
```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventName": "Festival de Verano",
    "organizerId": 2,
    "spotId": 1,
    "eventDate": "2024-12-31T21:00:00",
    "description": "Gran festival de fin de año",
    "category": "Festival",
    "ticketPrice": 35000.0,
    "capacity": 1000
  }'
```

### Comprar Tickets (Cliente):
```bash
curl -X POST http://localhost:8080/tickets/purchase \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 1,
    "userId": 3,
    "quantity": 2
  }'
```

### Ver mis eventos (Organizador):
```bash
curl http://localhost:8080/events/organizer/2
```

### Ver eventos en mi local (Propietario):
```bash
curl http://localhost:8080/events/spot/1
```

### Ver mis tickets (Cliente):
```bash
curl http://localhost:8080/tickets/user/3
```

### Validar QR Code (Staff/Organizador):
```bash
curl -X POST http://localhost:8080/tickets/validate-qr \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "qrData=TICKET_ID:1|EVENT:Concierto de Rock|USER:Juan Pérez|DATE:2024-12-25T20:00:00|VALIDATION_CODE:VAL11234"
```

---

## ⚠️ Notas Importantes

- Los precios de tickets se manejan en pesos chilenos (CLP)
- Las fechas deben estar en formato ISO 8601
- La capacidad de eventos es opcional, pero recomendada
- El sistema valida que no se vendan más tickets que la capacidad del evento
- Los roles de usuario son: "CLIENTE", "ORGANIZADOR", "PROPIETARIO"

## 🎫 **Nueva Funcionalidad: Códigos QR**

### ✨ **Características del Sistema QR:**
- **Generación automática**: Cada ticket comprado genera un QR único
- **Contenido del QR**: ID del ticket, evento, usuario, fecha y código de validación
- **Formato Base64**: El QR se devuelve como imagen en formato Base64
- **Validación**: Endpoint para escanear y validar QRs en eventos

### 📱 **Flujo de QR Codes:**
1. **Cliente compra ticket** → Se genera QR automáticamente
2. **Cliente recibe ticket con QR** → Puede mostrar/descargar/imprimir
3. **En el evento** → Staff escanea QR para validar entrada
4. **Validación exitosa** → Se muestran datos del ticket y evento

### 🔧 **Implementación Técnica:**
- **Librería ZXing**: Para generar códigos QR
- **Almacenamiento**: QR en Base64 guardado en base de datos
- **Validación**: Extracción de datos del QR para verificar autenticidad
- **Seguridad**: Código de validación único por ticket

- prueba testing
