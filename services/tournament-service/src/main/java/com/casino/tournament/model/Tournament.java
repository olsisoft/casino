package com.casino.tournament.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tournament entity
 * Competitive gaming tournaments with prize pools
 */
@Data
@Document(collection = "tournaments")
public class Tournament {

    @Id
    private String id;

    private String name;
    private String description;
    private String gameType;              // SLOTS, BLACKJACK, POKER, etc.
    private TournamentType tournamentType;
    private TournamentStatus status;

    // Timing
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime tournamentStart;
    private LocalDateTime tournamentEnd;

    // Entry
    private BigDecimal entryFee;
    private String currency;
    private Integer minParticipants;
    private Integer maxParticipants;
    private Integer currentParticipants;

    // Prize pool
    private BigDecimal prizePool;
    private PrizeDistribution prizeDistribution;
    private List<Prize> prizes;

    // Rules
    private Integer numberOfRounds;
    private Integer roundDuration;        // Minutes per round
    private BigDecimal startingChips;
    private Integer minBet;
    private Integer maxBet;

    // Leaderboard
    private LeaderboardType leaderboardType;
    private List<TournamentParticipant> participants;

    // Metadata
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String imageUrl;
    private List<String> tags;

    public enum TournamentType {
        SCHEDULED,           // Fixed start/end time
        SIT_AND_GO,         // Starts when full
        FREEROLL,           // Free entry
        GUARANTEED,         // Guaranteed prize pool
        SATELLITE,          // Winner gets entry to bigger tournament
        REBUY,              // Can rebuy chips
        FREEZEOUT           // One life only
    }

    public enum TournamentStatus {
        UPCOMING,           // Not started yet
        REGISTRATION_OPEN,  // Accepting registrations
        REGISTRATION_CLOSED,// Registration closed, waiting to start
        IN_PROGRESS,        // Tournament running
        COMPLETED,          // Finished
        CANCELLED           // Cancelled
    }

    public enum PrizeDistribution {
        WINNER_TAKES_ALL,   // 100% to 1st place
        TOP_3,              // Split among top 3 (50%, 30%, 20%)
        TOP_10,             // Split among top 10
        TOP_20_PERCENT,     // Top 20% of players
        CUSTOM              // Custom distribution
    }

    public enum LeaderboardType {
        HIGHEST_BALANCE,    // Most chips at end
        BIGGEST_WIN,        // Single biggest win
        TOTAL_WINS,         // Most wins
        WIN_STREAK,         // Longest win streak
        POINTS              // Custom points system
    }

    @Data
    public static class Prize {
        private Integer position;        // 1st, 2nd, 3rd, etc.
        private BigDecimal amount;
        private String description;      // "1st Place", "Runner-up", etc.
        private String specialPrize;     // Tickets, bonuses, merchandise
    }

    @Data
    public static class TournamentParticipant {
        private String userId;
        private String username;
        private LocalDateTime registeredAt;
        private BigDecimal currentBalance;
        private Integer gamesPlayed;
        private Integer gamesWon;
        private BigDecimal biggestWin;
        private Integer currentPosition;
        private Integer points;
        private Boolean eliminated;
    }
}
