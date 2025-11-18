# 🎯 Commandes Essentielles - Casino Platform

## Docker

### Démarrage
```bash
# Tout démarrer
docker-compose up -d

# Démarrer services spécifiques
docker-compose up -d postgres redis

# Rebuild et redémarrer
docker-compose up -d --build auth-service
```

### Monitoring
```bash
# Voir les logs
docker-compose logs -f auth-service
docker-compose logs -f api-gateway

# Statut des services
docker-compose ps

# Utilisation resources
docker stats
```

### Maintenance
```bash
# Restart un service
docker-compose restart auth-service

# Stop tout
docker-compose down

# Stop et supprimer volumes (ATTENTION: supprime les données)
docker-compose down -v

# Cleanup complet
docker system prune -a
```

## Maven (Backend)

### Build & Run
```bash
# Build un service
cd services/auth-service
mvn clean install

# Run en dev
mvn spring-boot:run

# Run avec profil
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Build sans tests
mvn clean install -DskipTests

# Package JAR
mvn clean package
```

### Tests
```bash
# Tous les tests
mvn test

# Tests spécifiques
mvn test -Dtest=AuthServiceTest

# Tests avec coverage
mvn clean test jacoco:report

# Integration tests
mvn verify
```

### Utilities
```bash
# Voir dépendances
mvn dependency:tree

# Mettre à jour dépendances
mvn versions:display-dependency-updates

# Clean
mvn clean
```

## Database (PostgreSQL)

### Connection
```bash
# Se connecter
psql -h localhost -U casino_user -d auth_db

# Avec Docker
docker exec -it casino-postgres psql -U casino_user -d auth_db
```

### Commandes SQL
```sql
-- Lister les bases
\l

-- Lister les tables
\dt

-- Décrire une table
\d users

-- Voir toutes les données
SELECT * FROM users;

-- Compter
SELECT COUNT(*) FROM users;

-- Quitter
\q
```

### Backup & Restore
```bash
# Backup
docker exec casino-postgres pg_dump -U casino_user auth_db > backup.sql

# Restore
docker exec -i casino-postgres psql -U casino_user -d auth_db < backup.sql

# Reset database
docker exec casino-postgres psql -U casino_user -d auth_db -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

## Redis

### Connection
```bash
# CLI
redis-cli

# Avec Docker
docker exec -it casino-redis redis-cli
```

### Commandes Redis
```bash
# Ping
PING

# Voir toutes les clés
KEYS *

# Voir une valeur
GET user:123

# Supprimer une clé
DEL user:123

# Flush tout (ATTENTION!)
FLUSHALL

# Info
INFO

# Monitor en temps réel
MONITOR
```

## Frontend (React Native)

### Installation
```bash
cd frontend/casino-mobile

# Installer dépendances
npm install

# iOS: installer pods
cd ios && pod install && cd ..

# Clean install
rm -rf node_modules package-lock.json
npm install
```

### Development
```bash
# Démarrer Metro
npm start

# Clean cache Metro
npm start -- --reset-cache

# Android
npm run android

# iOS
npm run ios

# iOS device spécifique
npm run ios -- --simulator="iPhone 15 Pro"

# Android release
npm run android -- --variant=release
```

### Debug
```bash
# Android logs
npm run android && npx react-native log-android

# iOS logs
npm run ios && npx react-native log-ios

# Ouvrir DevMenu
# iOS: Cmd+D
# Android: Cmd+M or Shake device

# React Native Debugger
open "rndebugger://set-debugger-loc?host=localhost&port=8081"
```

### Build
```bash
# Android APK
cd android
./gradlew assembleRelease
# APK in: android/app/build/outputs/apk/release/

# iOS Archive (nécessite Mac + Xcode)
cd ios
xcodebuild -workspace CasinoMobile.xcworkspace -scheme CasinoMobile -configuration Release archive
```

### Tests
```bash
# Run tests
npm test

# Watch mode
npm test -- --watch

# Coverage
npm test -- --coverage

# E2E (si Detox configuré)
npm run e2e
```

## Git

### Workflow Standard
```bash
# Nouvelle feature
git checkout -b feature/auth-service
git add .
git commit -m "feat: implement auth service"
git push origin feature/auth-service

# Update depuis main
git checkout main
git pull
git checkout feature/auth-service
git rebase main

# Merge PR
git checkout main
git pull
git merge feature/auth-service
git push
```

### Utilities
```bash
# Status
git status

# Voir branches
git branch -a

# Supprimer branch locale
git branch -d feature/old-feature

# Stash changes
git stash
git stash pop

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Voir l'historique
git log --oneline --graph --all
```

## Service Discovery (Eureka)

### URLs
```bash
# Dashboard
http://localhost:8761

