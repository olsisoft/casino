package com.casino.game.service;

import com.casino.game.dto.MinesGameResultData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinesGameEngine {

    private final RngService rngService;

    private static final int GRID_SIZE = 25; // 5x5 grid
    private static final double HOUSE_EDGE = 0.01; // 1%

    /**
     * Start a new Mines game
     */
    public MinesGameResultData startGame(
        String serverSeed,
        String clientSeed,
        long nonce,
        BigDecimal betAmount,
        int numberOfMines
    ) {
        // Validate mines count (1-24)
        if (numberOfMines < 1 || numberOfMines >= GRID_SIZE) {
            throw new IllegalArgumentException("Number of mines must be between 1 and 24");
        }

        // Generate mine positions
        Set<Integer> minePositions = generateMinePositions(serverSeed, clientSeed, nonce, numberOfMines);

        return MinesGameResultData.builder()
            .betAmount(betAmount)
            .numberOfMines(numberOfMines)
            .minePositions(minePositions)
            .revealedPositions(new HashSet<>())
            .gemsFound(0)
            .currentMultiplier(BigDecimal.ONE)
            .currentPayout(betAmount)
            .gameState("PLAYING")
            .canCashout(false)
            .serverSeed(serverSeed)
            .nonce(nonce)
            .build();
    }

    /**
     * Reveal a tile
     */
    public MinesGameResultData revealTile(MinesGameResultData currentGame, int position) {
        // Validate position
        if (position < 0 || position >= GRID_SIZE) {
            throw new IllegalArgumentException("Invalid position");
        }

        // Check if already revealed
        if (currentGame.getRevealedPositions().contains(position)) {
            throw new IllegalArgumentException("Position already revealed");
        }

        // Check if game is still playing
        if (!currentGame.getGameState().equals("PLAYING")) {
            throw new IllegalArgumentException("Game is not in playing state");
        }

        Set<Integer> revealedPositions = new HashSet<>(currentGame.getRevealedPositions());
        revealedPositions.add(position);

        // Check if mine
        if (currentGame.getMinePositions().contains(position)) {
            return MinesGameResultData.builder()
                .betAmount(currentGame.getBetAmount())
                .numberOfMines(currentGame.getNumberOfMines())
                .minePositions(currentGame.getMinePositions())
                .revealedPositions(revealedPositions)
                .gemsFound(currentGame.getGemsFound())
                .currentMultiplier(currentGame.getCurrentMultiplier())
                .currentPayout(BigDecimal.ZERO)
                .gameState("BUSTED")
                .canCashout(false)
                .serverSeed(currentGame.getServerSeed())
                .nonce(currentGame.getNonce())
                .build();
        }

        // Found a gem
        int gemsFound = currentGame.getGemsFound() + 1;
        BigDecimal multiplier = calculateMultiplier(
            currentGame.getNumberOfMines(),
            gemsFound
        );

        BigDecimal payout = currentGame.getBetAmount().multiply(multiplier);

        return MinesGameResultData.builder()
            .betAmount(currentGame.getBetAmount())
            .numberOfMines(currentGame.getNumberOfMines())
            .minePositions(currentGame.getMinePositions())
            .revealedPositions(revealedPositions)
            .gemsFound(gemsFound)
            .currentMultiplier(multiplier)
            .currentPayout(payout)
            .gameState("PLAYING")
            .canCashout(true)
            .serverSeed(currentGame.getServerSeed())
            .nonce(currentGame.getNonce())
            .build();
    }

    /**
     * Cash out current winnings
     */
    public MinesGameResultData cashout(MinesGameResultData currentGame) {
        if (!currentGame.canCashout()) {
            throw new IllegalArgumentException("Cannot cashout in current state");
        }

        return MinesGameResultData.builder()
            .betAmount(currentGame.getBetAmount())
            .numberOfMines(currentGame.getNumberOfMines())
            .minePositions(currentGame.getMinePositions())
            .revealedPositions(currentGame.getRevealedPositions())
            .gemsFound(currentGame.getGemsFound())
            .currentMultiplier(currentGame.getCurrentMultiplier())
            .currentPayout(currentGame.getCurrentPayout())
            .gameState("CASHED_OUT")
            .canCashout(false)
            .serverSeed(currentGame.getServerSeed())
            .nonce(currentGame.getNonce())
            .build();
    }

    /**
     * Generate mine positions
     */
    private Set<Integer> generateMinePositions(
        String serverSeed,
        String clientSeed,
        long nonce,
        int numberOfMines
    ) {
        // Create list of all positions
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            positions.add(i);
        }

        // Shuffle positions
        positions = rngService.shuffle(positions, serverSeed, clientSeed, nonce);

        // Take first N positions as mines
        return new HashSet<>(positions.subList(0, numberOfMines));
    }

    /**
     * Calculate multiplier based on mines and gems found
     */
    private BigDecimal calculateMultiplier(int numberOfMines, int gemsFound) {
        int totalTiles = GRID_SIZE;
        int safeTiles = totalTiles - numberOfMines;

        // Calculate probability
        double probability = 1.0;
        for (int i = 0; i < gemsFound; i++) {
            probability *= (double) (safeTiles - i) / (totalTiles - i);
        }

        // Multiplier = 1 / probability * (1 - house edge)
        BigDecimal multiplier = BigDecimal.ONE
            .divide(BigDecimal.valueOf(probability), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(1.0 - HOUSE_EDGE));

        return multiplier.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get possible multipliers for all gem counts
     */
    public Map<Integer, BigDecimal> getMultiplierTable(int numberOfMines) {
        Map<Integer, BigDecimal> table = new LinkedHashMap<>();
        int maxGems = GRID_SIZE - numberOfMines;

        for (int gems = 1; gems <= maxGems; gems++) {
            table.put(gems, calculateMultiplier(numberOfMines, gems));
        }

        return table;
    }
}
