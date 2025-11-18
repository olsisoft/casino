package com.casino.game.dto;

import com.casino.game.entity.GameConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameConfigDto {
    private String id;
    private String gameCode;
    private String gameName;
    private GameConfig.GameType gameType;
    private String description;
    private String imageUrl;
    private Boolean active;
    private BigDecimal rtpPercentage;
    private BigDecimal minBet;
    private BigDecimal maxBet;
    private Integer reels;
    private Integer rows;
    private Integer paylines;
    private Long totalPlays;
    private Long activePlayers;
}
