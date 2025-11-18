// User types
export * from './user.types';

// Game types
export * from './game.types';

// Payment types
export * from './payment.types';

// Tournament types
export * from './tournament.types';

// Achievement types
export * from './achievement.types';

// WebSocket types
export * from './websocket.types';

// Common response types
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: {
    code: string;
    message: string;
    details?: Record<string, any>;
  };
  timestamp: Date;
}

export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface ErrorResponse {
  statusCode: number;
  message: string;
  error?: string;
  timestamp: Date;
  path?: string;
}
