# Casino Platform - Architecture Microservices

## 🏗️ Architecture

Application de casino mobile avec architecture microservices :
- **Frontend**: React Native (iOS/Android)
- **Backend**: Java Spring Boot (Microservices)
- **Infrastructure**: API Gateway, Service Discovery, PostgreSQL, Redis

```
Frontend (React Native)
         ↓
   API Gateway (8080)
         ↓
    ├─ Auth Service (8081)
    ├─ User Service (8082)
    ├─ Game Service (8083)
    └─ Payment Service (8084)
         ↓
   Service Discovery (8761)
         ↓
   PostgreSQL + Redis
```

## 📁 Structure du Projet

```
casino/
├── frontend/
│   └── casino-mobile/          # Application React Native
│
├── services/                   # Microservices Backend
│   ├── auth-service/           # Authentification (8081)
│   ├── user-service/           # Gestion utilisateurs (8082)
│   ├── game-service/           # Logique des jeux (8083)
│   ├── payment-service/        # Paiements (8084)
│   ├── tournament-service/     # Tournois (8085)
│   └── notification-service/   # Notifications (8086)
│
├── infrastructure/             # Services d'infrastructure
│   ├── api-gateway/            # Point d'entrée unique (8080)
│   ├── service-discovery/      # Eureka (8761)
│   └── config-server/          # Configuration centralisée (8888)
│
├── shared/                     # Code partagé
│   └── types/                  # Types TypeScript
│
├── docs/                       # Documentation
├── scripts/                    # Scripts utilitaires
└── docker-compose.yml         # Docker orchestration
```

## 🚀 Phase 1 - MVP

### Features Implémentées

✅ **Auth Service**
- Inscription/Connexion
- JWT authentication
- Gestion des sessions

✅ **User Service**
- Profils utilisateurs
- Balance virtuelle
- Settings basiques

✅ **Game Service**
- Slots (machine à sous)
- Session de jeu
- Historique

✅ **Infrastructure**
- API Gateway
- Service Discovery
- Bases de données séparées

### À Venir (Phase 2+)
- ⏸️ Payment Service (Stripe)
- ⏸️ Tournament Service
- ⏸️ Notification Service
- ⏸️ Achievements & Progression

## 🔧 Prérequis

### Développement Local

**Java:**
- JDK 17 ou supérieur
- Maven 3.8+

**Frontend:**
- Node.js 18+
- React Native CLI
- Android Studio (Android) ou Xcode (iOS)

**Base de Données:**
- PostgreSQL 14+
- Redis 7+

**Optionnel:**
- Docker & Docker Compose

## 📦 Installation

### Option 1: Docker (Recommandé)

```bash
# 1. Cloner le repository
git clone <repository-url>
cd casino

# 2. Créer les fichiers .env
cp frontend/casino-mobile/.env.example frontend/casino-mobile/.env

# 3. Lancer tous les services avec Docker
docker-compose up -d

# 4. Vérifier que tous les services sont up
docker-compose ps

# 5. Accéder à Eureka Dashboard
http://localhost:8761
```

### Option 2: Installation Manuelle

#### Backend Services

**1. PostgreSQL:**
```bash
# Créer les bases de données
createdb auth_db
createdb user_db
createdb game_db
createdb payment_db
```

**2. Redis:**
```bash
# Démarrer Redis
redis-server
```

**3. Service Discovery (Eureka):**
```bash
cd infrastructure/service-discovery
mvn clean install
mvn spring-boot:run
# Accessible sur http://localhost:8761
```

**4. Auth Service:**
```bash
cd services/auth-service
mvn clean install
mvn spring-boot:run
# Running on http://localhost:8081
```

**5. User Service:**
```bash
cd services/user-service
mvn clean install
mvn spring-boot:run
# Running on http://localhost:8082
```

**6. Game Service:**
```bash
cd services/game-service
mvn clean install
mvn spring-boot:run
# Running on http://localhost:8083
```

**7. API Gateway:**
```bash
cd infrastructure/api-gateway
mvn clean install
mvn spring-boot:run
# Running on http://localhost:8080
```

#### Frontend (React Native)

```bash
cd frontend/casino-mobile

# Installer les dépendances
npm install

# iOS uniquement
cd ios && pod install && cd ..

# Créer .env
cp .env.example .env
# Éditer .env avec vos configurations

# Démarrer Metro
npm start

# Dans un autre terminal
# Android
npm run android

# iOS
npm run ios
```

## 🧪 Tests

### Backend

```bash
# Tester un service spécifique
cd services/auth-service
mvn test

# Tester tous les services
./scripts/test-all-services.sh
```

### Frontend

```bash
cd frontend/casino-mobile
npm test
```

### Tests E2E

```bash
# À venir
```

## 📝 Configuration

### Variables d'Environnement Backend

