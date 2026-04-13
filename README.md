# 💪 FitDiary Pro

Diario di allenamento avanzato — Full Stack PWA (React + Spring Boot + PostgreSQL)

---

## 🚀 Avvio Rapido con Docker

### Prerequisiti
- Docker Desktop ≥ 24
- Docker Compose ≥ 2.20

### Avvia tutto con un comando

```bash
docker compose up --build
```

| Servizio | URL |
|---|---|
| Frontend (PWA) | http://localhost:3000 |
| Backend API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/api/swagger-ui.html |
| PostgreSQL | localhost:5432 |

---

## 🛠 Sviluppo Locale

### Backend (Java 21 + Spring Boot)

```bash
cd backend
# Avvia solo Postgres
docker compose up postgres -d

# Esegui Spring Boot
./mvnw spring-boot:run
# oppure
mvn spring-boot:run
```

### Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

---

## 🏗 Architettura

```
fitdiary/
├── docker-compose.yml
├── backend/                   # Spring Boot 3.2 + Java 21
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/fitdiary/
│       ├── entity/            # JPA entities (User, WorkoutPlan, Session…)
│       ├── repository/        # Spring Data JPA repos
│       ├── service/           # Business logic
│       │   ├── AuthService    # JWT auth + refresh token
│       │   ├── WorkoutService # Plans, sessions, exercise logging
│       │   └── StatsService   # Volume, progressi, PR, insights
│       ├── controller/        # REST endpoints
│       ├── dto/               # Request/Response DTOs
│       ├── security/          # JWT filter, UserDetailsService
│       └── config/            # Security, OpenAPI, Exception handler
└── frontend/                  # React 18 + Vite
    ├── Dockerfile
    ├── nginx.conf
    └── src/
        ├── App.jsx            # Router
        ├── components/        # AppShell (bottom nav)
        ├── pages/
        │   ├── LoginPage      # Auth
        │   ├── RegisterPage
        │   ├── HomePage       # Dashboard + insights
        │   ├── PlansPage      # CRUD schede + template
        │   ├── LogPage        # Session logger live
        │   ├── ProgressPage   # Grafici Chart.js + PR
        │   └── ProfilePage    # Profilo + statistiche
        ├── services/api.js    # Axios client + auto-refresh
        └── store/stores.js    # Zustand (auth, active session)
```

---

## 📡 API Reference

### Auth
| Metodo | Endpoint | Descrizione |
|---|---|---|
| POST | `/api/auth/register` | Registrazione |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/refresh` | Rinnova token |
| POST | `/api/auth/logout` | Logout |

### Utente
| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/users/me` | Profilo corrente |
| PUT | `/api/users/me` | Aggiorna profilo |

### Schede
| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/plans` | Lista schede |
| POST | `/api/plans` | Crea scheda |
| GET | `/api/plans/{id}` | Dettaglio |
| DELETE | `/api/plans/{id}` | Elimina |
| POST | `/api/plans/{id}/activate` | Imposta attiva |

### Sessioni
| Metodo | Endpoint | Descrizione |
|---|---|---|
| POST | `/api/workouts/start` | Avvia sessione |
| POST | `/api/workouts/finish` | Termina e salva |
| POST | `/api/workouts/sets` | Aggiungi serie live |
| GET | `/api/workouts` | Lista sessioni |
| GET | `/api/workouts/{id}` | Dettaglio sessione |

### Esercizi
| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/exercises` | Libreria (+ ?search=) |
| POST | `/api/exercises` | Crea custom |

### Statistiche
| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/stats` | Statistiche generali |
| GET | `/api/stats/volume` | Volume settimanale |
| GET | `/api/stats/progress/exercise/{id}` | Progressione esercizio |
| GET | `/api/stats/prs` | Personal records |
| GET | `/api/stats/insights` | Insight automatici |
| GET | `/api/stats/suggest/{exerciseId}` | Suggerimento carico |

---

## 🗄 Schema DB

Entità principali:

```
users ──< workout_plans ──< workout_days ──< planned_exercises >── exercises
users ──< workout_sessions ──< exercise_logs ──< set_logs
users ──< personal_records >── exercises
users ──< refresh_tokens
```

---

## 🧠 Logiche Fitness Implementate

| Feature | Implementazione |
|---|---|
| **Progressive Overload** | `getSuggestedLoad`: RIR ≤ 1 → +2.5kg |
| **1RM Stimato** | Formula Epley: `w × (1 + r/30)` |
| **PR Automatici** | Aggiornati ad ogni sessione se 1RM migliora |
| **Volume Trend** | Confronto settimane recenti vs precedenti |
| **Deload Suggestion** | RIR medio < 1 nelle ultime 3 sessioni |
| **Streak** | Conteggio giorni consecutivi di allenamento |
| **Autofill** | Frontend pre-compila dai dati sessione precedente |
| **Timer Recupero** | Avvio automatico dopo ogni serie completata |

---

## 🔐 Sicurezza

- JWT (access token 24h) + Refresh Token (7gg)
- BCrypt per le password
- CORS configurabile
- Stateless (no session server-side)

---

## ⚙️ Variabili d'Ambiente

| Variabile | Default | Descrizione |
|---|---|---|
| `SPRING_DATASOURCE_URL` | jdbc:postgresql://postgres:5432/fitdiary | URL DB |
| `SPRING_DATASOURCE_USERNAME` | fitdiary | Username DB |
| `SPRING_DATASOURCE_PASSWORD` | fitdiary_secret | Password DB |
| `JWT_SECRET` | *(vedere docker-compose)* | Chiave JWT — **cambia in produzione!** |
| `JWT_EXPIRATION` | 86400000 | Scadenza access token (ms) |
| `JWT_REFRESH_EXPIRATION` | 604800000 | Scadenza refresh token (ms) |

---

## 📦 Tech Stack

**Backend:** Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA, PostgreSQL, JWT (jjwt), Swagger/OpenAPI, Lombok

**Frontend:** React 18, Vite, React Router 6, Zustand, Axios, Chart.js, date-fns, react-hot-toast

**Infra:** Docker, Docker Compose, Nginx (reverse proxy + SPA serving)
