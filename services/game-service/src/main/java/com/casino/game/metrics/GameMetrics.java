package com.casino.game.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Custom metrics for Game Service
 * Exposes business metrics to Prometheus
 */
@Component
@RequiredArgsConstructor
public class GameMetrics {

    private final MeterRegistry meterRegistry;

    /**
     * Record game played
     */
    public void recordGamePlayed(String gameType, String userId, boolean isWin, BigDecimal betAmount, BigDecimal payout) {
        // Total games counter
        Counter.builder("game_plays_total")
            .tag("game_type", gameType)
            .tag("result", isWin ? "win" : "loss")
            .description("Total number of games played")
            .register(meterRegistry)
            .increment();

        // Win/loss counters
        Counter.builder("game_results_total")
            .tag("game_type", gameType)
            .tag("result", isWin ? "win" : "loss")
            .description("Game results")
            .register(meterRegistry)
            .increment();

        // Bet amount
        meterRegistry.summary("game_bet_amount")
            .record(betAmount.doubleValue());

        // Payout amount
        meterRegistry.summary("game_payout_amount")
            .record(payout.doubleValue());

        // Profit/Loss
        BigDecimal profit = payout.subtract(betAmount);
        meterRegistry.summary("game_profit")
            .record(profit.doubleValue());

        // Calculate house edge
        if (betAmount.compareTo(BigDecimal.ZERO) > 0) {
            double houseEdge = profit.negate().divide(betAmount, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
            meterRegistry.gauge("game_house_edge", houseEdge);
        }
    }

    /**
     * Record game duration
     */
    public void recordGameDuration(String gameType, Duration duration) {
        Timer.builder("game_duration_seconds")
            .tag("game_type", gameType)
            .description("Game duration in seconds")
            .register(meterRegistry)
            .record(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Record RNG generation
     */
    public void recordRngGeneration(boolean success) {
        Counter.builder("game_rng_generation_total")
            .tag("result", success ? "success" : "failure")
            .description("RNG generation attempts")
            .register(meterRegistry)
            .increment();

        if (!success) {
            Counter.builder("game_rng_generation_failures_total")
                .description("RNG generation failures")
                .register(meterRegistry)
                .increment();
        }
    }

    /**
     * Record jackpot win
     */
    public void recordJackpotWin(String gameType, BigDecimal amount) {
        Counter.builder("game_jackpot_wins_total")
            .tag("game_type", gameType)
            .description("Total jackpot wins")
            .register(meterRegistry)
            .increment();

        meterRegistry.summary("game_jackpot_amount")
            .record(amount.doubleValue());
    }

    /**
     * Record big win (10x+ multiplier)
     */
    public void recordBigWin(String gameType, BigDecimal betAmount, BigDecimal payout) {
        BigDecimal multiplier = payout.divide(betAmount, 2, BigDecimal.ROUND_HALF_UP);

        if (multiplier.compareTo(BigDecimal.valueOf(10)) >= 0) {
            Counter.builder("game_big_wins_total")
                .tag("game_type", gameType)
                .description("Wins with 10x+ multiplier")
                .register(meterRegistry)
                .increment();

            meterRegistry.summary("game_big_win_multiplier")
                .record(multiplier.doubleValue());
        }
    }

    /**
     * Record active players
     */
    public void recordActivePlayers(String gameType, int count) {
        meterRegistry.gauge("game_active_players", count);
    }

    /**
     * Record game session
     */
    public void recordGameSession(String userId, Duration sessionDuration, int gamesPlayed) {
        Timer.builder("game_session_duration_seconds")
            .description("Game session duration")
            .register(meterRegistry)
            .record(sessionDuration.toMillis(), TimeUnit.MILLISECONDS);

        meterRegistry.summary("game_session_games_played")
            .record(gamesPlayed);
    }

    /**
     * Record bonus round
     */
    public void recordBonusRound(String gameType, boolean triggered, BigDecimal bonusPayout) {
        if (triggered) {
            Counter.builder("game_bonus_rounds_total")
                .tag("game_type", gameType)
                .description("Bonus rounds triggered")
                .register(meterRegistry)
                .increment();

            meterRegistry.summary("game_bonus_payout")
                .record(bonusPayout.doubleValue());
        }
    }

    /**
     * Record tournament participation
     */
    public void recordTournamentMetric(String metricType, String tournamentId, int value) {
        Counter.builder("tournament_" + metricType + "_total")
            .tag("tournament_id", tournamentId)
            .description("Tournament " + metricType)
            .register(meterRegistry)
            .increment(value);
    }
}
