# Architecture Détaillée

## Vue d'ensemble

L'application casino est construite avec une architecture client-serveur moderne:
- **Mobile**: React Native (iOS/Android)
- **Backend**: NestJS (API REST + WebSocket)
- **Database**: PostgreSQL + Redis
- **Infrastructure**: Cloud-ready (AWS/GCP)

## Diagramme d'Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Mobile Apps                              │
│                  (React Native)                              │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Auth    │  │  Games   │  │Tournament│  │ Profile  │   │
│  │ Screens  │  │ Screens  │  │ Screens  │  │ Screens  │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
│       │             │              │             │          │
│       └─────────────┴──────────────┴─────────────┘          │
│                     │                                        │
│              ┌──────▼──────┐                                │
│              │ Redux Store │                                │
│              └──────┬──────┘                                │
│                     │                                        │
│       ┌─────────────┴─────────────┐                        │
│       │                           │                         │
│  ┌────▼─────┐              ┌─────▼──────┐                 │
│  │   API    │              │  WebSocket │                  │
│  │ Service  │              │   Client   │                  │
│  └────┬─────┘              └─────┬──────┘                 │
└───────┼────────────────────────────┼─────────────────────────┘
        │                            │
        │ HTTPS/REST                 │ WSS
        │                            │
┌───────▼────────────────────────────▼─────────────────────────┐
│                    Load Balancer                             │
└───────┬────────────────────────────┬─────────────────────────┘
        │                            │
┌───────▼────────────────┐    ┌──────▼────────────────┐
│   API Gateway          │    │  WebSocket Gateway    │
│   (NestJS)             │    │  (Socket.io)          │
└───────┬────────────────┘    └──────┬────────────────┘
        │                            │
        └──────────┬───────────────────┘
                   │
        ┌──────────▼──────────┐
        │   NestJS Backend    │
        │                     │
        │  ┌───────────────┐ │
        │  │ Auth Module   │ │
        │  ├───────────────┤ │
        │  │ Users Module  │ │
        │  ├───────────────┤ │
        │  │ Games Module  │ │
        │  ├───────────────┤ │
        │  │Payment Module │ │
        │  ├───────────────┤ │
        │  │Tourney Module │ │
        │  └───────────────┘ │
        └──────────┬──────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
   ┌────▼─────┐        ┌─────▼─────┐
   │PostgreSQL│        │   Redis   │
   │  (Main)  │        │  (Cache)  │
   └──────────┘        └───────────┘
        │
   ┌────▼─────┐
   │  Stripe  │
   │   API    │
   └──────────┘
```

## Couches de l'Application

### 1. Couche Présentation (Mobile App)

**Responsabilités:**
- Interface utilisateur
- Gestion de l'état local
- Navigation
- Interactions utilisateur

**Technologies:**
- React Native + TypeScript
- Redux Toolkit (state management)
- React Navigation
- React Native Reanimated (animations)
- Socket.io client (temps réel)

**Patterns:**
- Container/Presentational components
- Custom hooks pour logique réutilisable
- Redux slices modulaires
- Service layer pour API calls

### 2. Couche API (Backend)

**Responsabilités:**
- Logique métier
- Authentification/Autorisation
- Validation des données
- Orchestration des services

**Technologies:**
- NestJS + TypeScript
- Passport (auth)
- Class-validator (validation)
- TypeORM (ORM)

**Patterns:**
- Module-based architecture
- Dependency Injection
- Guards pour autorisation
- Interceptors pour transformation
- Filters pour exception handling

### 3. Couche Données

**PostgreSQL (Base principale):**
- Données utilisateurs
- Transactions
- Historique des jeux
- Tournois
- Achievements

**Redis (Cache & Sessions):**
- Sessions utilisateurs
- Cache des données fréquentes
- Rate limiting
- Pub/Sub pour WebSocket
- Queue jobs (Bull)

## Flux de Données

### Authentification

```
Mobile App                Backend                Database
    │                        │                      │
    ├─ POST /auth/login ────>│                      │
    │                        ├─ Validate ──────────>│
    │                        │<─ User data ─────────┤
    │                        ├─ Generate JWT        │
    │                        ├─ Store session ─────>│ Redis
    │<─ JWT tokens ──────────┤                      │
    ├─ Store in AsyncStorage │                      │
    │                        │                      │
```

### Jeu en Temps Réel

```
Mobile                WebSocket               Game Service         Database
  │                      │                         │                  │
  ├─ Connect ───────────>│                         │                  │
  │                      ├─ Authenticate ─────────>│                  │
  │<─ Connected ─────────┤                         │                  │
  │                      │                         │                  │
  ├─ Place bet ─────────>│                         │                  │
  │                      ├─ Validate bet ─────────>│                  │
  │                      │                         ├─ Deduct balance >│
  │                      │                         ├─ Generate result │
  │                      │                         ├─ Calculate win   │
  │                      │                         ├─ Update balance >│
  │<─ Game result ───────┤<─ Broadcast ────────────┤                  │
  │                      │                         │                  │
