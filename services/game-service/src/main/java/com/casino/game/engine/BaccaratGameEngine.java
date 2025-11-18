package com.casino.game.engine;

import com.casino.game.dto.BaccaratResultData;
import com.casino.game.service.RngService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Baccarat Game Engine
 * Classic casino card game - Player vs Banker
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaccaratGameEngine {

    private final RngService rngService;

    private static final double HOUSE_EDGE_BANKER = 0.0106; // 1.06% on banker
    private static final double HOUSE_EDGE_PLAYER = 0.0124; // 1.24% on player
    private static final double HOUSE_EDGE_TIE = 0.1436;    // 14.36% on tie

    // Card ranks (1-13 representing A, 2-10, J, Q, K)
    private static final String[] RANKS = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};

    public enum BetType {
        PLAYER,
        BANKER,
        TIE
    }

    public enum Winner {
        PLAYER,
        BANKER,
        TIE
    }

    /**
     * Play a round of Baccarat
     */
    public BaccaratResultData play(String serverSeed, String clientSeed, long nonce,
                                    BetType betType, BigDecimal betAmount) {

        // Deal initial cards (2 to player, 2 to banker)
        List<Card> playerCards = new ArrayList<>();
        List<Card> bankerCards = new ArrayList<>();

        playerCards.add(drawCard(serverSeed, clientSeed, nonce));
        bankerCards.add(drawCard(serverSeed, clientSeed, nonce + 1));
        playerCards.add(drawCard(serverSeed, clientSeed, nonce + 2));
        bankerCards.add(drawCard(serverSeed, clientSeed, nonce + 3));

        int playerScore = calculateScore(playerCards);
        int bankerScore = calculateScore(bankerCards);

        long cardNonce = nonce + 4;

        // Natural check (8 or 9)
        boolean playerNatural = playerScore >= 8;
        boolean bankerNatural = bankerScore >= 8;

        if (!playerNatural && !bankerNatural) {
            // Player's third card rule
            if (playerScore <= 5) {
                Card playerThirdCard = drawCard(serverSeed, clientSeed, cardNonce++);
                playerCards.add(playerThirdCard);
                playerScore = calculateScore(playerCards);

                // Banker's third card rule (depends on player's third card)
                int playerThirdValue = getCardValue(playerThirdCard);
                if (shouldBankerDrawThirdCard(bankerScore, playerThirdValue)) {
                    bankerCards.add(drawCard(serverSeed, clientSeed, cardNonce++));
                    bankerScore = calculateScore(bankerCards);
                }
            } else {
                // Player stands, banker draws on 0-5
                if (bankerScore <= 5) {
                    bankerCards.add(drawCard(serverSeed, clientSeed, cardNonce++));
                    bankerScore = calculateScore(bankerCards);
                }
            }
        }

        // Determine winner
        Winner winner;
        if (playerScore > bankerScore) {
            winner = Winner.PLAYER;
        } else if (bankerScore > playerScore) {
            winner = Winner.BANKER;
        } else {
            winner = Winner.TIE;
        }

        // Calculate payout
        BigDecimal payout = calculatePayout(betType, betAmount, winner);
        BigDecimal profit = payout.subtract(betAmount);
        boolean isWin = payout.compareTo(betAmount) > 0;

        return BaccaratResultData.builder()
            .playerCards(convertCardsToString(playerCards))
            .bankerCards(convertCardsToString(bankerCards))
            .playerScore(playerScore)
            .bankerScore(bankerScore)
            .winner(winner)
            .betType(betType)
            .payout(payout)
            .profit(profit)
            .isWin(isWin)
            .serverSeed(serverSeed)
            .clientSeed(clientSeed)
            .nonce(nonce)
            .build();
    }

    /**
     * Draw a random card
     */
    private Card drawCard(String serverSeed, String clientSeed, long nonce) {
        int cardIndex = rngService.generateRandomNumber(serverSeed, clientSeed, nonce, 52);
        int rankIndex = cardIndex % 13;
        int suitIndex = cardIndex / 13;

        return new Card(RANKS[rankIndex], SUITS[suitIndex]);
    }

    /**
     * Calculate baccarat score (0-9)
     */
    private int calculateScore(List<Card> cards) {
        int total = 0;
        for (Card card : cards) {
            total += getCardValue(card);
        }
        return total % 10; // Only last digit counts
    }

    /**
     * Get card value in baccarat (A=1, 2-9=face, 10/J/Q/K=0)
     */
    private int getCardValue(Card card) {
        switch (card.rank) {
            case "A": return 1;
            case "2": return 2;
            case "3": return 3;
            case "4": return 4;
            case "5": return 5;
            case "6": return 6;
            case "7": return 7;
            case "8": return 8;
            case "9": return 9;
            default: return 0; // 10, J, Q, K
        }
    }

    /**
     * Complex banker third card drawing rules
     */
    private boolean shouldBankerDrawThirdCard(int bankerScore, int playerThirdCardValue) {
        switch (bankerScore) {
            case 0:
            case 1:
            case 2:
                return true; // Always draw
            case 3:
                return playerThirdCardValue != 8; // Draw unless player drew 8
            case 4:
                return playerThirdCardValue >= 2 && playerThirdCardValue <= 7;
            case 5:
                return playerThirdCardValue >= 4 && playerThirdCardValue <= 7;
            case 6:
                return playerThirdCardValue == 6 || playerThirdCardValue == 7;
            default:
                return false; // Stand on 7+
        }
    }

    /**
     * Calculate payout based on bet type and winner
     */
    private BigDecimal calculatePayout(BetType betType, BigDecimal betAmount, Winner winner) {
        if (betType == BetType.PLAYER && winner == Winner.PLAYER) {
            // Player bet pays 1:1
            return betAmount.multiply(BigDecimal.valueOf(2)).setScale(2, RoundingMode.HALF_UP);
        } else if (betType == BetType.BANKER && winner == Winner.BANKER) {
            // Banker bet pays 0.95:1 (5% commission)
            return betAmount.add(betAmount.multiply(BigDecimal.valueOf(0.95)))
                .setScale(2, RoundingMode.HALF_UP);
        } else if (betType == BetType.TIE && winner == Winner.TIE) {
            // Tie bet pays 8:1
            return betAmount.multiply(BigDecimal.valueOf(9)).setScale(2, RoundingMode.HALF_UP);
        } else if (winner == Winner.TIE) {
            // Push on tie (return bet)
            return betAmount;
        } else {
            // Loss
            return BigDecimal.ZERO;
        }
    }

    /**
     * Convert cards to display strings
     */
    private List<String> convertCardsToString(List<Card> cards) {
        List<String> result = new ArrayList<>();
        for (Card card : cards) {
            result.add(card.rank + card.suit);
        }
        return result;
    }

    /**
     * Card representation
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class Card {
        private String rank;
        private String suit;
    }
}
