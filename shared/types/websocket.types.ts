export enum WebSocketEvent {
  // Connection events
  CONNECT = 'connect',
  DISCONNECT = 'disconnect',
  ERROR = 'error',

  // Authentication
  AUTHENTICATE = 'authenticate',
  AUTHENTICATED = 'authenticated',

  // Game events
  GAME_JOIN = 'game:join',
  GAME_LEAVE = 'game:leave',
  GAME_STATE = 'game:state',
  GAME_ACTION = 'game:action',
  GAME_RESULT = 'game:result',
  GAME_ERROR = 'game:error',

  // Tournament events
  TOURNAMENT_JOIN = 'tournament:join',
  TOURNAMENT_LEAVE = 'tournament:leave',
  TOURNAMENT_UPDATE = 'tournament:update',
  TOURNAMENT_START = 'tournament:start',
  TOURNAMENT_END = 'tournament:end',

  // Chat events
  CHAT_MESSAGE = 'chat:message',
  CHAT_HISTORY = 'chat:history',
  CHAT_USER_JOIN = 'chat:user_join',
  CHAT_USER_LEAVE = 'chat:user_leave',

  // Balance events
  BALANCE_UPDATE = 'balance:update',

  // Notification events
  NOTIFICATION = 'notification',
}

export interface WebSocketMessage<T = any> {
  event: WebSocketEvent;
  data: T;
  timestamp: Date;
  id?: string;
}

export interface ChatMessage {
  id: string;
  userId: string;
  username: string;
  avatar?: string;
  message: string;
  roomId: string;
  timestamp: Date;
  edited?: boolean;
  editedAt?: Date;
}

export interface GameUpdateMessage {
  gameId: string;
  sessionId: string;
  state: Record<string, any>;
  players?: {
    userId: string;
    username: string;
    status: string;
  }[];
}

export interface NotificationMessage {
  id: string;
  userId: string;
  type: 'info' | 'success' | 'warning' | 'error';
  title: string;
  message: string;
  link?: string;
  read: boolean;
  createdAt: Date;
}
