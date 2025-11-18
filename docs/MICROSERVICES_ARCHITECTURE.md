# Architecture Microservices - Casino Platform

## Vue d'ensemble

Architecture microservices avec backend Java Spring Boot et frontend React Native.

## Architecture Globale

```
┌─────────────────────────────────────────────────────────────────┐
│                     Mobile App (React Native)                   │
│                         Port: 8081                              │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTPS/REST + WebSocket
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                      API Gateway (Spring Cloud)                 │
│                         Port: 8080                              │
│  - Routing                                                       │
│  - Load Balancing                                               │
│  - Rate Limiting                                                │
│  - JWT Validation                                               │
└────────┬────────────────────────────────────────────────────────┘
         │
         ├─────────────────────┬──────────────────┬────────────────┐
         │                     │                  │                │
┌────────▼────────┐   ┌───────▼────────┐  ┌─────▼──────┐  ┌─────▼─────┐
│  Auth Service   │   │  User Service  │  │Game Service│  │Payment Svc│
│   Port: 8081    │   │   Port: 8082   │  │ Port: 8083 │  │Port: 8084 │
│                 │   │                │  │            │  │           │
│ - Login         │   │ - Profiles     │  │ - Slots    │  │ - Stripe  │
│ - Register      │   │ - Settings     │  │ - Roulette │  │ - Deposit │
│ - JWT           │   │ - KYC          │  │ - Blackjack│  │ - Withdraw│
│ - 2FA           │   │ - Balance      │  │ - Poker    │  │ - History │
└────────┬────────┘   └───────┬────────┘  └─────┬──────┘  └─────┬─────┘
         │                    │                  │               │
         └────────────────────┴──────────────────┴───────────────┘
                              │
         ┌────────────────────┴──────────────────────────────────┐
         │                                                        │
┌────────▼────────┐                                     ┌────────▼────────┐
│Tournament Svc   │                                     │Notification Svc │
│  Port: 8085     │                                     │   Port: 8086    │
│                 │                                     │                 │
│ - Tournaments   │                                     │ - WebSocket     │
│ - Leaderboards  │                                     │ - Push Notif    │
│ - Events        │                                     │ - Email         │
└────────┬────────┘                                     └────────┬────────┘
         │                                                        │
         └────────────────────┬───────────────────────────────────┘
                              │
         ┌────────────────────┴──────────────────────────────────┐
         │                                                        │
┌────────▼────────┐                                     ┌────────▼────────┐
│Service Discovery│                                     │  Config Server  │
│    (Eureka)     │                                     │ (Spring Config) │
│  Port: 8761     │                                     │   Port: 8888    │
└─────────────────┘                                     └─────────────────┘
         │
         └────────────────────┬───────────────────────────────────┐
                              │                                   │
                     ┌────────▼────────┐                 ┌────────▼────────┐
                     │   PostgreSQL    │                 │     Redis       │
                     │   Port: 5432    │                 │   Port: 6379    │
                     │                 │                 │                 │
                     │ - Users DB      │                 │ - Cache         │
                     │ - Games DB      │                 │ - Sessions      │
                     │ - Payments DB   │                 │ - Pub/Sub       │
                     └─────────────────┘                 └─────────────────┘
```

## Microservices

### 1. Auth Service (Port 8081)

**Responsabilités:**
- Authentification (login, register)
- Génération et validation JWT
- 2FA (two-factor authentication)
- OAuth2 integration
- Password reset
- Token refresh

**Technologies:**
- Spring Boot 3.2
- Spring Security
- JWT (jjwt)
- BCrypt

**Database:** PostgreSQL (auth_db)

**Endpoints:**
```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/logout
POST   /api/auth/verify-email
POST   /api/auth/reset-password
POST   /api/auth/2fa/enable
POST   /api/auth/2fa/verify
```

### 2. User Service (Port 8082)

**Responsabilités:**
- Gestion profils utilisateurs
- Settings et préférences
- KYC (Know Your Customer)
- Gestion balance
- Historique utilisateur
- Achievements et progression

**Technologies:**
- Spring Boot 3.2
- Spring Data JPA
- Redis (cache)

**Database:** PostgreSQL (user_db)

**Endpoints:**
```
GET    /api/users/profile
PUT    /api/users/profile
GET    /api/users/settings
PUT    /api/users/settings
GET    /api/users/balance
POST   /api/users/kyc/submit
GET    /api/users/kyc/status
GET    /api/users/achievements
GET    /api/users/stats
```

### 3. Game Service (Port 8083)

**Responsabilités:**
- Logique des jeux (slots, roulette, blackjack, poker)
- Gestion des sessions de jeu
- RNG (Random Number Generator)
- Calcul des gains
- Historique des jeux
- Game analytics

**Technologies:**
- Spring Boot 3.2
- Spring WebSocket
- Redis (game state)
- SecureRandom (RNG)

**Database:** PostgreSQL (game_db)

