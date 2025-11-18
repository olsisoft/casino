# Phase 1 - Implémentation MVP

## 🎯 Objectifs Phase 1

Créer un MVP fonctionnel avec :
- ✅ Authentification complète
- ✅ Gestion utilisateurs
- ✅ Un jeu simple (Slots)
- ✅ Balance virtuelle
- ✅ Architecture microservices prête

## 📋 Checklist Implémentation

### ✅ Infrastructure (Complété)

- [x] Architecture microservices définie
- [x] Structure des dossiers
- [x] Docker Compose configuration
- [x] Service Discovery (Eureka)
- [x] API Gateway
- [x] PostgreSQL (6 databases)
- [x] Redis
- [x] Documentation complète

### 🔄 Auth Service (À Implémenter)

**Entités à créer:**

```java
// User.java
@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;
    private String email;
    private String username;
    private String password; // BCrypt hashed
    private UserRole role;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}

// RefreshToken.java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    private String id;
    private String userId;
    private String token;
    private LocalDateTime expiresAt;
}
```

**Services à implémenter:**

```java
// AuthService.java
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String userId);
    void verifyEmail(String token);
    void resetPassword(String email);
}

// JwtService.java
public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    boolean validateToken(String token);
    String getUserIdFromToken(String token);
}
```

**Controllers:**

```java
@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request);

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request);

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token);
}
```

**Temps estimé:** 2-3 jours

### 🔄 User Service (À Implémenter)

**Entités à créer:**

```java
// UserProfile.java
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    private String userId;
    private String firstName;
    private String lastName;
    private String avatar;
    private LocalDate dateOfBirth;
    private String country;
    private Integer level;
    private Long xp;
}

// UserBalance.java
@Entity
@Table(name = "user_balances")
public class UserBalance {
    @Id
    private String userId;
    private BigDecimal virtualBalance; // Phase 1: Virtual only
    private BigDecimal bonusBalance;
    private String currency;
    private LocalDateTime lastUpdated;
}

// UserSettings.java
@Entity
@Table(name = "user_settings")
public class UserSettings {
    @Id
    private String userId;
    private String language;
    private String theme;
    private boolean soundEnabled;
    private boolean notificationsEnabled;
}
```

**Services:**

```java
// UserService.java
public interface UserService {
    UserProfile getProfile(String userId);
    UserProfile updateProfile(String userId, UpdateProfileRequest request);
    UserBalance getBalance(String userId);
    void addVirtualBalance(String userId, BigDecimal amount);
    void deductBalance(String userId, BigDecimal amount);
    UserSettings getSettings(String userId);
    UserSettings updateSettings(String userId, UserSettings settings);
}
```

**Temps estimé:** 2-3 jours

### 🔄 Game Service (À Implémenter)

**Entités:**

```java
// GameConfig.java
@Entity
@Table(name = "game_configs")
public class GameConfig {
    @Id
    private String id;
    private GameType type;
    private String name;
    private String description;
    private BigDecimal minBet;
    private BigDecimal maxBet;
    private Double rtp; // Return to Player %
    private String configJson; // Game-specific config
}

// GameSession.java
@Entity
@Table(name = "game_sessions")
public class GameSession {
    @Id
    private String id;
    private String userId;
    private String gameId;
    private GameType gameType;
    private GameStatus status;
    private BigDecimal totalBet;
    private BigDecimal totalWin;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}

// GameResult.java
@Entity
@Table(name = "game_results")
public class GameResult {
    @Id
    private String id;
    private String sessionId;
    private String userId;
    private String gameId;
    private BigDecimal betAmount;
    private BigDecimal winAmount;
    private String resultJson; // Game-specific result
    private LocalDateTime timestamp;
}
```

**Logique Slots:**

```java
// SlotGame.java
public class SlotGame {

    // Configuration
    private static final String[][] REELS = {
        {"🍒", "🍋", "🍊", "🍇", "💎", "7️⃣"},
        {"🍒", "🍋", "🍊", "🍇", "💎", "7️⃣"},
        {"🍒", "🍋", "🍊", "🍇", "💎", "7️⃣"}
    };

    private static final Map<String, Integer> PAYOUTS = Map.of(
        "7️⃣7️⃣7️⃣", 100,  // Jackpot
        "💎💎💎", 50,
        "🍇🍇🍇", 25,
        "🍊🍊🍊", 15,
        "🍋🍋🍋", 10,
        "🍒🍒🍒", 5
    );

    public SlotSpinResult spin(BigDecimal betAmount) {
        // 1. Generate random symbols
        String[] result = new String[3];
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 3; i++) {
            int index = random.nextInt(REELS[i].length);
            result[i] = REELS[i][index];
        }

        // 2. Check for win
        String combination = String.join("", result);
        Integer multiplier = PAYOUTS.getOrDefault(combination, 0);

        // 3. Calculate win amount
        BigDecimal winAmount = betAmount.multiply(BigDecimal.valueOf(multiplier));

        // 4. Return result
        return SlotSpinResult.builder()
            .symbols(result)
            .winAmount(winAmount)
            .multiplier(multiplier)
            .isWin(multiplier > 0)
            .build();
    }
}
```

