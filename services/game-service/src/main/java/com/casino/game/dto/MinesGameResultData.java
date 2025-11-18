package com.casino.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MinesGameResultData {
    private BigDecimal betAmount;
    private Integer numberOfMines;
    private Set<Integer> minePositions; // Positions of mines (hidden until game ends)
    private Set<Integer> revealedPositions; // Positions revealed by player
    private Integer gemsFound;
    private BigDecimal currentMultiplier;
    private BigDecimal currentPayout;
    private String gameState; // PLAYING, BUSTED, CASHED_OUT
    private Boolean canCashout;
    private String serverSeed;
    private Long nonce;
}
