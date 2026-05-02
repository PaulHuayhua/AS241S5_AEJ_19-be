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

## 3. Dependencias Maven

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

## 4. Endpoints API

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
