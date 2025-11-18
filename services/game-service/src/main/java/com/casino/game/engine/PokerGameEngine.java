package com.casino.game.engine;

import com.casino.game.dto.PokerResultData;
import com.casino.game.service.RngService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Texas Hold'em Poker Game Engine (Player vs House)
 * Simplified heads-up poker against dealer
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PokerGameEngine {

    private final RngService rngService;

    private static final double HOUSE_EDGE = 0.02; // 2% house edge

    private static final String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};

    public enum HandRank {
        HIGH_CARD(1),
        PAIR(2),
        TWO_PAIR(3),
        THREE_OF_A_KIND(4),
        STRAIGHT(5),
        FLUSH(6),
        FULL_HOUSE(7),
        FOUR_OF_A_KIND(8),
        STRAIGHT_FLUSH(9),
        ROYAL_FLUSH(10);

        final int value;

        HandRank(int value) {
            this.value = value;
        }
    }

    // Payout table based on hand rank (ante bet pays these multipliers)
    private static final Map<HandRank, Integer> PAYOUT_TABLE = Map.of(
        HandRank.PAIR, 1,
        HandRank.TWO_PAIR, 2,
        HandRank.THREE_OF_A_KIND, 3,
        HandRank.STRAIGHT, 4,
        HandRank.FLUSH, 5,
        HandRank.FULL_HOUSE, 8,
        HandRank.FOUR_OF_A_KIND, 20,
        HandRank.STRAIGHT_FLUSH, 50,
        HandRank.ROYAL_FLUSH, 100
    );

    /**
     * Play a round of Texas Hold'em (Player vs Dealer)
     */
    public PokerResultData play(String serverSeed, String clientSeed, long nonce, BigDecimal anteBet) {

        // Create and shuffle deck
        List<Card> deck = createDeck();
        shuffleDeck(deck, serverSeed, clientSeed, nonce);

        // Deal cards
        List<Card> playerHole = Arrays.asList(deck.get(0), deck.get(1));
        List<Card> dealerHole = Arrays.asList(deck.get(2), deck.get(3));
        List<Card> communityCards = Arrays.asList(
            deck.get(4),  // Flop 1
            deck.get(5),  // Flop 2
            deck.get(6),  // Flop 3
            deck.get(7),  // Turn
            deck.get(8)   // River
        );

        // Evaluate hands (best 5-card hand from 7 cards)
        List<Card> playerAllCards = new ArrayList<>(playerHole);
        playerAllCards.addAll(communityCards);

        List<Card> dealerAllCards = new ArrayList<>(dealerHole);
        dealerAllCards.addAll(communityCards);

        HandEvaluation playerHand = evaluateBestHand(playerAllCards);
        HandEvaluation dealerHand = evaluateBestHand(dealerAllCards);

        // Determine winner
        int comparison = compareHands(playerHand, dealerHand);
        String result;
        BigDecimal payout = BigDecimal.ZERO;

        if (comparison > 0) {
            // Player wins
            result = "PLAYER_WIN";
            int multiplier = PAYOUT_TABLE.getOrDefault(playerHand.handRank, 1);
            payout = anteBet.multiply(BigDecimal.valueOf(multiplier + 1))
                .setScale(2, RoundingMode.HALF_UP);
        } else if (comparison < 0) {
            // Dealer wins
            result = "DEALER_WIN";
            payout = BigDecimal.ZERO;
        } else {
            // Tie - push
            result = "TIE";
            payout = anteBet; // Return ante
        }

        BigDecimal profit = payout.subtract(anteBet);
        boolean isWin = payout.compareTo(anteBet) > 0;

        return PokerResultData.builder()
            .playerHole(convertCardsToString(playerHole))
            .dealerHole(convertCardsToString(dealerHole))
            .communityCards(convertCardsToString(communityCards))
            .playerHandRank(playerHand.handRank)
            .dealerHandRank(dealerHand.handRank)
            .playerBestCards(convertCardsToString(playerHand.bestCards))
            .dealerBestCards(convertCardsToString(dealerHand.bestCards))
            .result(result)
            .payout(payout)
            .profit(profit)
            .isWin(isWin)
            .serverSeed(serverSeed)
            .clientSeed(clientSeed)
            .nonce(nonce)
            .build();
    }

    /**
     * Create a standard 52-card deck
     */
    private List<Card> createDeck() {
        List<Card> deck = new ArrayList<>();
        for (String suit : SUITS) {
            for (int i = 0; i < RANKS.length; i++) {
                deck.add(new Card(RANKS[i], suit, i + 2)); // Value: 2-14 (A=14)
            }
        }
        return deck;
    }

    /**
     * Shuffle deck using provably fair RNG
     */
    private void shuffleDeck(List<Card> deck, String serverSeed, String clientSeed, long nonce) {
        for (int i = deck.size() - 1; i > 0; i--) {
            int j = rngService.generateRandomNumber(serverSeed, clientSeed, nonce + i, i + 1);
            Collections.swap(deck, i, j);
        }
    }

    /**
     * Evaluate best 5-card hand from 7 cards
     */
    private HandEvaluation evaluateBestHand(List<Card> cards) {
        List<List<Card>> allCombinations = generateCombinations(cards, 5);
        HandEvaluation best = null;

        for (List<Card> combination : allCombinations) {
            HandEvaluation evaluation = evaluateHand(combination);
            if (best == null || compareHands(evaluation, best) > 0) {
                best = evaluation;
            }
        }

        return best;
    }

    /**
     * Generate all 5-card combinations from 7 cards
     */
    private List<List<Card>> generateCombinations(List<Card> cards, int k) {
        List<List<Card>> result = new ArrayList<>();
        generateCombinationsHelper(cards, k, 0, new ArrayList<>(), result);
        return result;
    }

    private void generateCombinationsHelper(List<Card> cards, int k, int start,
                                           List<Card> current, List<List<Card>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            generateCombinationsHelper(cards, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    /**
     * Evaluate a 5-card poker hand
     */
    private HandEvaluation evaluateHand(List<Card> cards) {
        cards = new ArrayList<>(cards);
        cards.sort((a, b) -> b.value - a.value); // Sort descending

        boolean isFlush = cards.stream().map(c -> c.suit).distinct().count() == 1;
        boolean isStraight = checkStraight(cards);

        Map<Integer, Long> valueCounts = cards.stream()
            .collect(Collectors.groupingBy(c -> c.value, Collectors.counting()));

        List<Long> counts = new ArrayList<>(valueCounts.values());
        counts.sort(Collections.reverseOrder());

        // Check hands from highest to lowest
        if (isStraight && isFlush) {
            if (cards.get(0).value == 14) { // Ace high
                return new HandEvaluation(HandRank.ROYAL_FLUSH, cards);
            }
            return new HandEvaluation(HandRank.STRAIGHT_FLUSH, cards);
        }
        if (counts.get(0) == 4) {
            return new HandEvaluation(HandRank.FOUR_OF_A_KIND, cards);
        }
        if (counts.get(0) == 3 && counts.get(1) == 2) {
            return new HandEvaluation(HandRank.FULL_HOUSE, cards);
        }
        if (isFlush) {
            return new HandEvaluation(HandRank.FLUSH, cards);
        }
        if (isStraight) {
            return new HandEvaluation(HandRank.STRAIGHT, cards);
        }
        if (counts.get(0) == 3) {
            return new HandEvaluation(HandRank.THREE_OF_A_KIND, cards);
        }
        if (counts.get(0) == 2 && counts.get(1) == 2) {
            return new HandEvaluation(HandRank.TWO_PAIR, cards);
        }
        if (counts.get(0) == 2) {
            return new HandEvaluation(HandRank.PAIR, cards);
        }

        return new HandEvaluation(HandRank.HIGH_CARD, cards);
    }

    /**
     * Check if cards form a straight
     */
    private boolean checkStraight(List<Card> cards) {
        for (int i = 0; i < cards.size() - 1; i++) {
            if (cards.get(i).value - cards.get(i + 1).value != 1) {
                // Special case: A-2-3-4-5 (wheel)
                if (i == 0 && cards.get(0).value == 14 && cards.get(1).value == 5) {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Compare two poker hands (positive = hand1 wins, negative = hand2 wins, 0 = tie)
     */
    private int compareHands(HandEvaluation hand1, HandEvaluation hand2) {
        if (hand1.handRank.value != hand2.handRank.value) {
            return hand1.handRank.value - hand2.handRank.value;
        }

        // Same rank - compare high cards
        for (int i = 0; i < 5; i++) {
            int diff = hand1.bestCards.get(i).value - hand2.bestCards.get(i).value;
            if (diff != 0) {
                return diff;
            }
        }

        return 0; // Perfect tie
    }

    /**
     * Convert cards to display strings
     */
    private List<String> convertCardsToString(List<Card> cards) {
        return cards.stream()
            .map(c -> c.rank + c.suit)
            .collect(Collectors.toList());
    }

    /**
     * Card representation
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class Card {
        private String rank;
        private String suit;
        private int value; // Numeric value for comparison (2-14)
    }

    /**
     * Hand evaluation result
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class HandEvaluation {
        private HandRank handRank;
        private List<Card> bestCards;
    }
}
