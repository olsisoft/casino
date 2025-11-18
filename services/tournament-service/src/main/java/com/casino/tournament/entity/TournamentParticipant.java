package com.casino.tournament.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_participants", indexes = {
    @Index(name = "idx_tournament_id", columnList = "tournamentId"),
    @Index(name = "idx_user_id", columnList = "userId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String tournamentId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String username;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal score;

    @Column(nullable = false)
    private Integer rank;

    @Column(precision = 19, scale = 2)
    private BigDecimal prizeWon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    private LocalDateTime eliminatedAt;

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
        if (score == null) score = BigDecimal.ZERO;
        if (rank == null) rank = 0;
        if (status == null) status = ParticipantStatus.REGISTERED;
    }

    public enum ParticipantStatus {
        REGISTERED,
        ACTIVE,
        ELIMINATED,
        WINNER
    }
}