**Endpoints:**
```
GET    /api/games/list
GET    /api/games/{id}
POST   /api/games/slots/spin
POST   /api/games/roulette/bet
POST   /api/games/blackjack/action
POST   /api/games/poker/join
GET    /api/games/session/{id}
GET    /api/games/history
GET    /api/games/stats
```

### 4. Payment Service (Port 8084)

**Responsabilités:**
- Intégration Stripe
- Dépôts et retraits
- Gestion transactions
- Historique paiements
- Fraud detection
- Compliance

**Technologies:**
- Spring Boot 3.2
- Stripe Java SDK
- Spring Kafka (events)

**Database:** PostgreSQL (payment_db)

**Endpoints:**
```
POST   /api/payments/deposit
POST   /api/payments/withdraw
GET    /api/payments/transactions
GET    /api/payments/methods
POST   /api/payments/methods
DELETE /api/payments/methods/{id}
POST   /api/payments/webhook/stripe
GET    /api/payments/balance
```

### 5. Tournament Service (Port 8085)

**Responsabilités:**
- Création et gestion tournois
- Inscriptions
- Leaderboards temps réel
- Calcul des prix
- Events et notifications

**Technologies:**
- Spring Boot 3.2
- Spring WebSocket
- Redis (leaderboards)
- Spring Scheduler

**Database:** PostgreSQL (tournament_db)

**Endpoints:**
```
GET    /api/tournaments/list
GET    /api/tournaments/{id}
POST   /api/tournaments/{id}/register
DELETE /api/tournaments/{id}/unregister
GET    /api/tournaments/{id}/leaderboard
GET    /api/tournaments/my-tournaments
WS     /ws/tournaments/{id}
```

### 6. Notification Service (Port 8086)

**Responsabilités:**
- WebSocket pour temps réel
- Push notifications
- Email notifications
- SMS (2FA)
- In-app notifications
- Event broadcasting

**Technologies:**
- Spring Boot 3.2
- Spring WebSocket
- Firebase Cloud Messaging
- SendGrid (email)
- Twilio (SMS)

**Database:** PostgreSQL (notification_db)

**Endpoints:**
```
WS     /ws/notifications
GET    /api/notifications/list
PUT    /api/notifications/{id}/read
DELETE /api/notifications/{id}
POST   /api/notifications/preferences
```

## Infrastructure

### API Gateway (Port 8080)

**Rôle:**
- Point d'entrée unique
- Routing vers microservices
- Load balancing
- Rate limiting
- JWT validation
- CORS handling
- Request/Response logging

**Technologies:**
- Spring Cloud Gateway
- Spring Security
- Redis (rate limiting)

