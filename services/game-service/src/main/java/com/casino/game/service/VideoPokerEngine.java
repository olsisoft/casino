package com.casino.game.service;

import com.casino.game.dto.VideoPokerResultData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoPokerEngine {

    private final RngService rngService;

    private static final String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};

    // Jacks or Better paytable (for 1 coin bet)
    private static final Map<HandRank, Integer> PAYTABLE = Map.of(
        HandRank.ROYAL_FLUSH, 800,
        HandRank.STRAIGHT_FLUSH, 50,
        HandRank.FOUR_OF_A_KIND, 25,
        HandRank.FULL_HOUSE, 9,
        HandRank.FLUSH, 6,
        HandRank.STRAIGHT, 4,
        HandRank.THREE_OF_A_KIND, 3,
        HandRank.TWO_PAIR, 2,
        HandRank.JACKS_OR_BETTER, 1
    );

    /**
     * Deal initial 5 cards
     */
    public VideoPokerResultData deal(
        String serverSeed,
        String clientSeed,
        long nonce,
        BigDecimal betAmount
    ) {
        // Create and shuffle deck
        List<Card> deck = createDeck();
        deck = shuffleDeck(deck, serverSeed, clientSeed, nonce);

        // Deal 5 cards
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            hand.add(deck.remove(0));
        }

        return VideoPokerResultData.builder()
            .hand(cardsToString(hand))
            .deck(deck)
            .betAmount(betAmount)
            .handRank(null)
            .payout(BigDecimal.ZERO)
            .gameState("INITIAL_DEAL")
            .heldCards(new boolean[]{false, false, false, false, false})
            .build();
    }

    /**
     * Draw new cards for non-held positions
     */
    public VideoPokerResultData draw(VideoPokerResultData currentGame, boolean[] holdCards) {
        List<Card> hand = stringToCards(currentGame.getHand());
        List<Card> deck = currentGame.getDeck();

        // Replace non-held cards
        for (int i = 0; i < 5; i++) {
            if (!holdCards[i]) {
                hand.set(i, deck.remove(0));
            }
        }

        // Evaluate hand
        HandRank handRank = evaluateHand(hand);
        BigDecimal payout = calculatePayout(handRank, currentGame.getBetAmount());

        return VideoPokerResultData.builder()
            .hand(cardsToString(hand))
            .deck(deck)
            .betAmount(currentGame.getBetAmount())
            .handRank(handRank)
            .payout(payout)
            .gameState("COMPLETE")
            .heldCards(holdCards)
            .build();
    }

    /**
     * Evaluate poker hand rank
     */
    private HandRank evaluateHand(List<Card> hand) {
        // Sort by rank value
        hand.sort(Comparator.comparingInt(c -> getRankValue(c.getRank())));

        boolean isFlush = isFlush(hand);
        boolean isStraight = isStraight(hand);
        Map<String, Integer> rankCounts = getRankCounts(hand);

        // Royal Flush (10-J-Q-K-A of same suit)
        if (isFlush && isStraight && hand.get(0).getRank().equals("10") &&
            hand.get(4).getRank().equals("A")) {
            return HandRank.ROYAL_FLUSH;
        }

        // Straight Flush
        if (isFlush && isStraight) {
            return HandRank.STRAIGHT_FLUSH;
        }

        // Four of a Kind
        if (rankCounts.containsValue(4)) {
            return HandRank.FOUR_OF_A_KIND;
        }

        // Full House
        if (rankCounts.containsValue(3) && rankCounts.containsValue(2)) {
            return HandRank.FULL_HOUSE;
        }

        // Flush
        if (isFlush) {
            return HandRank.FLUSH;
        }

        // Straight
        if (isStraight) {
            return HandRank.STRAIGHT;
        }

        // Three of a Kind
        if (rankCounts.containsValue(3)) {
            return HandRank.THREE_OF_A_KIND;
        }

        // Two Pair
        long pairs = rankCounts.values().stream().filter(count -> count == 2).count();
        if (pairs == 2) {
            return HandRank.TWO_PAIR;
        }

        // Jacks or Better (pair of J, Q, K, or A)
        if (pairs == 1) {
            for (Map.Entry<String, Integer> entry : rankCounts.entrySet()) {
                if (entry.getValue() == 2) {
                    String rank = entry.getKey();
                    if (rank.equals("J") || rank.equals("Q") ||
                        rank.equals("K") || rank.equals("A")) {
                        return HandRank.JACKS_OR_BETTER;
                    }
                }
            }
        }

        return HandRank.NO_WIN;
    }

    private boolean isFlush(List<Card> hand) {
        String suit = hand.get(0).getSuit();
        return hand.stream().allMatch(card -> card.getSuit().equals(suit));
    }

    private boolean isStraight(List<Card> hand) {
        // Check regular straight
        boolean regularStraight = true;
        for (int i = 1; i < hand.size(); i++) {
            if (getRankValue(hand.get(i).getRank()) !=
                getRankValue(hand.get(i - 1).getRank()) + 1) {
                regularStraight = false;
                break;
            }
        }

        if (regularStraight) return true;

        // Check for A-2-3-4-5 (wheel)
        return hand.get(0).getRank().equals("2") &&
               hand.get(1).getRank().equals("3") &&
               hand.get(2).getRank().equals("4") &&
               hand.get(3).getRank().equals("5") &&
               hand.get(4).getRank().equals("A");
    }

    private Map<String, Integer> getRankCounts(List<Card> hand) {
        Map<String, Integer> counts = new HashMap<>();
        for (Card card : hand) {
            counts.merge(card.getRank(), 1, Integer::sum);
        }
        return counts;
    }

    private int getRankValue(String rank) {
        return switch (rank) {
            case "A" -> 14;
            case "K" -> 13;
            case "Q" -> 12;
            case "J" -> 11;
            default -> Integer.parseInt(rank);
        };
    }

    private BigDecimal calculatePayout(HandRank handRank, BigDecimal betAmount) {
        Integer multiplier = PAYTABLE.get(handRank);
        if (multiplier == null) {
            return BigDecimal.ZERO;
        }
        return betAmount.multiply(BigDecimal.valueOf(multiplier));
    }

    // Helper methods
    private List<Card> createDeck() {
        List<Card> deck = new ArrayList<>();
        for (String suit : SUITS) {
            for (String rank : RANKS) {
                deck.add(new Card(rank, suit));
            }
        }
        return deck;
    }

    private List<Card> shuffleDeck(List<Card> deck, String serverSeed, String clientSeed, long nonce) {
        return rngService.shuffle(deck, serverSeed, clientSeed, nonce);
    }

    private List<String> cardsToString(List<Card> cards) {
        return cards.stream()
            .map(c -> c.getRank() + c.getSuit())
            .collect(Collectors.toList());
    }

    private List<Card> stringToCards(List<String> strings) {
        return strings.stream()
            .map(s -> {
                String rank = s.substring(0, s.length() - 1);
                String suit = s.substring(s.length() - 1);
                return new Card(rank, suit);
            })
            .collect(Collectors.toList());
    }

    // Inner classes
    public static class Card {
        private final String rank;
        private final String suit;

        public Card(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }

        public String getRank() {
            return rank;
        }

        public String getSuit() {
            return suit;
        }
    }

    public enum HandRank {
        NO_WIN,
        JACKS_OR_BETTER,
        TWO_PAIR,
        THREE_OF_A_KIND,
        STRAIGHT,
        FLUSH,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        STRAIGHT_FLUSH,
        ROYAL_FLUSH
    }
}
