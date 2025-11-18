package com.casino.game.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Keno game result data
 */
@Data
@Builder
public class KenoResultData {
    private List<Integer> pickedNumbers;              // Numbers player picked (1-10)
    private List<Integer> drawnNumbers;               // 20 numbers drawn (sorted)
    private List<Integer> matchedNumbers;             // Numbers that matched
    private Integer matchCount;                       // How many matches
    private Integer multiplier;                       // Payout multiplier
    private BigDecimal payout;                        // Total payout
    private BigDecimal profit;                        // Profit (payout - bet)
    private Boolean isWin;                            // Did player win?

    // Provably fair data
    private String serverSeed;
    private String clientSeed;
    private Long nonce;
}