**Configuration:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://AUTH-SERVICE
          predicates:
            - Path=/api/auth/**
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/api/users/**
        - id: game-service
          uri: lb://GAME-SERVICE
          predicates:
            - Path=/api/games/**
```

### Service Discovery (Port 8761)

**Rôle:**
- Enregistrement services
- Service discovery
- Health checks
- Load balancing

**Technologies:**
- Netflix Eureka Server

### Config Server (Port 8888)

**Rôle:**
- Configuration centralisée
- Gestion secrets
- Environment profiles
- Refresh dynamique

**Technologies:**
- Spring Cloud Config
- Git backend (optionnel)

## Base de Données

### Strategy: Database per Service

Chaque microservice a sa propre base de données pour assurer l'isolation.

**Bases PostgreSQL:**
```
auth_db         -> Auth Service
user_db         -> User Service
game_db         -> Game Service
payment_db      -> Payment Service
tournament_db   -> Tournament Service
notification_db -> Notification Service
```

### Shared Cache (Redis)

**Usages:**
- Sessions utilisateurs
- JWT blacklist
- Game state (sessions actives)
- Leaderboards
- Rate limiting
- Pub/Sub pour events

## Communication entre Services

### 1. Synchrone (REST)

Pour requêtes nécessitant réponse immédiate:

```java
// User Service appelle Auth Service pour validation token
@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @GetMapping("/api/auth/validate")
    ValidationResponse validateToken(@RequestHeader("Authorization") String token);
}
```

**Technologies:**
- Spring Cloud OpenFeign
- RestTemplate
- WebClient (reactive)

### 2. Asynchrone (Events)

Pour communications non-bloquantes:

```java
// Payment Service publie event après dépôt réussi
@Service
public class PaymentService {
    @Autowired
    private KafkaTemplate<String, DepositEvent> kafkaTemplate;

    public void publishDepositEvent(DepositEvent event) {
        kafkaTemplate.send("deposit-completed", event);
    }
}

// User Service écoute et met à jour balance
@KafkaListener(topics = "deposit-completed")
public void handleDepositCompleted(DepositEvent event) {
    userService.updateBalance(event.getUserId(), event.getAmount());
}
```

**Technologies:**
- Apache Kafka
- RabbitMQ (alternative)
- Spring Cloud Stream

## Sécurité

### JWT Flow

```
1. User login -> Auth Service
2. Auth Service génère JWT
3. Mobile App stocke JWT
4. Requêtes -> API Gateway avec JWT
5. Gateway valide JWT
6. Gateway route vers microservice
7. Microservice traite (userId extrait du JWT)
```

### JWT Structure

```json
{
  "sub": "user-id",
  "email": "user@example.com",
  "role": "PLAYER",
  "iat": 1234567890,
  "exp": 1234567890
}
```

### Service-to-Service Auth

```java
// Internal requests utilisent service tokens
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/internal/**").hasRole("SERVICE")
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

## Resilience

### Circuit Breaker

```java
@CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
public PaymentResponse processPayment(PaymentRequest request) {
    return paymentServiceClient.process(request);
}

public PaymentResponse paymentFallback(PaymentRequest request, Exception e) {
    // Fallback logic
    return PaymentResponse.failed("Service temporairement indisponible");
}
```

**Technologies:**
- Resilience4j
- Netflix Hystrix (deprecated)

### Retry Logic

```java
@Retryable(
    value = {ServiceUnavailableException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000)
)
public UserResponse getUser(String userId) {
    return userServiceClient.getUser(userId);
}
```

## Monitoring

### Health Checks

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Check database, Redis, external services
        if (databaseIsHealthy()) {
            return Health.up().build();
        }
        return Health.down().withDetail("reason", "DB connection failed").build();
    }
}
```

**Endpoints:**
```
GET /actuator/health
GET /actuator/metrics
GET /actuator/info
```

### Distributed Tracing

```
Request ID propagation à travers tous les services
```

**Technologies:**
- Spring Cloud Sleuth
- Zipkin
- Jaeger

### Logging

```java
@Slf4j
@Service
public class GameService {
    public GameResult spin(SpinRequest request) {
        MDC.put("userId", request.getUserId());
        MDC.put("requestId", request.getRequestId());
        log.info("Processing spin request");
        // Logic
    }
}
```

**Stack:**
- SLF4J + Logback
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Centralized logging

## Deployment

### Containerization (Docker)

Chaque microservice = 1 container

```dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/auth-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Orchestration (Kubernetes)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: casino/auth-service:latest
        ports:
        - containerPort: 8081
```

### CI/CD Pipeline

```
1. Git Push
2. Build (Maven)
3. Tests (Unit + Integration)
4. Docker Build
5. Push to Registry
6. Deploy to K8s (staging)
7. E2E Tests
8. Deploy to Production (manual approval)
```

## Scalabilité

### Horizontal Scaling

Chaque service peut être scalé indépendamment:

```bash
# Scaler game-service pendant pics de trafic
kubectl scale deployment game-service --replicas=10

# Scaler payment-service modérément
kubectl scale deployment payment-service --replicas=3
```

### Auto-Scaling

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: game-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: game-service
  minReplicas: 2
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

## Performance

### Caching Strategy

**Niveaux:**
1. API Gateway: Rate limiting, blacklist
2. Services: Données fréquentes (users, games config)
3. Database: Query result cache

```java
@Cacheable(value = "users", key = "#userId")
public User getUser(String userId) {
    return userRepository.findById(userId).orElseThrow();
}

@CacheEvict(value = "users", key = "#user.id")
public User updateUser(User user) {
    return userRepository.save(user);
}
```

### Database Optimization

- Indexes sur colonnes fréquemment requêtées
- Read replicas pour queries lourdes
- Connection pooling (HikariCP)
- Prepared statements

## Phase 1 - MVP Focus

Pour la Phase 1, on implémente :

### Services Essentiels:
1. ✅ **Auth Service** - Login/Register
2. ✅ **User Service** - Profils basiques
3. ✅ **Game Service** - 1 jeu simple (Slots)
4. ⏸️ **Payment Service** - Mode virtuel uniquement (pas Stripe encore)
5. ⏸️ **Tournament Service** - À venir Phase 2
6. ⏸️ **Notification Service** - Basique

### Infrastructure:
1. ✅ **API Gateway**
2. ✅ **Service Discovery**
3. ⏸️ **Config Server** - Optionnel Phase 1

### Features MVP:
- Authentification complète
- Profil utilisateur
- Balance virtuelle
- 1 jeu (Slots)
- Historique de jeu
- Leaderboard simple

## Avantages Microservices

✅ **Scalabilité indépendante** - Scaler game-service sans toucher auth
✅ **Déploiement indépendant** - Deploy payment sans redéployer tout
✅ **Technologies variées** - Possibilité d'utiliser Go pour performance
✅ **Isolation des pannes** - Si payment down, games continuent
✅ **Équipes autonomes** - Une équipe par service
✅ **Maintenance facilitée** - Code modulaire et isolé

## Challenges

⚠️ **Complexité** - Plus de services à gérer
⚠️ **Network latency** - Appels inter-services
⚠️ **Data consistency** - Transactions distribuées
⚠️ **Debugging** - Tracer les requêtes à travers services
⚠️ **DevOps** - Infrastructure plus complexe
