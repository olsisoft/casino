# Guide de Démarrage

Ce guide vous aidera à configurer l'environnement de développement et à lancer l'application casino.

## Table des Matières

1. [Prérequis](#prérequis)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [Lancement](#lancement)
5. [Développement](#développement)
6. [Dépannage](#dépannage)

## Prérequis

### Système

- **Node.js**: Version 18 ou supérieure
- **npm** ou **yarn**: Gestionnaire de packages
- **Git**: Pour cloner le repository

### Base de Données

- **PostgreSQL**: Version 14 ou supérieure
- **Redis**: Version 7 ou supérieure

### Mobile Development

#### Pour iOS:
- macOS
- Xcode 14+
- CocoaPods (`sudo gem install cocoapods`)
- iOS Simulator ou device physique

#### Pour Android:
- Android Studio
- Android SDK (API 33+)
- Java JDK 17
- Android Emulator ou device physique

### Services Externes

- **Stripe Account**: Pour les paiements
  - Créer un compte sur https://stripe.com
  - Obtenir les clés API (test mode pour développement)

## Installation

### 1. Cloner le Repository

```bash
git clone <repository-url>
cd casino
```

### 2. Installer PostgreSQL

**macOS (Homebrew):**
```bash
brew install postgresql@14
brew services start postgresql@14
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

**Windows:**
- Télécharger depuis https://www.postgresql.org/download/windows/
- Installer et configurer

### 3. Installer Redis

**macOS (Homebrew):**
```bash
brew install redis
brew services start redis
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install redis-server
sudo systemctl start redis-server
```

**Windows:**
- Télécharger depuis https://redis.io/download
- Ou utiliser Docker: `docker run -d -p 6379:6379 redis`

### 4. Créer la Base de Données

```bash
# Se connecter à PostgreSQL
psql postgres

# Créer un utilisateur
CREATE USER casino_user WITH PASSWORD 'your_password';

# Créer la base de données
CREATE DATABASE casino_db;

# Donner les privilèges
GRANT ALL PRIVILEGES ON DATABASE casino_db TO casino_user;

# Quitter
\q
```

### 5. Installer les Dépendances

**Backend:**
```bash
cd backend
npm install
```

**Mobile App:**
```bash
cd mobile-app
npm install

# Pour iOS uniquement
cd ios
pod install
cd ..
```

## Configuration

### Backend Configuration

1. **Créer le fichier .env:**

```bash
cd backend
cp .env.example .env  # Si un exemple existe
# Sinon, créer .env manuellement
```

2. **Éditer .env:**

```env
# Server
NODE_ENV=development
PORT=3000
API_PREFIX=api/v1

# Database
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=casino_user
DB_PASSWORD=your_password
DB_DATABASE=casino_db
DB_SYNC=true  # ATTENTION: false en production!

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DB=0

# JWT
JWT_SECRET=change_this_to_a_random_secret_key_123456789
JWT_REFRESH_SECRET=change_this_to_another_random_secret_key_987654321
JWT_EXPIRATION=15m
JWT_REFRESH_EXPIRATION=7d

# Stripe (Test mode)
STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
STRIPE_PUBLISHABLE_KEY=pk_test_your_stripe_publishable_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

# WebSocket
WS_PORT=3001
WS_CORS_ORIGIN=http://localhost:*

# Security
BCRYPT_ROUNDS=10
RATE_LIMIT_TTL=60
RATE_LIMIT_MAX=100

# Logging
LOG_LEVEL=debug
```

### Mobile App Configuration

1. **Créer le fichier .env:**

```bash
cd mobile-app
touch .env
```

2. **Éditer .env:**

```env
# API Configuration
API_URL=http://localhost:3000/api/v1
WS_URL=http://localhost:3001

# Pour Android Emulator, utiliser 10.0.2.2 au lieu de localhost:
# API_URL=http://10.0.2.2:3000/api/v1
# WS_URL=http://10.0.2.2:3001

# Stripe
STRIPE_PUBLISHABLE_KEY=pk_test_your_stripe_publishable_key

# App Config
APP_NAME=Casino
ENABLE_LOGGING=true
```

3. **Configuration React Native:**

Le fichier `babel.config.js` devrait inclure:

```javascript
module.exports = {
  presets: ['module:metro-react-native-babel-preset'],
  plugins: [
    ['module:react-native-dotenv', {
      moduleName: '@env',
      path: '.env',
    }],
    'react-native-reanimated/plugin',
  ],
};
```

## Lancement

### Démarrer le Backend

```bash
cd backend

# Mode développement (avec hot reload)
npm run start:dev

# Le serveur démarre sur http://localhost:3000
# WebSocket sur http://localhost:3001
```

Vérifier que le serveur fonctionne:
```bash
curl http://localhost:3000/api/v1/health
```

### Démarrer l'Application Mobile

**Terminal 1 - Metro Bundler:**
```bash
cd mobile-app
npm start
```

**Terminal 2 - iOS:**
```bash
cd mobile-app
npm run ios

# Ou pour un device spécifique:
npm run ios -- --simulator="iPhone 15 Pro"
```

**Terminal 2 - Android:**
```bash
cd mobile-app

# Démarrer l'émulateur d'abord via Android Studio
# Ou lister les devices:
adb devices

# Lancer l'app:
npm run android
```

## Développement

### Structure des Tâches Courantes

#### Créer un Nouveau Module Backend

```bash
cd backend
nest generate module modules/my-module
nest generate service modules/my-module
nest generate controller modules/my-module
```

#### Créer un Nouvel Écran Mobile

```bash
cd mobile-app/src/screens
mkdir MyScreen
touch MyScreen/index.tsx
touch MyScreen/styles.ts
```

#### Ajouter une Migration de Base de Données

```bash
cd backend
npm run migration:generate -- -n CreateMyTable
npm run migration:run
```

### Commandes Utiles

**Backend:**
```bash
# Linter
npm run lint

# Tests
npm run test
npm run test:watch
npm run test:cov

# Build
npm run build

# Type checking
tsc --noEmit
```

**Mobile:**
```bash
# Linter
npm run lint

# Type checking
npm run type-check

# Tests
npm run test

# Clear cache (en cas de problème)
npm start -- --reset-cache

# Clear build iOS
cd ios && rm -rf build && cd ..

# Clear build Android
cd android && ./gradlew clean && cd ..
```

### Outils de Développement

**Backend:**
- API Documentation: http://localhost:3000/api/docs (Swagger)
- Database GUI: pgAdmin, DBeaver, ou TablePlus
- Redis GUI: RedisInsight

**Mobile:**
- React Native Debugger
- Flipper
- Reactotron

## Dépannage

### Backend ne démarre pas

**Erreur: Cannot connect to database**
```bash
# Vérifier que PostgreSQL est en cours d'exécution
# macOS
brew services list

# Linux
sudo systemctl status postgresql

# Vérifier les credentials dans .env
# Tester la connexion:
psql -h localhost -U casino_user -d casino_db
```

**Erreur: Cannot connect to Redis**
```bash
# Vérifier que Redis fonctionne
redis-cli ping
# Devrait retourner "PONG"

# Si non, démarrer Redis:
# macOS
brew services start redis

# Linux
sudo systemctl start redis-server
```

**Erreur de port déjà utilisé**
```bash
# Trouver le processus utilisant le port 3000
# macOS/Linux
lsof -ti:3000 | xargs kill -9

# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

### Mobile App ne démarre pas

**Erreur: Unable to resolve module**
```bash
# Clear cache et réinstaller
cd mobile-app
rm -rf node_modules
npm install
npm start -- --reset-cache
```

**iOS - Erreur de pods**
```bash
cd mobile-app/ios
pod deintegrate
pod install
cd ..
```

**Android - Build failed**
```bash
# Clear Gradle cache
cd mobile-app/android
./gradlew clean
./gradlew cleanBuildCache
cd ..

# Rebuild
npm run android
```

**Erreur: Network request failed**
- Vérifier que le backend est lancé
- Android Emulator: utiliser `10.0.2.2` au lieu de `localhost`
- iOS Simulator: `localhost` devrait fonctionner
- Device physique: utiliser l'IP locale (ex: `192.168.1.x`)

### Erreurs Stripe

**Invalid API Key**
- Vérifier les clés dans `.env`
- S'assurer d'utiliser les clés de test (`sk_test_...` et `pk_test_...`)
- Vérifier que les variables sont bien chargées

**Webhook errors**
```bash
# Pour tester les webhooks localement, installer Stripe CLI:
stripe listen --forward-to localhost:3000/api/v1/payments/webhook

# Copier le webhook secret dans .env
```

### Performance

**Metro Bundler lent**
```bash
# Augmenter la limite de fichiers surveillés (macOS/Linux)
echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf
sudo sysctl -p
```

**Backend lent**
- Vérifier les indexes de base de données
- Activer query logging pour identifier les requêtes lentes
- Vérifier la connection pool PostgreSQL

### Logs Utiles

**Backend logs:**
```bash
# Voir tous les logs
tail -f backend/logs/app.log

# Logs par niveau
LOG_LEVEL=debug npm run start:dev
```

**Mobile logs:**
```bash
# iOS
npx react-native log-ios

# Android
npx react-native log-android
```

## Prochaines Étapes

1. **Explorer le code:**
   - Backend: `backend/src/modules`
   - Mobile: `mobile-app/src`
   - Types: `shared/types`

2. **Lire la documentation:**
   - [Architecture](./ARCHITECTURE.md)
   - [API Documentation](./API.md)
   - [Game Logic](./GAME_LOGIC.md)

3. **Implémenter votre premier feature:**
   - Choisir un module
   - Créer les types
   - Implémenter backend
   - Créer l'UI mobile
   - Tester

## Ressources

- [React Native Docs](https://reactnative.dev/docs/getting-started)
- [NestJS Docs](https://docs.nestjs.com)
- [TypeORM Docs](https://typeorm.io)
- [Redux Toolkit](https://redux-toolkit.js.org)
- [Stripe Docs](https://stripe.com/docs)

## Support

- Ouvrir une issue sur GitHub
- Consulter les logs
- Vérifier la documentation