```

### Paiement

```
Mobile              Backend              Stripe API         Database
  │                    │                     │                 │
  ├─ Deposit ────────>│                     │                 │
  │                    ├─ Create payment ──>│                 │
  │<─ Client secret ───┤<─ Payment intent ──┤                 │
  │                    │                     │                 │
  ├─ Confirm payment ─>│                     │                 │
  │                    ├─ Confirm ──────────>│                 │
  │                    │<─ Success ──────────┤                 │
  │                    ├─ Update balance ───────────────────>│
  │<─ Success ─────────┤                     │                 │
  │                    │                     │                 │
```

## Sécurité

### Authentification et Autorisation

```typescript
// JWT Strategy
@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  async validate(payload: JwtPayload) {
    // Vérifier utilisateur
    // Vérifier statut
    // Retourner user
  }
}

// Guards
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.PLAYER, UserRole.VIP)
async placeBet(@CurrentUser() user: User, @Body() bet: PlaceBetDto) {
  // Logique de pari
}
```

### Validation

```typescript
// DTO avec class-validator
export class PlaceBetDto {
  @IsUUID()
  gameId: string;

  @IsNumber()
  @Min(1)
  @Max(10000)
  amount: number;

  @IsOptional()
  @IsObject()
  betDetails?: Record<string, any>;
}
```

### Rate Limiting

```typescript
// Throttler configuration
@ThrottlerGuard({
  ttl: 60,
  limit: 100, // 100 requêtes par minute
})
```

## Scalabilité

### Horizontal Scaling

**Backend:**
- Stateless API servers
- Session dans Redis (partagée)
- Load balancer (NGINX/AWS ALB)

**WebSocket:**
- Socket.io avec Redis adapter
- Sticky sessions si nécessaire
- Multiple instances

**Database:**
- Read replicas PostgreSQL
- Connection pooling
- Query optimization

### Caching Strategy

**Niveaux de cache:**

1. **Client (Mobile):**
   - AsyncStorage pour données persistantes
   - Redux pour état en mémoire
   - Image caching

2. **Backend:**
   - Redis pour données fréquentes
   - TTL adaptatif
   - Cache invalidation

3. **Database:**
   - Materialized views
   - Indexes optimisés

### Performance

**Mobile:**
- Code splitting
- Lazy loading screens
- Image optimization
- Bundle size monitoring

**Backend:**
- Database indexing
- Query optimization
- Background jobs (Bull)
- Compression

## Monitoring et Logging

### Logging

```typescript
// Winston logger
private logger = new Logger(GameService.name);

async placeBet(userId: string, bet: PlaceBetDto) {
  this.logger.log(`User ${userId} placing bet`, { bet });
  try {
    // Logic
  } catch (error) {
    this.logger.error(`Bet failed for ${userId}`, error.stack);
    throw error;
  }
}
```

### Métriques

- Request/Response times
- Error rates
- Database query performance
- WebSocket connections
- Active users
- Game sessions

### Tools

- Sentry (error tracking)
- LogRocket (mobile analytics)
- Prometheus + Grafana (metrics)
- CloudWatch/DataDog (infrastructure)

## Déploiement

### Environnements

1. **Development**: Local machines
2. **Staging**: Cloud environment mirroring production
3. **Production**: Scaled cloud deployment

### CI/CD Pipeline

```yaml
# Exemple GitHub Actions
name: Deploy

on:
  push:
    branches: [main]

jobs:
  test:
    - Run tests
    - Run linter
    - Type checking

  build:
    - Build mobile (iOS/Android)
    - Build backend
    - Docker images

  deploy:
    - Deploy to staging
    - Run E2E tests
    - Deploy to production (manual approval)
```

### Infrastructure as Code

- Terraform pour infrastructure
- Docker pour containerization
- Kubernetes pour orchestration (optionnel)

## Base de Données

### Schéma Principal

**Tables:**
- users
- user_settings
- sessions
- transactions
- game_sessions
- game_results
- tournaments
- tournament_players
- achievements
- user_achievements
- payment_methods
- notifications

### Relations

```sql
users 1──N transactions
users 1──N game_sessions
users 1──N user_achievements
users N──M tournaments (via tournament_players)
```

### Indexes

- users(email) - unique
- users(username) - unique
- transactions(userId, createdAt)
- game_sessions(userId, status)
- tournament_players(tournamentId, userId)

## APIs Externes

### Stripe

- Deposits
- Withdrawals
- Payment methods
- Webhooks

### Potentiels Futurs

- Twilio (SMS/2FA)
- SendGrid (Email)
- Analytics (Mixpanel, Amplitude)
- KYC providers (Onfido, Jumio)

## Conformité et Régulations

### RGPD

- Droit à l'oubli
- Export des données
- Consentement explicite
- Privacy by design

### Jeu Responsable

- Limites de dépôt
- Auto-exclusion
- Temps de session
- Alertes

### AML/KYC

- Vérification identité
- Monitoring transactions
- Rapports suspects
- Limites sans KYC
