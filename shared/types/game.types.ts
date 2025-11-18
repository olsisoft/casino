export enum GameType {
  SLOT = 'slot',
  BLACKJACK = 'blackjack',
  ROULETTE = 'roulette',
  POKER = 'poker',
  CRAPS = 'craps',
  SIC_BO = 'sic_bo',
  BACCARAT = 'baccarat',
  VIDEO_POKER = 'video_poker',
}

export enum GameStatus {
  IDLE = 'idle',
  BETTING = 'betting',
  PLAYING = 'playing',
  RESOLVING = 'resolving',
  COMPLETED = 'completed',
}

export interface GameConfig {
  id: string;
  type: GameType;
  name: string;
  description: string;
  thumbnail: string;
  minBet: number;
  maxBet: number;
  rtp: number; // Return to Player percentage
  volatility: 'low' | 'medium' | 'high';
  maxPlayers?: number;
  isMultiplayer: boolean;
  features: string[];
  rules: Record<string, any>;
}

export interface GameSession {
  id: string;
  userId: string;
  gameId: string;
  gameType: GameType;
  status: GameStatus;
  betAmount: number;
  winAmount: number;
  startedAt: Date;
  endedAt?: Date;
  roundsPlayed: number;
  gameState: Record<string, any>;
}

export interface GameResult {
  sessionId: string;
  userId: string;
  gameType: GameType;
  betAmount: number;
  winAmount: number;
  netProfit: number;
  timestamp: Date;
  details: Record<string, any>;
}

// Slot specific types
export interface SlotSymbol {
  id: string;
  icon: string;
  value: number;
  multiplier: number;
  isWild?: boolean;
  isScatter?: boolean;
}

export interface SlotReelConfig {
  symbols: SlotSymbol[];
  rows: number;
  columns: number;
  paylines: number[][];
}

export interface SlotSpinResult {
  reels: string[][]; // symbol IDs
  winningLines: {
    line: number[];
    symbols: string;
    multiplier: number;
    payout: number;
  }[];
  totalWin: number;
  bonusTriggered?: {
    type: 'free_spins' | 'bonus_game' | 'multiplier';
    value: number;
  };
}

// Roulette specific types
export enum RouletteBetType {
  STRAIGHT = 'straight',
  SPLIT = 'split',
  STREET = 'street',
  CORNER = 'corner',
  LINE = 'line',
  DOZEN = 'dozen',
  COLUMN = 'column',
  RED_BLACK = 'red_black',
  EVEN_ODD = 'even_odd',
  HIGH_LOW = 'high_low',
}

export interface RouletteBet {
  type: RouletteBetType;
  numbers: number[];
  amount: number;
}

export interface RouletteSpinResult {
  number: number;
  color: 'red' | 'black' | 'green';
  bets: RouletteBet[];
  winningBets: {
    bet: RouletteBet;
    payout: number;
  }[];
  totalWin: number;
}

// Blackjack specific types
export enum BlackjackAction {
  HIT = 'hit',
  STAND = 'stand',
  DOUBLE = 'double',
  SPLIT = 'split',
  SURRENDER = 'surrender',
  INSURANCE = 'insurance',
}

export interface Card {
  suit: 'hearts' | 'diamonds' | 'clubs' | 'spades';
  rank: string;
  value: number;
}

export interface BlackjackHand {
  cards: Card[];
  value: number;
  isSoft: boolean;
  isBlackjack: boolean;
  isBusted: boolean;
}

export interface BlackjackGameState {
  dealerHand: BlackjackHand;
  playerHands: BlackjackHand[];
  currentHandIndex: number;
  deck: Card[];
  bets: number[];
}

// Poker specific types
export enum PokerHandRank {
  HIGH_CARD = 'high_card',
  PAIR = 'pair',
  TWO_PAIR = 'two_pair',
  THREE_OF_KIND = 'three_of_kind',
  STRAIGHT = 'straight',
  FLUSH = 'flush',
  FULL_HOUSE = 'full_house',
  FOUR_OF_KIND = 'four_of_kind',
  STRAIGHT_FLUSH = 'straight_flush',
  ROYAL_FLUSH = 'royal_flush',
}

export interface PokerHand {
  cards: Card[];
  rank: PokerHandRank;
  rankValue: number;
}

export interface PokerPlayer {
  userId: string;
  username: string;
  avatar?: string;
  chips: number;
  hand: Card[];
  currentBet: number;
  folded: boolean;
  isDealer: boolean;
}

export interface PokerGameState {
  players: PokerPlayer[];
  communityCards: Card[];
  pot: number;
  currentBet: number;
  currentPlayerIndex: number;
  round: 'preflop' | 'flop' | 'turn' | 'river' | 'showdown';
}