# API
curl http://localhost:8761/eureka/apps

# Health check
curl http://localhost:8761/actuator/health
```

## API Gateway

### Test Routes
```bash
# Health
curl http://localhost:8080/actuator/health

# Auth routes
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","username":"test","password":"Test123!"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test123!"}'

# User routes (avec JWT)
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Games routes
curl http://localhost:8080/api/games/list \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Monitoring

### Health Checks
```bash
# Tous les services
curl http://localhost:8761/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health

# Script pour tous
for port in 8080 8081 8082 8083 8761; do
  echo "Checking port $port..."
  curl -s http://localhost:$port/actuator/health | jq .
done
```

### Metrics
```bash
# Prometheus format
curl http://localhost:8080/actuator/prometheus

# Metrics JSON
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### Logs
```bash
# Docker logs
docker-compose logs -f auth-service

# Tail logs (si fichier)
tail -f services/auth-service/logs/app.log

# All services
docker-compose logs -f
```

## Troubleshooting

### Port déjà utilisé
```bash
# Linux/Mac - Trouver process
lsof -ti:8080
lsof -ti:8761

# Kill process
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Clean tout
```bash
# Docker
docker-compose down -v
docker system prune -a

# Backend
cd services/auth-service
mvn clean
rm -rf target/

# Frontend
cd frontend/casino-mobile
rm -rf node_modules
rm -rf ios/Pods
rm -rf android/.gradle
rm -rf android/app/build
npm install
```

### Reset Base de Données
```bash
# Drop et recréer
docker exec casino-postgres psql -U casino_user -c "DROP DATABASE auth_db;"
docker exec casino-postgres psql -U casino_user -c "CREATE DATABASE auth_db;"

# Ou restart container
docker-compose restart postgres
```

## Development Workflow

### Matin
```bash
# 1. Pull derniers changes
git pull

# 2. Démarrer infrastructure
docker-compose up -d postgres redis service-discovery

# 3. Démarrer services
cd services/auth-service && mvn spring-boot:run &
cd services/user-service && mvn spring-boot:run &
cd services/game-service && mvn spring-boot:run &
cd infrastructure/api-gateway && mvn spring-boot:run &

# 4. Démarrer frontend
cd frontend/casino-mobile
npm start &
npm run android
```

### Avant commit
```bash
# 1. Tests
mvn test
npm test

# 2. Format code
mvn fmt:format
npm run lint --fix

# 3. Build
mvn clean install
npm run build

# 4. Commit
git add .
git commit -m "feat: your message"
git push
```

### Fin de journée
```bash
# Stop tout
docker-compose down
pkill -f spring-boot
pkill -f node

# Ou garder DB pour demain
docker-compose stop auth-service user-service game-service api-gateway
# Garder postgres, redis, eureka running
```

## Production Deployment

### Build pour Production
```bash
# Backend: Build JARs
cd services/auth-service
mvn clean package -Pprod

# Frontend: Build release
cd frontend/casino-mobile
npm run build:android
npm run build:ios

# Docker: Build images
docker-compose build
docker-compose push
```

### Deploy
```bash
# Docker Swarm
docker stack deploy -c docker-compose.yml casino

# Kubernetes
kubectl apply -f k8s/

# Manual
java -jar auth-service.jar --spring.profiles.active=prod
```

## Aliases Utiles

Ajouter dans `.bashrc` ou `.zshrc`:

```bash
# Casino project shortcuts
alias casino-start="cd ~/casino && docker-compose up -d"
alias casino-stop="docker-compose down"
alias casino-logs="docker-compose logs -f"
alias casino-reset="docker-compose down -v && docker-compose up -d"

# Service shortcuts
alias auth-run="cd services/auth-service && mvn spring-boot:run"
alias user-run="cd services/user-service && mvn spring-boot:run"
alias game-run="cd services/game-service && mvn spring-boot:run"

# Frontend
alias mobile-start="cd frontend/casino-mobile && npm start"
alias mobile-android="cd frontend/casino-mobile && npm run android"
alias mobile-ios="cd frontend/casino-mobile && npm run ios"
```

## Variables d'Environnement

### Export pour dev
```bash
export DB_PASSWORD=your_password
export REDIS_PASSWORD=
export JWT_SECRET=your-super-secret-key
export API_GATEWAY_URL=http://localhost:8080/api
```

### Ou fichier .env
```bash
# Backend
cp .env.example .env
# Éditer .env

# Frontend
cd frontend/casino-mobile
cp .env.example .env
# Éditer .env
```

---

**💡 Tip**: Bookmark this file pour référence rapide!