Chaque service utilise les variables suivantes:

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=<service>_db
DB_USERNAME=casino_user
DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Eureka
EUREKA_URL=http://localhost:8761/eureka/

# JWT (Auth Service & API Gateway)
JWT_SECRET=your-super-secret-key-min-256-bits
JWT_EXPIRATION=900000
```

### Variables d'Environnement Frontend

```bash
# API Gateway
API_GATEWAY_URL=http://localhost:8080/api

# Features
ENABLE_REAL_MONEY=false  # Phase 1: Virtual money only
```

## 🔌 Endpoints API

### Auth Service (via Gateway: /api/auth)

```
POST   /api/auth/register       - Inscription
POST   /api/auth/login          - Connexion
POST   /api/auth/refresh        - Refresh token
POST   /api/auth/logout         - Déconnexion
```

### User Service (via Gateway: /api/users)

```
GET    /api/users/profile       - Obtenir profil
PUT    /api/users/profile       - Mettre à jour profil
GET    /api/users/balance       - Obtenir balance
GET    /api/users/settings      - Obtenir settings
PUT    /api/users/settings      - Mettre à jour settings
```

### Game Service (via Gateway: /api/games)

```
GET    /api/games/list          - Liste des jeux
GET    /api/games/{id}          - Détails d'un jeu
POST   /api/games/slots/spin    - Spin slots
GET    /api/games/session/{id}  - Session de jeu
GET    /api/games/history       - Historique
```

## 📊 Monitoring

### Eureka Dashboard
```
http://localhost:8761
```

### Health Checks

```bash
# Service Discovery
curl http://localhost:8761/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health

# Auth Service
curl http://localhost:8081/actuator/health

# User Service
curl http://localhost:8082/actuator/health

# Game Service
curl http://localhost:8083/actuator/health
```

### Metrics

```bash
# Prometheus format
curl http://localhost:8080/actuator/prometheus
```

## 🐛 Dépannage

### Services ne démarrent pas

**Vérifier PostgreSQL:**
```bash
psql -h localhost -U casino_user -d auth_db
```

**Vérifier Redis:**
```bash
redis-cli ping
# Devrait retourner PONG
```

**Vérifier ports disponibles:**
```bash
# Linux/Mac
lsof -i :8080
lsof -i :8761

# Windows
netstat -ano | findstr :8080
```

### Eureka Dashboard vide

Attendre 30-60 secondes pour que les services s'enregistrent.

### Frontend ne peut pas se connecter

**Android Emulator:**
```bash
# Utiliser 10.0.2.2 au lieu de localhost
API_GATEWAY_URL=http://10.0.2.2:8080/api
```

**Device physique:**
```bash
# Utiliser IP locale
ipconfig  # Windows
ifconfig  # Linux/Mac

# Puis
API_GATEWAY_URL=http://192.168.1.x:8080/api
```

### Docker: Services unhealthy

```bash
# Voir les logs
docker-compose logs auth-service

# Restart un service
docker-compose restart auth-service

# Rebuild si changements
docker-compose up -d --build auth-service
```

## 🚢 Déploiement

### Development
```bash
docker-compose up -d
```

### Production

**À venir:**
- Kubernetes manifests
- CI/CD pipeline
- Environment configs

## 📚 Documentation

- [Architecture Microservices](./docs/MICROSERVICES_ARCHITECTURE.md)
- [Getting Started](./docs/GETTING_STARTED.md)
- [API Documentation](./docs/API.md)
- [Dependencies](./docs/DEPENDENCIES.md)

## 🗺️ Roadmap

### ✅ Phase 1 - MVP (Actuel)
- [x] Architecture microservices
- [x] Auth Service
- [x] User Service
- [x] Game Service (Slots)
- [x] API Gateway
- [x] Service Discovery
- [x] Frontend basique

### 🔄 Phase 2 - Core Features (2-3 mois)
- [ ] Payment Service (mode virtuel avancé)
- [ ] 2-3 jeux additionnels (Roulette, Blackjack)
- [ ] Tournament Service basique
- [ ] Notification Service
- [ ] Achievements système
- [ ] Chat basique

### 📅 Phase 3 - Production Ready (3-4 mois)
- [ ] Payment Service (Stripe production)
- [ ] KYC/AML compliance
- [ ] Tous les jeux
- [ ] Analytics avancés
- [ ] Performance optimizations
- [ ] Security hardening

## 🤝 Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit (`git commit -m 'Add some AmazingFeature'`)
4. Push (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📄 Licence

Propriétaire - Tous droits réservés

## 🆘 Support

- Documentation: `./docs`
- Issues: GitHub Issues
- Email: support@casino-platform.com

---

**Développé avec:**
- Java 17 + Spring Boot 3.2
- React Native
- PostgreSQL + Redis
- Docker

**Architecture:**
- Microservices
- API Gateway Pattern
- Service Discovery
- Database per Service
