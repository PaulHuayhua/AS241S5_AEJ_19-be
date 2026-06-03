# GitHub Actions Pipelines

Este proyecto tiene **2 pipelines de CI/CD**:

## 1. Pipeline Docker Hub (`dockerhub-ci.yml`)

**Plataforma:** GitHub Actions + Docker Hub

### ¿Qué hace?
- Build de la imagen Docker del backend
- Push a Docker Hub con tags `latest` y el SHA del commit
- Se ejecuta en cada push/PR a `develop` o `main`
- También programado para ejecutarse diariamente a las 8:00 AM (Perú)

### Secrets requeridos:
- `DOCKERHUB_USERNAME_PAUL`: tu usuario de Docker Hub
- `DOCKERHUB_TOKEN_PAUL`: tu token de acceso de Docker Hub

### Flujo:
```
Push/PR → Checkout → Login Docker Hub → Build & Push
```

---

## 2. Pipeline Kubernetes (`pipeline.yml`) ✨ NUEVO

**Plataforma:** GitHub Actions + Kubernetes

### ¿Qué hace?
- **Job 1 (Build)**: 
  - Ejecuta tests de Maven
  - Build del proyecto Spring Boot
  - Build y push de imagen Docker con tags dinámicos
- **Job 2 (Deploy)**: 
  - Configura conexión a cluster Kubernetes
  - Aplica manifests (namespace, secrets, service, deployment)
  - Espera a que el rollout termine exitosamente
  - Solo se ejecuta en `push` a `develop` o `main` (no en PRs)
- **Job 3 (Notify)**:
  - Muestra resumen del deployment (éxito o fallo)

### Secrets requeridos:
- `DOCKERHUB_USERNAME_PAUL`: tu usuario de Docker Hub
- `DOCKERHUB_TOKEN_PAUL`: tu token de acceso de Docker Hub
- `KUBE_CONFIG`: tu kubeconfig en base64
- `GROQ_API_KEY`: API key de Groq
- `RAPIDAPI_KEY`: API key de RapidAPI
- `MONGODB_URI`: URI de conexión a MongoDB

### Flujo:
```
Push/PR a develop/main
        ↓
   Run Tests (Maven)
        ↓
   Build JAR + Docker
        ↓
   Push to Docker Hub
        ↓
[Solo en Push a develop/main]
        ↓
   Configure kubectl
        ↓
   Apply K8s manifests
        ↓
   Wait for rollout
        ↓
   Verify deployment ✅
```

### Cómo obtener KUBE_CONFIG en base64:
```bash
cat ~/.kube/config | base64 -w 0
```

### Tags de imagen generados:
- `develop` → `as241s5_aej_19-be:develop-abc1234`
- `main` → `as241s5_aej_19-be:main-abc1234`
- `PR #5` → `as241s5_aej_19-be:pr-5`

---

## Comparación

| Característica | dockerhub-ci.yml | pipeline.yml |
|----------------|------------------|--------------|
| **Plataforma destino** | Docker Hub | Kubernetes |
| **Tests** | ❌ No | ✅ Sí (Maven) |
| **Deploy automático** | ❌ No | ✅ Sí |
| **Ejecución programada** | ✅ Sí (diaria) | ❌ No |
| **Deploy en PRs** | N/A | ❌ No (solo build) |
| **Notificaciones** | ❌ No | ✅ Sí |

---

## Ejecución manual

Ambos pipelines soportan `workflow_dispatch`, lo que permite ejecutarlos manualmente desde:

**GitHub → Actions → [nombre del workflow] → Run workflow**
