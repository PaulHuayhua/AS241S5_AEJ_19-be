# AS241S5_AEJ_19-be — Paul Huayhua

APIs de Inteligencia Artificial integradas con Spring WebFlux y MongoDB.
Permite consumir 2 servicios IA desde RapidAPI: un scraper de contenido web
y un servicio de clima inteligente, almacenando los resultados en MongoDB Atlas.

## 1. APIs IA

### AI Content Scraper
RapidAPI - AI Content Scraper. Extrae y procesa el contenido textual de
cualquier página web usando inteligencia artificial. Analiza la estructura
HTML de la URL proporcionada y devuelve el contenido limpio, estructurado
y resumido. Ideal para obtener información relevante de sitios web de forma
automática sin necesidad de procesar HTML manualmente.

### Weather Intelligence
RapidAPI - Weather Intelligence. Servicio meteorológico basado en IA que
obtiene datos climáticos en tiempo real a partir de coordenadas geográficas
(latitud y longitud). Retorna temperatura, condiciones del clima, humedad
y pronósticos utilizando modelos predictivos de inteligencia artificial.

## 2. Spring Boot

- **Java:** JDK 17
- **IDE:** Visual Studio Code | Codespace
- **Maven:** Apache Maven
- **Frameworks:** Spring Boot 3.5.11

## 3. Maven Dependencias

- spring-boot-starter-webflux
- spring-boot-starter-data-mongodb-reactive
- lombok
- reactor-test
- springdoc-openapi-starter-webflux-ui

### Dependencias Spring WebFlux + MongoDB (NoSQL)
Spring WebFlux | Data MongoDB Reactive | Project Reactor
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-test</artifactId>
    <scope>test</scope>
</dependency>
```

### Dependencias Swagger para Spring WebFlux
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.8.15</version>
</dependency>
```

### Dependencia Lombok
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```