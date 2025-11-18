export enum AchievementCategory {
  GAMES = 'games',
  WINS = 'wins',
  SOCIAL = 'social',
  SPECIAL = 'special',
  PROGRESSION = 'progression',
}

export enum AchievementRarity {
  COMMON = 'common',
  RARE = 'rare',
  EPIC = 'epic',
  LEGENDARY = 'legendary',
}

export interface Achievement {
  id: string;
  name: string;
  description: string;
  category: AchievementCategory;
  rarity: AchievementRarity;
  icon: string;
  xpReward: number;
  bonusReward?: number;
  requirement: {
    type: string;
    value: number;
    gameType?: string;
  };
  isSecret: boolean;
  createdAt: Date;
}

export interface UserAchievement {
  achievementId: string;
  userId: string;
  progress: number;
  completed: boolean;
  completedAt?: Date;
  claimed: boolean;
  claimedAt?: Date;
}

export interface DailyMission {
  id: string;
  name: string;
  description: string;
  requirement: {
    type: string;
    value: number;
    gameType?: string;
  };
  xpReward: number;
  bonusReward?: number;
  expiresAt: Date;
}

export interface UserDailyMission {
  missionId: string;
  userId: string;
  progress: number;
  completed: boolean;
  completedAt?: Date;
  claimed: boolean;
}

export interface Leaderboard {
  id: string;
  name: string;
  type: 'daily' | 'weekly' | 'monthly' | 'all_time';
  metric: 'wins' | 'wagered' | 'xp' | 'profit';
  gameType?: string;
  startDate: Date;
  endDate?: Date;
  prizes: {
    position: number;
    prize: number;
  }[];
}

export interface LeaderboardEntry {
  leaderboardId: string;
  userId: string;
  username: string;
  avatar?: string;
  rank: number;
  score: number;
  change?: number; // position change
}
