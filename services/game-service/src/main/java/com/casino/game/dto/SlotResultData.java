package com.casino.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotResultData {
    private List<List<String>> reels; // 2D array of symbols
    private List<WinLine> winLines;
    private boolean bonusTriggered;
    private Integer freeSpinsAwarded;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WinLine {
        private Integer lineNumber;
        private String symbol;
        private Integer count;
        private java.math.BigDecimal payout;
    }
}
