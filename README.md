# 🏛️ DGI Immatriculation — Full Stack DevOps Project

> **Direction Générale des Impôts (DGI)** — A full-stack tax registration platform built with Angular, Spring Boot, and PostgreSQL, fully containerized and deployed with a complete DevOps pipeline.

---

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Prerequisites](#-prerequisites)
- [Quick Start — Docker Compose](#-quick-start--docker-compose)
- [Kubernetes Deployment](#-kubernetes-deployment)
- [CI/CD Pipeline — Jenkins](#-cicd-pipeline--jenkins)
- [Monitoring — Prometheus & Grafana](#-monitoring--prometheus--grafana)
- [Default Credentials](#-default-credentials)
- [Project Structure](#-project-structure)
- [API Documentation](#-api-documentation)

---

## 📌 Project Overview

DGI Immatriculation is a government tax registration system that allows:

- 👤 **Citizens** to register, submit tax dossiers, upload documents, and track status
- 🏢 **DGI Agents** to review dossiers, request information, and manage submissions
- 🔐 **Admins** to manage users, agents, and system configuration
- 🤖 **AI Assistant** (Groq LLM) for chatbot support and document OCR

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 17, Tailwind CSS, TypeScript |
| Backend | Spring Boot 3, Java 21, Spring Security (JWT) |
| Database | PostgreSQL 16, Flyway migrations |
| OCR | Tesseract (Arabic + French) |
| Face Verification | OpenCV |
| AI/LLM | Groq API (LLaMA 3.1 70B) |
| Containerization | Docker, Docker Compose |
| Orchestration | Kubernetes (Minikube) |
| CI/CD | Jenkins (Declarative Pipeline) |
| Registry | Docker Hub |
| Monitoring | Prometheus, Grafana |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    User Browser                          │
└─────────────────────┬───────────────────────────────────┘
                       │ :4200
┌─────────────────────▼───────────────────────────────────┐
│              Angular Frontend (Nginx)                    │
│                  Port 80 / 4200                          │
└─────────────────────┬───────────────────────────────────┘
                       │ /api/* proxy
┌─────────────────────▼───────────────────────────────────┐
│            Spring Boot Backend                           │
│                  Port 8081                               │
│   JWT Auth │ Flyway │ OCR │ Face Verify │ LLM Chat      │
└─────────────────────┬───────────────────────────────────┘
                       │
┌─────────────────────▼───────────────────────────────────┐
│              PostgreSQL 16                               │
│                  Port 5432                               │
│         DB: dgi_immatriculation                         │
└─────────────────────────────────────────────────────────┘

CI/CD Flow:
git push → GitHub Webhook → Jenkins →
  Build JAR → Build Docker Images →
  Push to Docker Hub → Deploy
```

---

## ✅ Prerequisites

Make sure you have the following installed:

| Tool | Version | Install |
|---|---|---|
| Docker | 24+ | [docs.docker.com](https://docs.docker.com/get-docker/) |
| Docker Compose | 2.27+ | included with Docker Desktop |
| kubectl | 1.29+ | [kubernetes.io](https://kubernetes.io/docs/tasks/tools/) |
| Minikube | 1.33+ | [minikube.sigs.k8s.io](https://minikube.sigs.k8s.io/docs/start/) |
| Git | 2.x | [git-scm.com](https://git-scm.com/) |
| Java | 21 | [adoptium.net](https://adoptium.net/) |
| Maven | 3.9+ | [maven.apache.org](https://maven.apache.org/) |
| Node.js | 20+ | [nodejs.org](https://nodejs.org/) |

---

## 🚀 Quick Start — Docker Compose

### 1. Clone the repository

```bash
git clone https://github.com/ghaithsoltani/Direction_G-n-rale_des_imp-ts.git
cd Direction_G-n-rale_des_imp-ts
```

### 2. Start all services

```bash
docker-compose up --build
```

This will:
- Build the Spring Boot backend (Maven multi-stage)
- Build the Angular frontend (Node + Nginx)
- Start PostgreSQL with automatic schema migration (Flyway)
- Start all 3 containers

### 3. Access the application

| Service | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8081 |
| Health Check | http://localhost:8081/actuator/health |

### 4. Create admin user

```bash
docker-compose exec postgres psql -U dgi_user -d dgi_immatriculation -c "
INSERT INTO utilisateurs (id, email, mot_de_passe_hash, role, actif, date_creation)
VALUES (
    gen_random_uuid(),
    'admin@dgi.tn',
    '\$2a\$10\$BEEYZbMp349RkEh2REvIwOMtwbFqQ5FN0vgBOegowWmFOHaFY2Gqe',
    'ADMIN',
    true,
    now()
);"
```

### 5. Stop all services

```bash
docker-compose down
```

To also remove the database volume:
```bash
docker-compose down -v
```

---

## ☸️ Kubernetes Deployment

### 1. Start Minikube

```bash
minikube start
minikube status
```

### 2. Deploy the full stack

```bash
# Create namespace
kubectl apply -f k8s/namespace.yml

# Deploy PostgreSQL
kubectl apply -f k8s/postgres/

# Deploy Backend
kubectl apply -f k8s/backend/

# Deploy Frontend
kubectl apply -f k8s/frontend/

# Watch pods start
kubectl get pods -n dgi -w
```

### 3. Wait for all pods to be Running

```
NAME                      READY   STATUS    RESTARTS   AGE
backend-xxx               1/1     Running   0          1m
frontend-xxx              1/1     Running   0          1m
postgres-xxx              1/1     Running   0          1m
```

### 4. Pre-pull images (speeds up first deploy)

```bash
minikube ssh "docker pull ghaithsoltani/dgi-backend:latest"
minikube ssh "docker pull ghaithsoltani/dgi-frontend:latest"
```

### 5. Access the application

```bash
# Terminal 1 — Frontend
kubectl port-forward service/frontend-service 4200:80 -n dgi

# Terminal 2 — Backend
kubectl port-forward service/backend-service 8081:8081 -n dgi
```

Open: **http://localhost:4200**

### 6. Create admin user in Kubernetes

```bash
kubectl exec -n dgi deployment/postgres -- psql -U dgi_user -d dgi_immatriculation -c "
INSERT INTO utilisateurs (id, email, mot_de_passe_hash, role, actif, date_creation)
VALUES (
    gen_random_uuid(),
    'admin@dgi.tn',
    '\$2a\$10\$BEEYZbMp349RkEh2REvIwOMtwbFqQ5FN0vgBOegowWmFOHaFY2Gqe',
    'ADMIN',
    true,
    now()
);"
```

### 7. Deploy monitoring (optional)

```bash
kubectl apply -f k8s/monitoring/namespace.yml
kubectl apply -f k8s/monitoring/prometheus-config.yml
kubectl apply -f k8s/monitoring/prometheus.yml
kubectl apply -f k8s/monitoring/grafana.yml

# Access
kubectl port-forward service/prometheus-service 9090:9090 -n monitoring
kubectl port-forward service/grafana-service 3000:3000 -n monitoring
```

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin / admin123)

### 8. Tear down

```bash
kubectl delete namespace dgi
kubectl delete namespace monitoring
```

---

## 🔄 CI/CD Pipeline — Jenkins

### Setup Jenkins

```bash
# Run Jenkins in Docker
docker volume create jenkins_home

docker run -d \
  --name jenkins \
  --restart unless-stopped \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts

# Get initial password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### Required Jenkins Credentials

| ID | Type | Value |
|---|---|---|
| `github-credentials` | Username + Password | GitHub username + token |
| `dockerhub-credentials` | Username + Password | Docker Hub username + token |

### Pipeline Stages

```
Checkout → Build Backend (Maven) → Build Frontend (npm) →
Build Docker Images → Push to Docker Hub → Deploy
```

Every push to `main` triggers the full pipeline automatically via GitHub webhook.

---

## 📊 Monitoring — Prometheus & Grafana

Once deployed, import the JVM dashboard in Grafana:

1. Go to http://localhost:3000
2. **Connections → Data Sources → Add → Prometheus**
3. URL: `http://prometheus-service.monitoring.svc.cluster.local:9090`
4. **Save & Test**
5. **Dashboards → Import → ID: `4701`** → Import

---

## 🔐 Default Credentials

| Service | Username | Password |
|---|---|---|
| App Admin | admin@dgi.tn | admin123 |
| PostgreSQL | dgi_user | changeme |
| Grafana | admin | admin123 |

> ⚠️ **Change all passwords before any production deployment.**

---

## 📁 Project Structure

```
MyExperinsDevV1/
├── dgi-backend-fixed/          # Spring Boot backend
│   ├── src/
│   │   └── main/
│   │       ├── java/tn/gov/dgi/
│   │       │   ├── config/         # Security, CORS, JWT
│   │       │   ├── controller/     # REST controllers
│   │       │   ├── service/        # Business logic
│   │       │   ├── repository/     # JPA repositories
│   │       │   └── model/          # JPA entities
│   │       └── resources/
│   │           ├── application.yml
│   │           └── db/migration/   # Flyway SQL scripts
│   ├── Dockerfile
│   └── pom.xml
│
├── dgi-frontend-fixed/         # Angular frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   ├── services/
│   │   │   └── environments/
│   │   └── assets/
│   ├── nginx.conf
│   ├── Dockerfile
│   └── angular.json
│
├── k8s/                        # Kubernetes manifests
│   ├── namespace.yml
│   ├── backend/
│   ├── frontend/
│   ├── postgres/
│   └── monitoring/             # Prometheus + Grafana
│
├── docker-compose.yml          # Local full-stack setup
├── Jenkinsfile                 # CI/CD pipeline
└── README.md
```

---

## 📖 API Documentation

The backend exposes a REST API at `http://localhost:8081/api/`.

Key endpoints:

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | /api/auth/login | Login | Public |
| POST | /api/auth/register | Register citizen | Public |
| POST | /api/auth/register-agent | Create agent | Admin |
| GET | /api/dossiers | List dossiers | Agent/Admin |
| POST | /api/dossiers | Create dossier | Citizen |
| POST | /api/dossiers/{id}/soumettre | Submit dossier | Citizen |
| POST | /api/dossiers/{id}/documents | Upload document | Citizen |
| PUT | /api/dossiers/{id}/statut | Update status | Agent/Admin |
| POST | /api/ocr/extract | Extract text from image | Citizen |
| POST | /api/face/verify | Verify face | Citizen |
| POST | /api/chatbot/message | Chat with AI | Authenticated |
| GET | /actuator/health | Health check | Public |

Full Postman collection available in: `dgi-backend-fixed/postman/DGI-Immatriculation.postman_collection.json`

---

## 👨‍💻 Author

**Ghaith Soltani**
- GitHub: [@ghaithsoltani](https://github.com/ghaithsoltani)
- Docker Hub: [ghaithsoltani](https://hub.docker.com/u/ghaithsoltani)

---

## 📄 License

This project was built as a final-year DevOps learning project.
