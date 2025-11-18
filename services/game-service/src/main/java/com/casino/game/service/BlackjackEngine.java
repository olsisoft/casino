package com.casino.game.service;

import com.casino.game.dto.BlackjackResultData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlackjackEngine {

    private final RngService rngService;

    private static final String[] RANKS = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};

    /**
     * Start a new Blackjack game
     */
    public BlackjackResultData startGame(
        String serverSeed,
        String clientSeed,
        long nonce,
        BigDecimal betAmount
    ) {
        // Create and shuffle deck
        List<Card> deck = createDeck();
        deck = shuffleDeck(deck, serverSeed, clientSeed, nonce);

        // Deal initial cards
        List<Card> playerHand = new ArrayList<>();
        List<Card> dealerHand = new ArrayList<>();

        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));
        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));

        int playerValue = calculateHandValue(playerHand);
        int dealerValue = calculateHandValue(dealerHand);

        // Check for natural blackjack
        boolean playerBlackjack = playerValue == 21 && playerHand.size() == 2;
        boolean dealerBlackjack = dealerValue == 21 && dealerHand.size() == 2;

        String gameState;
        BigDecimal payout = BigDecimal.ZERO;

        if (playerBlackjack && dealerBlackjack) {
            gameState = "PUSH";
            payout = betAmount;
        } else if (playerBlackjack) {
            gameState = "BLACKJACK";
            payout = betAmount.multiply(new BigDecimal("2.5")); // 3:2 payout
        } else if (dealerBlackjack) {
            gameState = "DEALER_BLACKJACK";
            payout = BigDecimal.ZERO;
        } else {
            gameState = "PLAYING";
        }

        return BlackjackResultData.builder()
            .playerHand(cardsToString(playerHand))
            .dealerHand(cardsToString(dealerHand))
            .dealerVisible(List.of(cardToString(dealerHand.get(0)))) // Only first card visible
            .playerValue(playerValue)
            .dealerValue(calculateHandValue(List.of(dealerHand.get(0))))
            .gameState(gameState)
            .deck(deck)
            .betAmount(betAmount)
            .payout(payout)
            .canHit(gameState.equals("PLAYING"))
            .canStand(gameState.equals("PLAYING"))
            .canDouble(gameState.equals("PLAYING") && playerHand.size() == 2)
            .canSplit(gameState.equals("PLAYING") && canSplit(playerHand))
            .build();
    }

    /**
     * Player hits (takes another card)
     */
    public BlackjackResultData hit(BlackjackResultData currentGame) {
        List<Card> playerHand = stringToCards(currentGame.getPlayerHand());
        List<Card> dealerHand = stringToCards(currentGame.getDealerHand());
        List<Card> deck = currentGame.getDeck();

        // Deal one card to player
        playerHand.add(deck.remove(0));

        int playerValue = calculateHandValue(playerHand);
        String gameState = currentGame.getGameState();
        BigDecimal payout = currentGame.getPayout();

        // Check for bust
        if (playerValue > 21) {
            gameState = "BUST";
            payout = BigDecimal.ZERO;
        }

        return BlackjackResultData.builder()
            .playerHand(cardsToString(playerHand))
            .dealerHand(cardsToString(dealerHand))
            .dealerVisible(currentGame.getDealerVisible())
            .playerValue(playerValue)
            .dealerValue(currentGame.getDealerValue())
            .gameState(gameState)
            .deck(deck)
            .betAmount(currentGame.getBetAmount())
            .payout(payout)
            .canHit(!gameState.equals("BUST"))
            .canStand(!gameState.equals("BUST"))
            .canDouble(false)
            .canSplit(false)
            .build();
    }

    /**
     * Player stands (dealer plays)
     */
    public BlackjackResultData stand(BlackjackResultData currentGame) {
        List<Card> playerHand = stringToCards(currentGame.getPlayerHand());
        List<Card> dealerHand = stringToCards(currentGame.getDealerHand());
        List<Card> deck = currentGame.getDeck();

        int playerValue = calculateHandValue(playerHand);

        // Dealer plays (hits until 17+)
        while (calculateHandValue(dealerHand) < 17) {
            dealerHand.add(deck.remove(0));
        }

        int dealerValue = calculateHandValue(dealerHand);

        // Determine winner
        String gameState;
        BigDecimal payout;

        if (dealerValue > 21) {
            gameState = "WIN";
            payout = currentGame.getBetAmount().multiply(BigDecimal.valueOf(2));
        } else if (playerValue > dealerValue) {
            gameState = "WIN";
            payout = currentGame.getBetAmount().multiply(BigDecimal.valueOf(2));
        } else if (playerValue < dealerValue) {
            gameState = "LOSE";
            payout = BigDecimal.ZERO;
        } else {
            gameState = "PUSH";
            payout = currentGame.getBetAmount();
        }

        return BlackjackResultData.builder()
            .playerHand(cardsToString(playerHand))
            .dealerHand(cardsToString(dealerHand))
            .dealerVisible(cardsToString(dealerHand))
            .playerValue(playerValue)
            .dealerValue(dealerValue)
            .gameState(gameState)
            .deck(deck)
            .betAmount(currentGame.getBetAmount())
            .payout(payout)
            .canHit(false)
            .canStand(false)
            .canDouble(false)
            .canSplit(false)
            .build();
    }

    /**
     * Player doubles down
     */
    public BlackjackResultData doubleDown(BlackjackResultData currentGame) {
        List<Card> playerHand = stringToCards(currentGame.getPlayerHand());
        List<Card> dealerHand = stringToCards(currentGame.getDealerHand());
        List<Card> deck = currentGame.getDeck();

        // Double the bet
        BigDecimal newBet = currentGame.getBetAmount().multiply(BigDecimal.valueOf(2));

        // Deal one card to player
        playerHand.add(deck.remove(0));

        int playerValue = calculateHandValue(playerHand);

        // Check for bust
        if (playerValue > 21) {
            return BlackjackResultData.builder()
                .playerHand(cardsToString(playerHand))
                .dealerHand(cardsToString(dealerHand))
                .dealerVisible(currentGame.getDealerVisible())
                .playerValue(playerValue)
                .dealerValue(currentGame.getDealerValue())
                .gameState("BUST")
                .deck(deck)
                .betAmount(newBet)
                .payout(BigDecimal.ZERO)
                .canHit(false)
                .canStand(false)
                .canDouble(false)
                .canSplit(false)
                .build();
        }

        // Dealer plays
        while (calculateHandValue(dealerHand) < 17) {
            dealerHand.add(deck.remove(0));
        }

        int dealerValue = calculateHandValue(dealerHand);

        // Determine winner
        String gameState;
        BigDecimal payout;

        if (dealerValue > 21 || playerValue > dealerValue) {
            gameState = "WIN";
            payout = newBet.multiply(BigDecimal.valueOf(2));
        } else if (playerValue < dealerValue) {
            gameState = "LOSE";
            payout = BigDecimal.ZERO;
        } else {
            gameState = "PUSH";
            payout = newBet;
        }

        return BlackjackResultData.builder()
            .playerHand(cardsToString(playerHand))
            .dealerHand(cardsToString(dealerHand))
            .dealerVisible(cardsToString(dealerHand))
            .playerValue(playerValue)
            .dealerValue(dealerValue)
            .gameState(gameState)
            .deck(deck)
            .betAmount(newBet)
            .payout(payout)
            .canHit(false)
            .canStand(false)
            .canDouble(false)
            .canSplit(false)
            .build();
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

    private int calculateHandValue(List<Card> hand) {
        int value = 0;
        int aces = 0;

        for (Card card : hand) {
            String rank = card.getRank();
            if (rank.equals("A")) {
                aces++;
                value += 11;
            } else if (rank.equals("J") || rank.equals("Q") || rank.equals("K")) {
                value += 10;
            } else {
                value += Integer.parseInt(rank);
            }
        }

        // Adjust for aces
        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }

        return value;
    }

    private boolean canSplit(List<Card> hand) {
        if (hand.size() != 2) return false;
        Card card1 = hand.get(0);
        Card card2 = hand.get(1);
        return card1.getRank().equals(card2.getRank());
    }

    private List<String> cardsToString(List<Card> cards) {
        return cards.stream()
            .map(this::cardToString)
            .collect(Collectors.toList());
    }

    private String cardToString(Card card) {
        return card.getRank() + card.getSuit();
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

    // Inner class for Card
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

        @Override
        public String toString() {
            return rank + suit;
        }
    }
}
