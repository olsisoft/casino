# Dépendances du Projet

Ce document liste toutes les dépendances du projet casino, leur rôle et leurs alternatives.

## Mobile App (React Native)

### Core Dependencies

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| react | ^18.2.0 | Library UI | - |
| react-native | 0.73.2 | Framework mobile | Flutter, Ionic |
| typescript | ^5.3.3 | Langage typé | Flow |

### Navigation

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| @react-navigation/native | ^6.1.9 | Navigation core | React Native Navigation |
| @react-navigation/stack | ^6.3.20 | Stack navigation | - |
| @react-navigation/bottom-tabs | ^6.5.11 | Tab navigation | - |
| react-native-screens | ^3.29.0 | Native screens | - |
| react-native-safe-area-context | ^4.8.2 | Safe area handling | - |
| react-native-gesture-handler | ^2.14.1 | Gesture system | - |

### State Management

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| @reduxjs/toolkit | ^2.0.1 | Redux moderne | Zustand, MobX, Recoil |
| react-redux | ^9.0.4 | React bindings | - |
| redux-persist | ^6.0.0 | Persist state | AsyncStorage direct |

### UI & Animations

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| react-native-paper | ^5.11.6 | Material Design | NativeBase, UI Kitten |
| react-native-reanimated | ^3.6.1 | Animations performantes | Animated API |
| react-native-skia | ^1.0.0 | Graphics 2D/3D | Canvas API |
| react-native-svg | ^14.1.0 | SVG support | - |
| react-native-linear-gradient | ^2.8.3 | Gradients | - |
| react-native-vector-icons | ^10.0.3 | Icônes | react-native-svg |

### Storage & Data

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| @react-native-async-storage/async-storage | ^1.21.0 | Local storage | SQLite, Realm |

### Network & API

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| axios | ^1.6.5 | HTTP client | fetch, ky |
| socket.io-client | ^4.6.1 | WebSocket | native WebSocket |

### Payments

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| @stripe/stripe-react-native | ^0.35.0 | Stripe integration | PayPal SDK, Braintree |

### Utilities

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| date-fns | ^3.0.6 | Date manipulation | moment, dayjs |
| zod | ^3.22.4 | Schema validation | yup, joi |
| jwt-decode | ^4.0.0 | JWT decoding | - |

### Development

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| @testing-library/react-native | ^12.4.3 | Testing library | Enzyme |
| jest | ^29.7.0 | Test runner | Vitest |
| eslint | ^8.56.0 | Linter | Biome |
| prettier | ^3.1.1 | Code formatter | - |

## Backend (NestJS)

### Core Dependencies

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| @nestjs/common | ^10.3.0 | NestJS core | Express, Fastify |
| @nestjs/core | ^10.3.0 | NestJS core | - |
| @nestjs/platform-express | ^10.3.0 | Express adapter | @nestjs/platform-fastify |
| typescript | ^5.3.3 | Langage typé | - |

### Database

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| @nestjs/typeorm | ^10.0.1 | TypeORM integration | Prisma, Sequelize |
| typeorm | ^0.3.19 | ORM | Prisma, Sequelize |
| pg | ^8.11.3 | PostgreSQL driver | - |

### Cache & Queue

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| redis | ^4.6.12 | Redis client | - |
| ioredis | ^5.3.2 | Redis client (advanced) | redis |
| @nestjs/bull | ^10.0.1 | Queue integration | BullMQ direct |
| bull | ^4.12.0 | Queue system | BullMQ, Bee-Queue |

### Authentication

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| @nestjs/passport | ^10.0.3 | Passport integration | - |
| @nestjs/jwt | ^10.2.0 | JWT utilities | jsonwebtoken |
| passport | ^0.7.0 | Auth middleware | - |
| passport-jwt | ^4.0.1 | JWT strategy | - |
| passport-local | ^1.0.0 | Local strategy | - |
| bcrypt | ^5.1.1 | Password hashing | argon2, scrypt |

### WebSocket

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| @nestjs/websockets | ^10.3.0 | WebSocket integration | - |
| @nestjs/platform-socket.io | ^10.3.0 | Socket.io adapter | ws |
| socket.io | ^4.6.1 | WebSocket library | ws |

### Validation & Configuration

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| class-validator | ^0.14.1 | DTO validation | zod, yup |
| class-transformer | ^0.5.1 | Object transformation | - |
| @nestjs/config | ^3.1.1 | Config management | dotenv |

### Payments

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| stripe | ^14.12.0 | Stripe SDK | PayPal SDK, Square |

### Security

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| @nestjs/throttler | ^5.1.1 | Rate limiting | express-rate-limit |
| helmet | ^7.1.0 | Security headers | - |

### Utilities

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| uuid | ^9.0.1 | UUID generation | nanoid, cuid |
| date-fns | ^3.0.6 | Date utilities | moment, dayjs |
| zod | ^3.22.4 | Schema validation | yup, joi |
| winston | ^3.11.0 | Logging | pino, bunyan |
| compression | ^1.7.4 | Response compression | - |
| rxjs | ^7.8.1 | Reactive programming | - |

### Development

