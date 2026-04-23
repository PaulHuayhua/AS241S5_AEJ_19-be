# AS241S5_AEJ_19-be — Paul Huayhua

APIs de Inteligencia Artificial integradas con Spring WebFlux y MongoDB.
Permite consumir 2 servicios IA: Groq Chat (Llama 4 Scout) y Weather Intelligence,
almacenando los resultados estructurados en MongoDB Atlas.

## 1. APIs IA

### Groq Chat API
Groq - Chat Completions con Llama 4 Scout. Servicio de chat conversacional usando
el modelo Llama 4 Scout 17B ejecutado en la infraestructura LPU de Groq,
que ofrece inferencia ultra-rápida. Ideal para chatbots, asistentes virtuales,
generación de contenido y análisis de texto con respuestas en tiempo real.

Modelo: `meta-llama/llama-4-scout-17b-16e-instruct`

### Weather Intelligence
RapidAPI - Weather Intelligence. Servicio meteorológico basado en IA que
obtiene datos climáticos en tiempo real a partir de coordenadas geográficas
(latitud y longitud). Retorna temperatura, condiciones del clima, humedad
y pronósticos utilizando modelos predictivos de inteligencia artificial.

## 2. Tecnologías

- **Java:** JDK 17
- **IDE:** Visual Studio Code | Codespace
- **Maven:** Apache Maven
- **Framework:** Spring Boot 3.5.11
- **Base de Datos:** MongoDB Atlas (Reactive)
- **Programación Reactiva:** Project Reactor (WebFlux)

## 3. Arquitectura del Proyecto

```
src/main/java/ap1/paul/huayhua/
├── config/              # Configuraciones (WebClient, OpenAPI, MongoDB)
├── model/               # Entidades de MongoDB
│   └── dto/             # DTOs para requests/responses
├── repository/          # Repositorios reactivos
├── rest/                # Controladores REST
├── service/             # Interfaces de servicios
│   └── impl/            # Implementaciones de servicios
└── Application.java     # Clase principal
```

## 4. Dependencias Maven

- spring-boot-starter-webflux
- spring-boot-starter-data-mongodb-reactive
- lombok
- reactor-test
- springdoc-openapi-starter-webflux-ui

### Spring WebFlux + MongoDB Reactive
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
```

### Swagger para WebFlux
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.8.15</version>
</dependency>
```

### Lombok
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

## 5. Configuración

### Variables de Entorno
Copia `.env.example` y crea tu archivo `.env` con tus credenciales:

```bash
PORT=8080
SERVER_URL=http://localhost:8080
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/db
GROQ_API_KEY=tu_groq_api_key
GROQ_MODEL=meta-llama/llama-4-scout-17b-16e-instruct
RAPIDAPI_KEY=tu_rapidapi_key
RAPIDAPI_HOST_WEATHER=weather-intelligence1.p.rapidapi.com
```

### Obtener API Keys

#### Groq API Key
1. Visita [https://console.groq.com](https://console.groq.com)
2. Crea una cuenta o inicia sesión
3. Ve a "API Keys" y genera una nueva key
4. Copia la key y agrégala a tu `.env`

#### RapidAPI Key
1. Visita [https://rapidapi.com](https://rapidapi.com)
2. Suscríbete a "Weather Intelligence"
3. Copia tu API key desde el dashboard

## 6. Endpoints API

### Groq Chat (POST)
```bash
POST /api/chat
Content-Type: application/json

{
  "message": "¿Qué es la inteligencia artificial?"
}
```

**Respuesta:**
```json
{
  "content": "La inteligencia artificial (IA) se refiere al desarrollo...",
  "model": "meta-llama/llama-4-scout-17b-16e-instruct",
  "tokens_used": 145
}
```

### Groq Chat (GET)
```bash
GET /api/chat?message=Explica qué es la inteligencia artificial
```

### Weather
```bash
GET /api/weather?lat=-12.0464&lon=-77.0428
```

**Respuesta:**
```json
{
  "latitude": -12.0,
  "longitude": -77.04,
  "timezone": "America/Lima",
  "temperature_c": 22.1,
  "condition": "Overcast",
  "weather_code": 3,
  "wind_speed_kmh": 2.9,
  "relative_humidity_percent": 85,
  "is_day": true
}
```

## 7. Estructura de Datos en MongoDB

Los resultados se almacenan en la colección `api_results` con la siguiente estructura:

```json
{
  "_id": "ObjectId",
  "type": "groq-chat" | "weather",
  "query": {
    "message": "texto del mensaje"
  },
  "response": {
    "content": "...",
    "model": "...",
    "tokens_used": 145
  },
  "created_at": "2026-04-23T13:59:37.158Z",
  "updated_at": "2026-04-23T13:59:37.158Z"
}
```

Los campos `query` y `response` se guardan como objetos JSON estructurados (no como strings), facilitando consultas y análisis de datos.

## 8. Documentación Swagger

Una vez iniciada la aplicación, accede a:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`

## 9. Ejecutar el Proyecto

```bash
# Compilar
./mvnw clean install

# Ejecutar
./mvnw spring-boot:run
```

## 10. Ejemplos de Uso

### Ejemplo con cURL - Chat (POST)
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿Cuál es la capital de Perú?"}'
```

### Ejemplo con cURL - Chat (GET)
```bash
curl "http://localhost:8080/api/chat?message=¿Cuál%20es%20la%20capital%20de%20Perú?"
```

### Ejemplo con cURL - Weather
```bash
curl "http://localhost:8080/api/weather?lat=-12.0464&lon=-77.0428"
```

## 11. Características Técnicas

- **Programación Reactiva:** Uso de Mono y Flux para operaciones no bloqueantes
- **WebClient:** Cliente HTTP reactivo para consumir APIs externas
- **MongoDB Reactive:** Persistencia reactiva con Spring Data MongoDB
- **Type Safety:** Uso de ParameterizedTypeReference para evitar warnings de casting
- **Structured Logging:** Logs detallados con SLF4J y Lombok
- **Error Handling:** Manejo robusto de errores con onErrorResume
- **DTOs:** Objetos de transferencia de datos para requests y responses estructurados