**RNG Service:**

```java
// RNGService.java
@Service
public class RNGService {
    private final SecureRandom secureRandom;

    public RNGService() {
        this.secureRandom = new SecureRandom();
    }

    public int getRandomInt(int min, int max) {
        return secureRandom.nextInt(max - min + 1) + min;
    }

    public double getRandomDouble() {
        return secureRandom.nextDouble();
    }
}
```

**Services:**

```java
// GameService.java
public interface GameService {
    List<GameConfig> getAllGames();
    GameConfig getGame(String gameId);
    GameSession startSession(String userId, String gameId);
    SlotSpinResult spinSlot(String userId, String sessionId, BigDecimal betAmount);
    GameSession endSession(String sessionId);
    List<GameResult> getHistory(String userId, int page, int size);
}
```

**Temps estimé:** 3-4 jours

### 🔄 API Gateway (À Implémenter)

**Filtres personnalisés:**

```java
// AuthenticationFilter.java
@Component
public class AuthenticationFilter implements GatewayFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = extractToken(exchange.getRequest());

        if (token == null || !jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userId = jwtUtil.getUserIdFromToken(token);
        exchange.getRequest().mutate()
            .header("X-User-Id", userId)
            .build();

        return chain.filter(exchange);
    }
}

// RateLimitingFilter.java
@Component
public class RateLimitingFilter implements GatewayFilter {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String key = "rate_limit:" + userId;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        if (count > 100) { // 100 requests per minute
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
```

**Temps estimé:** 1-2 jours

### 🔄 Frontend React Native (À Implémenter)

**Structure:**

```
frontend/casino-mobile/src/
├── screens/
│   ├── Auth/
│   │   ├── LoginScreen.tsx
│   │   ├── RegisterScreen.tsx
│   │   └── styles.ts
│   ├── Home/
│   │   ├── HomeScreen.tsx
│   │   └── styles.ts
│   ├── Games/
│   │   ├── SlotsScreen.tsx
│   │   ├── GameListScreen.tsx
│   │   └── styles.ts
│   └── Profile/
│       ├── ProfileScreen.tsx
│       ├── BalanceScreen.tsx
│       └── styles.ts
├── components/
│   ├── SlotMachine/
│   │   ├── SlotMachine.tsx
│   │   ├── Reel.tsx
│   │   └── animations.ts
│   └── common/
│       ├── Button.tsx
│       ├── Input.tsx
│       └── Card.tsx
├── services/
│   ├── api.service.ts
│   ├── auth.service.ts
│   ├── user.service.ts
│   └── game.service.ts
├── store/
│   ├── slices/
│   │   ├── authSlice.ts
│   │   ├── userSlice.ts
│   │   └── gameSlice.ts
│   └── store.ts
└── navigation/
    ├── AppNavigator.tsx
    └── AuthNavigator.tsx
```

**Composant Slots:**

```typescript
// SlotMachine.tsx
import React, { useState } from 'react';
import { View, TouchableOpacity, Text } from 'react-native';
import Animated, { useAnimatedStyle, withSpring } from 'react-native-reanimated';
import { gameService } from '@/services/game.service';

export const SlotMachine: React.FC = () => {
  const [symbols, setSymbols] = useState(['🍒', '🍒', '🍒']);
  const [spinning, setSpinning] = useState(false);
  const [balance, setBalance] = useState(1000);
  const [betAmount] = useState(10);

  const spin = async () => {
    if (spinning || balance < betAmount) return;

    setSpinning(true);
    setBalance(prev => prev - betAmount);

    // Animate reels
    // ... animation logic

    try {
      const result = await gameService.spinSlot(sessionId, betAmount);

      setSymbols(result.symbols);

      if (result.isWin) {
        setBalance(prev => prev + result.winAmount);
        // Show win animation
      }
    } catch (error) {
      console.error('Spin failed:', error);
      setBalance(prev => prev + betAmount); // Refund
    } finally {
      setSpinning(false);
    }
  };

  return (
    <View>
      <Text>Balance: ${balance}</Text>

      <View style={styles.reels}>
        {symbols.map((symbol, index) => (
          <Reel key={index} symbol={symbol} spinning={spinning} />
        ))}
      </View>

      <TouchableOpacity onPress={spin} disabled={spinning}>
        <Text>SPIN - ${betAmount}</Text>
      </TouchableOpacity>
    </View>
  );
};
```

**Services API:**

```typescript
// game.service.ts
import axios from 'axios';
import { API_GATEWAY_URL } from '@env';

class GameService {
  private baseUrl = `${API_GATEWAY_URL}/games`;

  async getGames() {
    const response = await axios.get(`${this.baseUrl}/list`);
    return response.data;
  }

  async startSession(gameId: string) {
    const response = await axios.post(`${this.baseUrl}/session/start`, {
      gameId,
    });
    return response.data;
  }

  async spinSlot(sessionId: string, betAmount: number) {
    const response = await axios.post(`${this.baseUrl}/slots/spin`, {
      sessionId,
      betAmount,
    });
    return response.data;
  }

  async getHistory(page = 0, size = 20) {
    const response = await axios.get(`${this.baseUrl}/history`, {
      params: { page, size },
    });
    return response.data;
  }
}

export const gameService = new GameService();
```