| Package | Version | Description | Alternatives |
|---------|---------|-------------|--------------|
| @nestjs/testing | ^10.3.0 | Testing utilities | - |
| jest | ^29.7.0 | Test runner | Vitest |
| ts-jest | ^29.1.1 | Jest TS support | - |
| eslint | ^8.56.0 | Linter | Biome |
| prettier | ^3.1.1 | Formatter | - |

## Infrastructure Dependencies

### Services Requis

| Service | Type | Description | Alternatives |
|---------|------|-------------|--------------|
| PostgreSQL | Database | Base de données principale | MySQL, MongoDB |
| Redis | Cache/Queue | Cache et queues | Memcached, RabbitMQ |
| Stripe | Payment | Processeur de paiement | PayPal, Square, Adyen |

### Services Optionnels

| Service | Type | Description | Alternatives |
|---------|------|-------------|--------------|
| AWS S3 | Storage | Stockage fichiers | Google Cloud Storage, Azure Blob |
| CloudFront | CDN | Content delivery | CloudFlare, Fastly |
| Sentry | Monitoring | Error tracking | LogRocket, Rollbar |
| SendGrid | Email | Service email | Mailgun, AWS SES |
| Twilio | SMS | 2FA et notifications | Vonage, AWS SNS |

## Pourquoi ces Choix?

### React Native vs Flutter
**Choix: React Native**
- Écosystème JavaScript/TypeScript
- Large communauté
- Nombreuses libraries
- Performance suffisante pour casino
- Hot reload excellent

### Redux Toolkit vs Zustand
**Choix: Redux Toolkit**
- State management robuste
- DevTools puissants
- Middleware pour async
- Standard de l'industrie
- Redux Persist pour persistance

**Alternative possible: Zustand**
- Plus simple
- Moins de boilerplate
- Suffisant pour petits projets

### NestJS vs Express
**Choix: NestJS**
- Architecture structurée
- TypeScript natif
- Dependency injection
- Modules organisés
- Excellent pour scaling

### TypeORM vs Prisma
**Choix: TypeORM**
- Mature et stable
- Decorators élégants
- Support excellent NestJS
- Migrations robustes

**Alternative: Prisma**
- Type-safety supérieur
- Générateur de client
- Migrations simplifiées
- Performance légèrement meilleure

### PostgreSQL vs MongoDB
**Choix: PostgreSQL**
- Transactions ACID (crucial pour casino)
- Relations complexes
- Intégrité des données
- Performant pour analytics
- JSON support si nécessaire

### Socket.io vs WebSocket natif
**Choix: Socket.io**
- Auto-reconnection
- Rooms/namespaces
- Fallbacks multiples
- Événements nommés
- Broadcast facile

### Stripe vs PayPal
**Choix: Stripe**
- API moderne
- Documentation excellente
- Fees compétitifs
- Support international
- Webhooks fiables
- Customisation complète

## Installation Optimisée

### Ordre d'Installation Recommandé

1. **Base:**
   ```bash
   # Installer Node.js 18+
   # Installer PostgreSQL
   # Installer Redis
   ```

2. **Backend:**
   ```bash
   cd backend
   npm install
   # Créer .env
   npm run migration:run
   ```

3. **Mobile:**
   ```bash
   cd mobile-app
   npm install
   cd ios && pod install && cd .. # iOS uniquement
   # Créer .env
   ```

### Problèmes Courants

**Node Modules trop gros:**
- Utiliser npm ci au lieu de npm install
- Nettoyer cache: npm cache clean --force
- Utiliser pnpm pour économiser espace

**Conflits de versions:**
- Fixer versions dans package.json (sans ^)
- Utiliser package-lock.json
- Tester avec versions exactes

**Performance:**
- Profiler avec React DevTools
- Lazy load modules lourds
- Code splitting
- Bundle analyzer

## Mise à Jour des Dépendances

### Stratégie

1. **Patch versions** (x.x.X): Sûr, auto-update OK
2. **Minor versions** (x.X.x): Tester en dev
3. **Major versions** (X.x.x): Review changelog, tests complets

### Commandes Utiles

```bash
# Vérifier versions obsolètes
npm outdated

# Mise à jour patch seulement
npm update

# Mise à jour interactive
npx npm-check-updates -i

# Audit sécurité
npm audit
npm audit fix
```

### Fréquence Recommandée

- **Hebdomadaire**: Audit sécurité
- **Mensuel**: Patch updates
- **Trimestriel**: Minor updates
- **Annuel**: Major updates (planifié)

## Licences

Toutes les dépendances utilisent des licences compatibles:
- MIT (majorité)
- Apache 2.0
- BSD-3-Clause

Aucune licence GPL (incompatible avec app propriétaire).

## Budget Estimé

### Services Mensuels (Production)

- **Stripe**: 2.9% + 0.30€ par transaction
- **AWS/Cloud**: ~200-500€/mois (dépend du trafic)
- **Sentry**: ~26€/mois (plan Team)
- **SendGrid**: ~15€/mois (plan Essentials)
- **Total estimé**: 250-600€/mois + fees transactions

### One-time Costs

- Apple Developer: 99$/an
- Google Play: 25$ (one-time)
- Domaine: ~15€/an
- SSL: Gratuit (Let's Encrypt)
