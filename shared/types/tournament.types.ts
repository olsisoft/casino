export enum TournamentType {
  SCHEDULED = 'scheduled',
  SIT_AND_GO = 'sit_and_go',
  FREEROLL = 'freeroll',
  GUARANTEED = 'guaranteed',
}

export enum TournamentStatus {
  UPCOMING = 'upcoming',
  REGISTERING = 'registering',
  IN_PROGRESS = 'in_progress',
  COMPLETED = 'completed',
  CANCELLED = 'cancelled',
}

export interface Tournament {
  id: string;
  name: string;
  description: string;
  type: TournamentType;
  gameType: string;
  status: TournamentStatus;
  entryFee: number;
  prizePool: number;
  guaranteedPrizePool?: number;
  maxPlayers: number;
  minPlayers: number;
  currentPlayers: number;
  startTime: Date;
  endTime?: Date;
  registrationDeadline: Date;
  duration?: number; // in minutes
  levels?: {
    level: number;
    smallBlind?: number;
    bigBlind?: number;
    ante?: number;
    duration: number;
  }[];
  prizeDistribution: {
    position: number;
    prize: number;
    percentage: number;
  }[];
  rules: Record<string, any>;
  createdAt: Date;
}

export interface TournamentPlayer {
  tournamentId: string;
  userId: string;
  username: string;
  avatar?: string;
  registeredAt: Date;
  currentPosition: number;
  score: number;
  chips?: number;
  eliminated: boolean;
  eliminatedAt?: Date;
  finalPosition?: number;
  prizeWon?: number;
}

export interface TournamentLeaderboard {
  tournamentId: string;
  players: {
    position: number;
    userId: string;
    username: string;
    avatar?: string;
    score: number;
    chips?: number;
    gamesPlayed: number;
  }[];
  lastUpdated: Date;
}

export interface TournamentRegistration {
  tournamentId: string;
  userId: string;
  entryFee: number;
  timestamp: Date;
}

export interface TournamentResult {
  tournamentId: string;
  userId: string;
  finalPosition: number;
  score: number;
  prizeWon: number;
  gamesPlayed: number;
  timestamp: Date;
}