**Temps estimé:** 4-5 jours

## 📅 Timeline Phase 1

| Semaine | Tâches | Temps |
|---------|--------|-------|
| **Semaine 1** | Auth Service + User Service | 5 jours |
| **Semaine 2** | Game Service (Slots) | 4 jours |
| **Semaine 3** | API Gateway + Integration | 3 jours |
| **Semaine 4** | Frontend (Screens + Navigation) | 5 jours |
| **Semaine 5** | Frontend (Slots Component) | 3 jours |
| **Semaine 6** | Tests + Bug fixes | 5 jours |
| **Semaine 7** | Polish + Documentation | 3 jours |

**Total estimé:** 6-7 semaines

## 🧪 Plan de Tests

### Tests Unitaires

**Backend:**
```java
@SpringBootTest
class AuthServiceTest {
    @Test
    void shouldRegisterNewUser() {
        RegisterRequest request = new RegisterRequest(
            "test@example.com",
            "testuser",
            "Password123!"
        );

        AuthResponse response = authService.register(request);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    void shouldLoginExistingUser() {
        // Test login
    }
}

@SpringBootTest
class SlotGameTest {
    @Test
    void shouldGenerateValidSpinResult() {
        SlotGame slotGame = new SlotGame();
        SlotSpinResult result = slotGame.spin(BigDecimal.TEN);

        assertNotNull(result.getSymbols());
        assertEquals(3, result.getSymbols().length);
        assertTrue(result.getWinAmount().compareTo(BigDecimal.ZERO) >= 0);
    }
}
```

**Frontend:**
```typescript
describe('SlotMachine', () => {
  it('should spin when button pressed', async () => {
    const { getByText } = render(<SlotMachine />);
    const spinButton = getByText(/SPIN/i);

    fireEvent.press(spinButton);

    await waitFor(() => {
      expect(gameService.spinSlot).toHaveBeenCalled();
    });
  });
});
```

### Tests d'Intégration

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class GameFlowIntegrationTest {

    @Test
    void completeGameFlow() {
        // 1. Register
        AuthResponse auth = registerUser();

        // 2. Start game session
        GameSession session = startGameSession(auth.getAccessToken());

        // 3. Spin slots
        SlotSpinResult result = spinSlot(session.getId(), BigDecimal.TEN);

        // 4. Check balance updated
        UserBalance balance = getBalance(auth.getAccessToken());

        // Assert balance changed correctly
    }
}
```

## 🚀 Déploiement Phase 1

### Local Development

```bash
# 1. Démarrer infrastructure
docker-compose up -d postgres redis service-discovery

# 2. Démarrer services (en parallèle)
cd services/auth-service && mvn spring-boot:run &
cd services/user-service && mvn spring-boot:run &
cd services/game-service && mvn spring-boot:run &
cd infrastructure/api-gateway && mvn spring-boot:run &

# 3. Démarrer frontend
cd frontend/casino-mobile
npm start
npm run android # ou ios
```

### Staging (Docker)

```bash
docker-compose up -d
```

## 📊 Métriques de Succès Phase 1

- ✅ Tous les services démarrent sans erreur
- ✅ Registration/Login fonctionnel
- ✅ Balance virtuelle fonctionne
- ✅ Slots jouable et fun
- ✅ Historique des jeux sauvegardé
- ✅ Tests passent (>80% coverage)
- ✅ Documentation complète
- ✅ Performance acceptable (<200ms response time)

## 🔜 Après Phase 1

**Phase 2 Focus:**
- Payment Service (Stripe integration)
- 2 jeux additionnels (Roulette, Blackjack)
- Tournament Service
- Achievements système
- Performance optimizations

## 📝 Notes d'Implémentation

### Sécurité

- ✅ Passwords hashed avec BCrypt (strength: 10)
- ✅ JWT avec expiration courte (15min)
- ✅ Refresh tokens (7 jours)
- ✅ HTTPS en production
- ✅ Input validation partout
- ✅ SQL injection protection (JPA)
- ✅ XSS protection
- ✅ CORS configuré

### Performance

- ✅ Redis caching (user profiles, game configs)
- ✅ Database indexes (userId, sessionId, timestamp)
- ✅ Connection pooling (HikariCP)
- ✅ Lazy loading JPA relations
- ✅ API pagination
- ✅ Frontend: React Native Reanimated pour animations

### Monitoring

- ✅ Actuator health checks
- ✅ Prometheus metrics
- ✅ Structured logging (JSON)
- ✅ Request tracing (correlation ID)

## ✅ Prochaine Action

**Pour commencer l'implémentation:**

1. Créer les entités JPA pour Auth Service
2. Implémenter AuthService
3. Créer les tests
4. Répéter pour User et Game Service
5. Implémenter Frontend

Voulez-vous que je commence par implémenter un service spécifique ?
