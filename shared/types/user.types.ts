export enum UserRole {
  PLAYER = 'player',
  VIP = 'vip',
  ADMIN = 'admin',
  MODERATOR = 'moderator',
}

export enum UserStatus {
  ACTIVE = 'active',
  SUSPENDED = 'suspended',
  BANNED = 'banned',
  SELF_EXCLUDED = 'self_excluded',
}

export enum KYCStatus {
  NOT_STARTED = 'not_started',
  PENDING = 'pending',
  VERIFIED = 'verified',
  REJECTED = 'rejected',
}

export interface User {
  id: string;
  email: string;
  username: string;
  firstName?: string;
  lastName?: string;
  role: UserRole;
  status: UserStatus;
  kycStatus: KYCStatus;
  avatar?: string;
  level: number;
  xp: number;
  balance: number;
  bonusBalance: number;
  dateOfBirth?: Date;
  country?: string;
  phoneNumber?: string;
  emailVerified: boolean;
  phoneVerified: boolean;
  twoFactorEnabled: boolean;
  createdAt: Date;
  updatedAt: Date;
  lastLoginAt?: Date;
}

export interface UserProfile extends Omit<User, 'id'> {
  totalWagered: number;
  totalWon: number;
  gamesPlayed: number;
  achievementsUnlocked: number;
  currentStreak: number;
  bestStreak: number;
}

export interface UserSettings {
  userId: string;
  notifications: {
    email: boolean;
    push: boolean;
    inApp: boolean;
  };
  privacy: {
    showProfile: boolean;
    showStats: boolean;
  };
  responsibleGaming: {
    dailyDepositLimit?: number;
    weeklyDepositLimit?: number;
    monthlyDepositLimit?: number;
    sessionTimeLimit?: number;
    coolOffPeriod?: Date;
  };
  preferences: {
    language: string;
    currency: string;
    theme: 'light' | 'dark' | 'auto';
    soundEnabled: boolean;
    musicEnabled: boolean;
  };
}

export interface LoginRequest {
  email: string;
  password: string;
  twoFactorCode?: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  firstName?: string;
  lastName?: string;
  dateOfBirth: string;
  country: string;
  acceptTerms: boolean;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface AuthResponse {
  user: User;
  tokens: AuthTokens;
}
