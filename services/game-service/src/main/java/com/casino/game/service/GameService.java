package com.casino.game.service;

import com.casino.game.dto.*;
import com.casino.game.entity.GameConfig;
import com.casino.game.entity.GameResult;
import com.casino.game.entity.GameSession;
import com.casino.game.exception.GameNotFoundException;
import com.casino.game.exception.InsufficientBalanceException;
import com.casino.game.exception.InvalidBetException;
import com.casino.game.exception.SessionNotFoundException;
import com.casino.game.repository.GameConfigRepository;
import com.casino.game.repository.GameResultRepository;
import com.casino.game.repository.GameSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameConfigRepository gameConfigRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GameResultRepository gameResultRepository;
    private final RngService rngService;
    private final SlotGameEngine slotGameEngine;
    private final BlackjackEngine blackjackEngine;
    private final RouletteEngine rouletteEngine;
    private final VideoPokerEngine videoPokerEngine;
    private final DiceGameEngine diceGameEngine;
    private final MinesGameEngine minesGameEngine;
    private final CrashGameEngine crashGameEngine;
    private final CoinFlipEngine coinFlipEngine;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Session timeout in minutes
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    /**
     * Get all available games
     */
    public List<GameConfigDto> getAllGames() {
        return gameConfigRepository.findByActiveTrue().stream()
            .map(this::toGameConfigDto)
            .collect(Collectors.toList());
    }

    /**
     * Get games by type
     */
    public List<GameConfigDto> getGamesByType(GameConfig.GameType gameType) {
        return gameConfigRepository.findByGameTypeAndActiveTrue(gameType, true).stream()
            .map(this::toGameConfigDto)
            .collect(Collectors.toList());
    }

    /**
     * Get most popular games
     */
    public List<GameConfigDto> getMostPopularGames() {
        return gameConfigRepository.findMostPopular().stream()
            .limit(10)
            .map(this::toGameConfigDto)
            .collect(Collectors.toList());
    }

    /**
     * Get a specific game by code
     */
    public GameConfigDto getGame(String gameCode) {
        GameConfig game = gameConfigRepository.findByGameCode(gameCode)
            .orElseThrow(() -> new GameNotFoundException("Game not found: " + gameCode));
        return toGameConfigDto(game);
    }

    /**
     * Start a new game session
     */
    @Transactional
    public GameSessionDto startSession(String userId, StartSessionRequest request) {
        // Verify game exists
        GameConfig game = gameConfigRepository.findByGameCode(request.getGameCode())
            .orElseThrow(() -> new GameNotFoundException("Game not found: " + request.getGameCode()));

        if (!game.getActive()) {
            throw new GameNotFoundException("Game is not active: " + request.getGameCode());
        }

        // Check if user has an active session for this game
        gameSessionRepository.findFirstByUserIdAndStatusOrderByStartedAtDesc(
                userId,
                GameSession.SessionStatus.ACTIVE
            )
            .ifPresent(session -> {
                // End the previous session
                endSession(session.getId(), userId);
            });

        // Create new session
        GameSession session = GameSession.builder()
            .userId(userId)
            .gameCode(request.getGameCode())
            .startingBalance(request.getStartingBalance())
            .currentBalance(request.getStartingBalance())
            .balanceType(request.getBalanceType())
            .build();

        session = gameSessionRepository.save(session);

        // Update game statistics
        gameConfigRepository.incrementPlayCount(request.getGameCode());

        log.info("Started session {} for user {} on game {}", session.getId(), userId, request.getGameCode());

        return toGameSessionDto(session);
    }

    /**
     * Play a round in a game session
     */
    @Transactional
    public PlayRoundResponse playRound(String userId, PlayRoundRequest request) {
        // Get the session
        GameSession session = gameSessionRepository.findByIdAndUserId(request.getSessionId(), userId)
            .orElseThrow(() -> new SessionNotFoundException("Session not found or does not belong to user"));

        if (session.getStatus() != GameSession.SessionStatus.ACTIVE) {
            throw new SessionNotFoundException("Session is not active");
        }

        // Get game config
        GameConfig game = gameConfigRepository.findByGameCode(session.getGameCode())
            .orElseThrow(() -> new GameNotFoundException("Game not found: " + session.getGameCode()));

        // Validate bet amount
        validateBet(request.getBetAmount(), game, session);

        // Generate server seed and nonce
        String serverSeed = rngService.generateServerSeed();
        Long nonce = (gameResultRepository.getLastRoundNumber(session.getId()) != null)
            ? gameResultRepository.getLastRoundNumber(session.getId()) + 1
            : 1L;

        // Play the round based on game type
        PlayRoundResponse response = switch (game.getGameType()) {
            case SLOTS -> playSlotRound(session, game, request, serverSeed, nonce);
            case BLACKJACK -> playBlackjackRound(session, game, request, serverSeed, nonce);
            case ROULETTE -> playRouletteRound(session, game, request, serverSeed, nonce);
            case VIDEO_POKER -> playVideoPokerRound(session, game, request, serverSeed, nonce);
            case DICE -> playDiceRound(session, game, request, serverSeed, nonce);
            case MINES -> playMinesRound(session, game, request, serverSeed, nonce);
            case CRASH -> playCrashRound(session, game, request, serverSeed, nonce);
            case COIN_FLIP -> playCoinFlipRound(session, game, request, serverSeed, nonce);
            default -> throw new UnsupportedOperationException(
                "Game type not yet implemented: " + game.getGameType()
            );
        };

        // Update session balance
        gameSessionRepository.updateBalance(
            session.getId(),
            response.getBalanceAfter(),
            LocalDateTime.now()
        );

        // Update session statistics
        gameSessionRepository.updateSessionStats(
            session.getId(),
            request.getBetAmount(),
            response.getWinAmount(),
            response.getNetProfit(),
            response.getOutcome() == GameResult.RoundOutcome.WIN,
            LocalDateTime.now()
        );

        // Update game statistics
        gameConfigRepository.addToTotalWagered(game.getGameCode(), request.getBetAmount());
        if (response.getWinAmount().compareTo(BigDecimal.ZERO) > 0) {
            gameConfigRepository.addToTotalPaidOut(game.getGameCode(), response.getWinAmount());
        }

        log.info("Round {} completed for session {}. Outcome: {}, Win: {}",
            response.getRoundNumber(), session.getId(), response.getOutcome(), response.getWinAmount());

        return response;
    }

    /**
     * Play a slot machine round
     */
    private PlayRoundResponse playSlotRound(
        GameSession session,
        GameConfig game,
        PlayRoundRequest request,
        String serverSeed,
        Long nonce
    ) {
        BigDecimal balanceBefore = session.getCurrentBalance();
        BigDecimal betAmount = request.getBetAmount();

        // Spin the reels
        SlotResultData slotResult = slotGameEngine.spin(
            game,
            betAmount,
            serverSeed,
            request.getClientSeed(),
            nonce
        );

        // Calculate winnings
        BigDecimal winAmount = slotGameEngine.calculateTotalPayout(slotResult);
        BigDecimal netProfit = winAmount.subtract(betAmount);
        BigDecimal balanceAfter = balanceBefore.add(netProfit);

        // Determine outcome
        GameResult.RoundOutcome outcome;
        if (slotResult.isBonusTriggered()) {
            outcome = GameResult.RoundOutcome.BONUS_TRIGGERED;
        } else if (winAmount.compareTo(BigDecimal.ZERO) > 0) {
            outcome = GameResult.RoundOutcome.WIN;
        } else {
            outcome = GameResult.RoundOutcome.LOSS;
        }

        // Calculate multiplier
        BigDecimal multiplier = BigDecimal.ZERO;
        if (betAmount.compareTo(BigDecimal.ZERO) > 0) {
            multiplier = winAmount.divide(betAmount, 2, RoundingMode.HALF_UP);
        }

        // Save result
        GameResult result = GameResult.builder()
            .sessionId(session.getId())
            .userId(session.getUserId())
            .gameCode(session.getGameCode())
            .roundNumber(nonce)
            .outcome(outcome)
            .betAmount(betAmount)
            .winAmount(winAmount)
            .netProfit(netProfit)
            .multiplier(multiplier)
            .balanceBefore(balanceBefore)
            .balanceAfter(balanceAfter)
            .resultJson(slotGameEngine.toJson(slotResult))
            .serverSeed(serverSeed)
            .clientSeed(request.getClientSeed())
            .nonce(nonce)
            .build();

        result = gameResultRepository.save(result);

        // Update session current balance
        session.setCurrentBalance(balanceAfter);

        return PlayRoundResponse.builder()
            .resultId(result.getId())
            .roundNumber(nonce)
            .outcome(outcome)
            .betAmount(betAmount)
            .winAmount(winAmount)
            .netProfit(netProfit)
            .multiplier(multiplier)
            .balanceBefore(balanceBefore)
            .balanceAfter(balanceAfter)
            .resultData(slotGameEngine.toJson(slotResult))
            .serverSeed(serverSeed)
            .nonce(nonce)
            .isBigWin(result.isBigWin())
            .isMegaWin(result.isMegaWin())
            .build();
    }

    /**
     * Play a blackjack round
     */
    private PlayRoundResponse playBlackjackRound(
        GameSession session,
        GameConfig game,
        PlayRoundRequest request,
        String serverSeed,
        Long nonce
    ) {
        BigDecimal balanceBefore = session.getCurrentBalance();
        BigDecimal betAmount = request.getBetAmount();

        // Start blackjack game
        BlackjackResultData blackjackResult = blackjackEngine.startGame(
            serverSeed,
            request.getClientSeed(),
            nonce,
            betAmount
        );

        BigDecimal winAmount = blackjackResult.getPayout() != null ?
            blackjackResult.getPayout() : BigDecimal.ZERO;
        BigDecimal netProfit = winAmount.subtract(betAmount);
        BigDecimal balanceAfter = balanceBefore.add(netProfit);

        // Determine outcome
        GameResult.RoundOutcome outcome = switch (blackjackResult.getGameState()) {
            case "BLACKJACK" -> GameResult.RoundOutcome.WIN;
            case "WIN" -> GameResult.RoundOutcome.WIN;
            case "LOSS" -> GameResult.RoundOutcome.LOSS;
            case "PUSH" -> GameResult.RoundOutcome.PUSH;
            default -> GameResult.RoundOutcome.PLAYING;
        };

        BigDecimal multiplier = betAmount.compareTo(BigDecimal.ZERO) > 0 ?
            winAmount.divide(betAmount, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        GameResult result = saveGameResult(
            session, serverSeed, request.getClientSeed(), nonce,
            betAmount, winAmount, netProfit, multiplier,
            balanceBefore, balanceAfter, outcome,
            toJson(blackjackResult)
        );

        return buildPlayRoundResponse(result, serverSeed, nonce, toJson(blackjackResult));
    }

    /**
     * Play a roulette round
     */
    private PlayRoundResponse playRouletteRound(
        GameSession session,
        GameConfig game,
        PlayRoundRequest request,
        String serverSeed,
        Long nonce
    ) {
        BigDecimal balanceBefore = session.getCurrentBalance();
        BigDecimal betAmount = request.getBetAmount();

        // Get roulette type and bet from request data
        RouletteEngine.RouletteType rouletteType = RouletteEngine.RouletteType.EUROPEAN;
        String betType = "red"; // Default, should come from request

        // Spin roulette
        RouletteResultData rouletteResult = rouletteEngine.spin(
            serverSeed,
            request.getClientSeed(),
            nonce,
            betAmount,
            betType,
            rouletteType
        );

        BigDecimal winAmount = rouletteResult.getPayout();
        BigDecimal netProfit = winAmount.subtract(betAmount);
        BigDecimal balanceAfter = balanceBefore.add(netProfit);

        GameResult.RoundOutcome outcome = rouletteResult.getIsWin() ?
            GameResult.RoundOutcome.WIN : GameResult.RoundOutcome.LOSS;

        BigDecimal multiplier = rouletteResult.getMultiplier();

        GameResult result = saveGameResult(
            session, serverSeed, request.getClientSeed(), nonce,
            betAmount, winAmount, netProfit, multiplier,
            balanceBefore, balanceAfter, outcome,
            toJson(rouletteResult)
        );

        return buildPlayRoundResponse(result, serverSeed, nonce, toJson(rouletteResult));
    }

    /**
     * Play a video poker round
     */
    private PlayRoundResponse playVideoPokerRound(
        GameSession session,
        GameConfig game,
        PlayRoundRequest request,
        String serverSeed,
        Long nonce
    ) {
        BigDecimal balanceBefore = session.getCurrentBalance();
        BigDecimal betAmount = request.getBetAmount();

        // Deal initial hand
        VideoPokerResultData pokerResult = videoPokerEngine.dealInitialHand(
            serverSeed,
            request.getClientSeed(),
            nonce,
            betAmount
        );

        BigDecimal winAmount = pokerResult.getPayout() != null ?
            pokerResult.getPayout() : BigDecimal.ZERO;
        BigDecimal netProfit = winAmount.subtract(betAmount);
        BigDecimal balanceAfter = balanceBefore.add(netProfit);

        GameResult.RoundOutcome outcome = winAmount.compareTo(BigDecimal.ZERO) > 0 ?
            GameResult.RoundOutcome.WIN : GameResult.RoundOutcome.LOSS;

        BigDecimal multiplier = betAmount.compareTo(BigDecimal.ZERO) > 0 ?
            winAmount.divide(betAmount, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        GameResult result = saveGameResult(
            session, serverSeed, request.getClientSeed(), nonce,
            betAmount, winAmount, netProfit, multiplier,
            balanceBefore, balanceAfter, outcome,
            toJson(pokerResult)
        );

        return buildPlayRoundResponse(result, serverSeed, nonce, toJson(pokerResult));
    }

    /**
     * Play a dice round
     */
    private PlayRoundResponse playDiceRound(
        GameSession session,
        GameConfig game,
        PlayRoundRequest request,
        String serverSeed,
        Long nonce
    ) {
        BigDecimal balanceBefore = session.getCurrentBalance();
        BigDecimal betAmount = request.getBetAmount();

        // Get target and direction from request (defaults for now)
        int targetNumber = 50;
        boolean rollOver = true;

        // Roll dice
        DiceGameResultData diceResult = diceGameEngine.roll(
            serverSeed,
            request.getClientSeed(),
            nonce,
            betAmount,
            targetNumber,
            rollOver
        );

        BigDecimal winAmount = diceResult.getPayout();
        BigDecimal netProfit = diceResult.getNetProfit();
        BigDecimal balanceAfter = balanceBefore.add(netProfit);

        GameResult.RoundOutcome outcome = diceResult.getIsWin() ?
            GameResult.RoundOutcome.WIN : GameResult.RoundOutcome.LOSS;

        BigDecimal multiplier = diceResult.getMultiplier();

        GameResult result = saveGameResult(
            session, serverSeed, request.getClientSeed(), nonce,
            betAmount, winAmount, netProfit, multiplier,
            balanceBefore, balanceAfter, outcome,
            toJson(diceResult)
        );

        return buildPlayRoundResponse(result, serverSeed, nonce, toJson(diceResult));
    }

    /**
     * Play a mines round
     */
    private PlayRoundResponse playMinesRound(
        GameSession session,
        GameConfig game,
        PlayRoundRequest request,
        String serverSeed,
        Long nonce
    ) {
        BigDecimal balanceBefore = session.getCurrentBalance();
        BigDecimal betAmount = request.getBetAmount();

        // Get number of mines from request (default for now)
        int numberOfMines = 3;

        // Start mines game
        MinesGameResultData minesResult = minesGameEngine.startGame(
            serverSeed,
            request.getClientSeed(),
            nonce,
            betAmount,
            numberOfMines
        );

        BigDecimal winAmount = minesResult.getCurrentPayout();
        BigDecimal netProfit = winAmount.subtract(betAmount);
        BigDecimal balanceAfter = balanceBefore.add(netProfit);

        GameResult.RoundOutcome outcome = GameResult.RoundOutcome.PLAYING;

        BigDecimal multiplier = minesResult.getCurrentMultiplier();

        GameResult result = saveGameResult(
            session, serverSeed, request.getClientSeed(), nonce,
            betAmount, winAmount, netProfit, multiplier,
            balanceBefore, balanceAfter, outcome,
            toJson(minesResult)
        );

        return buildPlayRoundResponse(result, serverSeed, nonce, toJson(minesResult));
    }

    /**
     * Play a crash round
     */
    private PlayRoundResponse playCrashRound(
        GameSession session,
        GameConfig game,
        PlayRoundRequest request,
        String serverSeed,
        Long nonce
    ) {
        BigDecimal balanceBefore = session.getCurrentBalance();
        BigDecimal betAmount = request.getBetAmount();

        // Get auto cashout from request (optional)
        BigDecimal autoCashoutAt = null; // Can be set from request

        // Play crash game
        CrashGameResultData crashResult = crashGameEngine.play(
            serverSeed,
            request.getClientSeed(),
            nonce,
            betAmount,
            autoCashoutAt
        );

        BigDecimal winAmount = crashResult.getPayout();
        BigDecimal netProfit = crashResult.getNetProfit();
        BigDecimal balanceAfter = balanceBefore.add(netProfit);

        GameResult.RoundOutcome outcome = crashResult.getIsWin() ?
            GameResult.RoundOutcome.WIN : GameResult.RoundOutcome.LOSS;

        BigDecimal multiplier = crashResult.getCashedOutAt() != null ?
            crashResult.getCashedOutAt() : BigDecimal.ZERO;

        GameResult result = saveGameResult(
            session, serverSeed, request.getClientSeed(), nonce,
            betAmount, winAmount, netProfit, multiplier,
            balanceBefore, balanceAfter, outcome,
            toJson(crashResult)
        );

        return buildPlayRoundResponse(result, serverSeed, nonce, toJson(crashResult));
    }

    /**
     * Play a coin flip round
     */
    private PlayRoundResponse playCoinFlipRound(
        GameSession session,
        GameConfig game,
        PlayRoundRequest request,
        String serverSeed,
        Long nonce
    ) {
        BigDecimal balanceBefore = session.getCurrentBalance();
        BigDecimal betAmount = request.getBetAmount();

        // Get player choice from request (default for now)
        CoinFlipEngine.CoinSide playerChoice = CoinFlipEngine.CoinSide.HEADS;

        // Flip coin
        CoinFlipResultData coinResult = coinFlipEngine.flip(
            serverSeed,
            request.getClientSeed(),
            nonce,
            betAmount,
            playerChoice
        );

        BigDecimal winAmount = coinResult.getPayout();
        BigDecimal netProfit = coinResult.getNetProfit();
        BigDecimal balanceAfter = balanceBefore.add(netProfit);

        GameResult.RoundOutcome outcome = coinResult.getIsWin() ?
            GameResult.RoundOutcome.WIN : GameResult.RoundOutcome.LOSS;

        BigDecimal multiplier = coinResult.getMultiplier();

        GameResult result = saveGameResult(
            session, serverSeed, request.getClientSeed(), nonce,
            betAmount, winAmount, netProfit, multiplier,
            balanceBefore, balanceAfter, outcome,
            toJson(coinResult)
        );

        return buildPlayRoundResponse(result, serverSeed, nonce, toJson(coinResult));
    }

    /**
     * Helper method to save game result
     */
    private GameResult saveGameResult(
        GameSession session,
        String serverSeed,
        String clientSeed,
        Long nonce,
        BigDecimal betAmount,
        BigDecimal winAmount,
        BigDecimal netProfit,
        BigDecimal multiplier,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        GameResult.RoundOutcome outcome,
        String resultJson
    ) {
        GameResult result = GameResult.builder()
            .sessionId(session.getId())
            .userId(session.getUserId())
            .gameCode(session.getGameCode())
            .roundNumber(nonce)
            .outcome(outcome)
            .betAmount(betAmount)
            .winAmount(winAmount)
            .netProfit(netProfit)
            .multiplier(multiplier)
            .balanceBefore(balanceBefore)
            .balanceAfter(balanceAfter)
            .resultJson(resultJson)
            .serverSeed(serverSeed)
            .clientSeed(clientSeed)
            .nonce(nonce)
            .build();

        return gameResultRepository.save(result);
    }

    /**
     * Helper method to build play round response
     */
    private PlayRoundResponse buildPlayRoundResponse(
        GameResult result,
        String serverSeed,
        Long nonce,
        String resultData
    ) {
        return PlayRoundResponse.builder()
            .resultId(result.getId())
            .roundNumber(nonce)
            .outcome(result.getOutcome())
            .betAmount(result.getBetAmount())
            .winAmount(result.getWinAmount())
            .netProfit(result.getNetProfit())
            .multiplier(result.getMultiplier())
            .balanceBefore(result.getBalanceBefore())
            .balanceAfter(result.getBalanceAfter())
            .resultData(resultData)
            .serverSeed(serverSeed)
            .nonce(nonce)
            .isBigWin(result.isBigWin())
            .isMegaWin(result.isMegaWin())
            .build();
    }

    /**
     * Convert object to JSON string
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error converting to JSON", e);
            return "{}";
        }
    }

    /**
     * Get current session for a user
     */
    public GameSessionDto getCurrentSession(String userId) {
        GameSession session = gameSessionRepository
            .findFirstByUserIdAndStatusOrderByStartedAtDesc(userId, GameSession.SessionStatus.ACTIVE)
            .orElseThrow(() -> new SessionNotFoundException("No active session found"));

        return toGameSessionDto(session);
    }

    /**
     * Get session history for a user
     */
    public List<GameSessionDto> getSessionHistory(String userId) {
        return gameSessionRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
            .map(this::toGameSessionDto)
            .collect(Collectors.toList());
    }

    /**
     * End a game session
     */
    @Transactional
    public void endSession(String sessionId, String userId) {
        GameSession session = gameSessionRepository.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new SessionNotFoundException("Session not found"));

        if (session.getStatus() == GameSession.SessionStatus.COMPLETED) {
            return; // Already completed
        }

        gameSessionRepository.endSession(
            sessionId,
            GameSession.SessionStatus.COMPLETED,
            LocalDateTime.now()
        );

        // Decrement active player count
        gameConfigRepository.decrementActiveCount(session.getGameCode());

        log.info("Session {} ended for user {}", sessionId, userId);
    }

    /**
     * Clean up abandoned sessions
     */
    @Transactional
    public void cleanupAbandonedSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES);
        List<GameSession> abandonedSessions = gameSessionRepository.findInactiveSessions(
            GameSession.SessionStatus.ACTIVE,
            cutoff
        );

        for (GameSession session : abandonedSessions) {
            gameSessionRepository.endSession(
                session.getId(),
                GameSession.SessionStatus.ABANDONED,
                LocalDateTime.now()
            );
            gameConfigRepository.decrementActiveCount(session.getGameCode());
        }

        if (!abandonedSessions.isEmpty()) {
            log.info("Cleaned up {} abandoned sessions", abandonedSessions.size());
        }
    }

    /**
     * Validate bet amount
     */
    private void validateBet(BigDecimal betAmount, GameConfig game, GameSession session) {
        if (betAmount.compareTo(game.getMinBet()) < 0) {
            throw new InvalidBetException("Bet amount is below minimum: " + game.getMinBet());
        }

        if (betAmount.compareTo(game.getMaxBet()) > 0) {
            throw new InvalidBetException("Bet amount exceeds maximum: " + game.getMaxBet());
        }

        if (betAmount.compareTo(session.getCurrentBalance()) > 0) {
            throw new InsufficientBalanceException("Insufficient balance for bet");
        }
    }

    // DTO Converters

    private GameConfigDto toGameConfigDto(GameConfig game) {
        return GameConfigDto.builder()
            .id(game.getId())
            .gameCode(game.getGameCode())
            .gameName(game.getGameName())
            .gameType(game.getGameType())
            .description(game.getDescription())
            .imageUrl(game.getImageUrl())
            .active(game.getActive())
            .rtpPercentage(game.getRtpPercentage())
            .minBet(game.getMinBet())
            .maxBet(game.getMaxBet())
            .reels(game.getReels())
            .rows(game.getRows())
            .paylines(game.getPaylines())
            .totalPlays(game.getTotalPlays())
            .activePlayers(game.getActivePlayers())
            .build();
    }

    private GameSessionDto toGameSessionDto(GameSession session) {
        return GameSessionDto.builder()
            .id(session.getId())
            .userId(session.getUserId())
            .gameCode(session.getGameCode())
            .status(session.getStatus())
            .startingBalance(session.getStartingBalance())
            .currentBalance(session.getCurrentBalance())
            .totalBet(session.getTotalBet())
            .totalWon(session.getTotalWon())
            .netProfit(session.getNetProfit())
            .roundsPlayed(session.getRoundsPlayed())
            .roundsWon(session.getRoundsWon())
            .roundsLost(session.getRoundsLost())
            .biggestWin(session.getBiggestWin())
            .biggestLoss(session.getBiggestLoss())
            .startedAt(session.getStartedAt())
            .endedAt(session.getEndedAt())
            .durationSeconds(session.getDurationSeconds())
            .balanceType(session.getBalanceType())
            .build();
    }
}
