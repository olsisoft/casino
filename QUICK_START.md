# 🚀 Quick Start - Casino Platform

## En 5 Minutes

### Prérequis
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Node.js 18+

### Démarrage Rapide

```bash
# 1. Cloner le projet
git clone <your-repo>
cd casino

# 2. Démarrer avec Docker
docker-compose up -d

# 3. Vérifier que tout fonctionne
# Eureka Dashboard
open http://localhost:8761

# 4. Tester l'API
curl http://localhost:8080/actuator/health

# 5. Démarrer le frontend
cd frontend/casino-mobile
npm install
npm start
# Dans un autre terminal
npm run android  # ou npm run ios
```

## Architecture en Bref

```
Mobile App (React Native)
    ↓
API Gateway (8080)
    ↓
├─ Auth Service (8081)    - Login/Register
├─ User Service (8082)    - Profils/Balance
└─ Game Service (8083)    - Slots/Jeux
    ↓
PostgreSQL + Redis
```

## Services & Ports

| Service | Port | URL | Description |
|---------|------|-----|-------------|
| API Gateway | 8080 | http://localhost:8080 | Point d'entrée |
| Auth Service | 8081 | http://localhost:8081 | Authentification |
| User Service | 8082 | http://localhost:8082 | Utilisateurs |
| Game Service | 8083 | http://localhost:8083 | Jeux |
| Eureka | 8761 | http://localhost:8761 | Service Discovery |
| PostgreSQL | 5432 | localhost:5432 | Base de données |
| Redis | 6379 | localhost:6379 | Cache |

## Endpoints API Principaux

### Auth
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "username": "player1",
    "password": "Password123!"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123!"
  }'
```

### User
```bash
# Get Profile (avec token)
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get Balance
curl http://localhost:8080/api/users/balance \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Games
```bash
# Liste des jeux
curl http://localhost:8080/api/games/list \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Spin slots
curl -X POST http://localhost:8080/api/games/slots/spin \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "session-id",
    "betAmount": 10
  }'
```

## Structure du Projet

```
casino/
├── frontend/casino-mobile/     # React Native App
├── services/                   # Microservices Java
│   ├── auth-service/
│   ├── user-service/
│   ├── game-service/
│   └── payment-service/
├── infrastructure/             # Gateway, Discovery
├── docs/                       # Documentation
└── docker-compose.yml
```

## Phase 1 - Statut Actuel

### ✅ Complété
- Architecture microservices
- Configuration tous les services
- Docker setup
- Documentation complète

### 🔄 À Implémenter
- Entités JPA
- Services métier
- Controllers
- Frontend screens
- Tests

## Prochaines Étapes

1. **Lire la doc**: `docs/MICROSERVICES_ARCHITECTURE.md`
2. **Voir le plan**: `docs/PHASE1_IMPLEMENTATION.md`
3. **Commencer**: Implémenter Auth Service

## Commandes Utiles

```bash
# Rebuild un service
docker-compose up -d --build auth-service

# Voir les logs
docker-compose logs -f auth-service

# Restart tout
docker-compose restart

# Stop tout
docker-compose down

# Reset databases
docker-compose down -v
docker-compose up -d
```

## Tests

```bash
# Backend tests
cd services/auth-service
mvn test

# Frontend tests
cd frontend/casino-mobile
npm test
```

## Aide

- 📚 Documentation: `./docs/`
- 🐛 Problèmes: Voir `docs/GETTING_STARTED.md#dépannage`
- 💡 Architecture: `docs/MICROSERVICES_ARCHITECTURE.md`

## Variables d'Environnement

### Backend
```bash
DB_PASSWORD=your_password
REDIS_PASSWORD=
JWT_SECRET=your-secret-key
```

### Frontend
```bash
API_GATEWAY_URL=http://localhost:8080/api
ENABLE_REAL_MONEY=false
```

---

**Ready to code!** 🎮
