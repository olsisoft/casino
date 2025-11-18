export enum TransactionType {
  DEPOSIT = 'deposit',
  WITHDRAWAL = 'withdrawal',
  BET = 'bet',
  WIN = 'win',
  BONUS = 'bonus',
  REFUND = 'refund',
  FEE = 'fee',
}

export enum TransactionStatus {
  PENDING = 'pending',
  PROCESSING = 'processing',
  COMPLETED = 'completed',
  FAILED = 'failed',
  CANCELLED = 'cancelled',
  REVERSED = 'reversed',
}

export enum PaymentMethod {
  CREDIT_CARD = 'credit_card',
  DEBIT_CARD = 'debit_card',
  BANK_TRANSFER = 'bank_transfer',
  E_WALLET = 'e_wallet',
  CRYPTO = 'crypto',
  PAYPAL = 'paypal',
  STRIPE = 'stripe',
}

export interface Transaction {
  id: string;
  userId: string;
  type: TransactionType;
  status: TransactionStatus;
  amount: number;
  currency: string;
  paymentMethod?: PaymentMethod;
  paymentMethodDetails?: Record<string, any>;
  description?: string;
  metadata?: Record<string, any>;
  balanceBefore: number;
  balanceAfter: number;
  createdAt: Date;
  processedAt?: Date;
  completedAt?: Date;
  failureReason?: string;
  externalId?: string; // Stripe/payment gateway ID
}

export interface DepositRequest {
  amount: number;
  currency: string;
  paymentMethod: PaymentMethod;
  paymentMethodId?: string; // Stripe payment method ID
  savePaymentMethod?: boolean;
}

export interface DepositResponse {
  transaction: Transaction;
  clientSecret?: string; // For Stripe payment confirmation
  requiresAction?: boolean;
}

export interface WithdrawalRequest {
  amount: number;
  currency: string;
  paymentMethod: PaymentMethod;
  paymentMethodId?: string;
  destinationDetails: {
    accountNumber?: string;
    routingNumber?: string;
    iban?: string;
    walletAddress?: string;
    email?: string;
  };
}

export interface WithdrawalResponse {
  transaction: Transaction;
  estimatedCompletionTime: Date;
}

export interface PaymentMethodInfo {
  id: string;
  userId: string;
  type: PaymentMethod;
  isDefault: boolean;
  details: {
    last4?: string;
    brand?: string;
    expiryMonth?: number;
    expiryYear?: number;
    email?: string;
    walletType?: string;
  };
  createdAt: Date;
}

export interface Balance {
  userId: string;
  realBalance: number;
  bonusBalance: number;
  totalBalance: number;
  currency: string;
  lockedAmount: number;
  availableAmount: number;
  lastUpdated: Date;
}

export interface BonusInfo {
  id: string;
  userId: string;
  type: 'deposit' | 'no_deposit' | 'free_spins' | 'cashback';
  amount: number;
  wageringRequirement: number;
  wageringProgress: number;
  expiresAt: Date;
  isActive: boolean;
  conditions: Record<string, any>;
}
