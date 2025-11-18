package com.casino.notification.service;

import com.casino.notification.entity.ChatMessage;
import com.casino.notification.entity.ChatModerationAction;
import com.casino.notification.entity.ChatRoom;
import com.casino.notification.exception.ChatException;
import com.casino.notification.repository.ChatMessageRepository;
import com.casino.notification.repository.ChatModerationRepository;
import com.casino.notification.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatModerationRepository chatModerationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final int MAX_MESSAGE_LENGTH = 500;
    private static final int SLOW_MODE_DEFAULT_SECONDS = 5;

    /**
     * Get all active chat rooms
     */
    public List<ChatRoom> getActiveRooms() {
        return chatRoomRepository.findByActiveTrue();
    }

    /**
     * Get room by code
     */
    public ChatRoom getRoom(String roomCode) {
        return chatRoomRepository.findByRoomCode(roomCode)
            .orElseThrow(() -> new ChatException("Chat room not found: " + roomCode));
    }

    /**
     * Create a new chat room
     */
    @Transactional
    public ChatRoom createRoom(ChatRoom room) {
        if (chatRoomRepository.existsByRoomCode(room.getRoomCode())) {
            throw new ChatException("Room code already exists");
        }

        room = chatRoomRepository.save(room);

        log.info("Created chat room: {} ({})", room.getRoomName(), room.getRoomCode());

        return room;
    }

    /**
     * Join a chat room
     */
    @Transactional
    public void joinRoom(String roomCode, String userId, String username) {
        ChatRoom room = getRoom(roomCode);

        if (room.isFull()) {
            throw new ChatException("Chat room is full");
        }

        // Check if user is banned
        if (isUserBanned(userId, room.getId())) {
            throw new ChatException("You are banned from this room");
        }

        room.addParticipant(userId);
        chatRoomRepository.save(room);

        // Send join notification
        ChatMessage joinMessage = ChatMessage.builder()
            .roomId(room.getId())
            .userId(userId)
            .username(username)
            .messageType(ChatMessage.MessageType.JOIN)
            .content(username + " joined the room")
            .isSystemMessage(true)
            .build();

        sendMessage(joinMessage);

        log.info("User {} joined room {}", userId, roomCode);
    }

    /**
     * Leave a chat room
     */
    @Transactional
    public void leaveRoom(String roomCode, String userId, String username) {
        ChatRoom room = getRoom(roomCode);

        room.removeParticipant(userId);
        chatRoomRepository.save(room);

        // Send leave notification
        ChatMessage leaveMessage = ChatMessage.builder()
            .roomId(room.getId())
            .userId(userId)
            .username(username)
            .messageType(ChatMessage.MessageType.LEAVE)
            .content(username + " left the room")
            .isSystemMessage(true)
            .build();

        sendMessage(leaveMessage);

        log.info("User {} left room {}", userId, roomCode);
    }

    /**
     * Send a message to a room
     */
    @Transactional
    public ChatMessage sendMessage(String roomCode, String userId, String username, String content) {
        ChatRoom room = getRoom(roomCode);

        // Validate user is participant
        if (!room.hasParticipant(userId)) {
            throw new ChatException("You are not a participant of this room");
        }

        // Check if user is muted
        if (isUserMuted(userId, room.getId())) {
            throw new ChatException("You are muted in this room");
        }

        // Check slow mode
        if (room.getSlowMode()) {
            int slowModeSeconds = room.getSlowModeSeconds() != null ?
                room.getSlowModeSeconds() : SLOW_MODE_DEFAULT_SECONDS;

            LocalDateTime since = LocalDateTime.now().minusSeconds(slowModeSeconds);
            Long recentMessageCount = chatMessageRepository.countByUserIdAndCreatedAtAfter(userId, since);

            if (recentMessageCount > 0) {
                throw new ChatException("Slow mode active. Please wait before sending another message.");
            }
        }

        // Validate message
        if (content == null || content.trim().isEmpty()) {
            throw new ChatException("Message cannot be empty");
        }

        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new ChatException("Message too long (max " + MAX_MESSAGE_LENGTH + " characters)");
        }

        ChatMessage message = ChatMessage.builder()
            .roomId(room.getId())
            .userId(userId)
            .username(username)
            .messageType(ChatMessage.MessageType.TEXT)
            .content(content.trim())
            .isSystemMessage(false)
            .build();

        return sendMessage(message);
    }

    /**
     * Internal method to send message (saves and broadcasts)
     */
    @Transactional
    public ChatMessage sendMessage(ChatMessage message) {
        // Save to database
        message = chatMessageRepository.save(message);

        // Broadcast to WebSocket subscribers
        messagingTemplate.convertAndSend("/topic/room." + message.getRoomId(), message);

        return message;
    }

    /**
     * Get recent messages for a room
     */
    public List<ChatMessage> getRecentMessages(String roomCode, int limit) {
        ChatRoom room = getRoom(roomCode);
        return chatMessageRepository.findActiveMessagesByRoom(
            room.getId(),
            PageRequest.of(0, limit)
        );
    }

    /**
     * Get messages since a certain time (for polling)
     */
    public List<ChatMessage> getMessagesSince(String roomCode, LocalDateTime since) {
        ChatRoom room = getRoom(roomCode);
        return chatMessageRepository.findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(
            room.getId(),
            since
        );
    }

    /**
     * Delete a message (moderation)
     */
    @Transactional
    public void deleteMessage(String messageId, String moderatorId, String reason) {
        ChatMessage message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new ChatException("Message not found"));

        message.markAsDeleted(moderatorId, reason);
        chatMessageRepository.save(message);

        // Notify subscribers
        messagingTemplate.convertAndSend("/topic/room." + message.getRoomId() + ".deleted", messageId);

        log.info("Message {} deleted by moderator {}", messageId, moderatorId);
    }

    /**
     * Mute a user
     */
    @Transactional
    public void muteUser(String userId, String roomId, String moderatorId, String reason, Integer durationMinutes) {
        LocalDateTime expiresAt = durationMinutes != null ?
            LocalDateTime.now().plusMinutes(durationMinutes) : null;

        ChatModerationAction action = ChatModerationAction.builder()
            .userId(userId)
            .roomId(roomId)
            .actionType(ChatModerationAction.ActionType.MUTE)
            .moderatorId(moderatorId)
            .reason(reason)
            .expiresAt(expiresAt)
            .build();

        chatModerationRepository.save(action);

        log.info("User {} muted in room {} by moderator {}. Duration: {} minutes",
            userId, roomId, moderatorId, durationMinutes);
    }

    /**
     * Ban a user
     */
    @Transactional
    public void banUser(String userId, String roomId, String moderatorId, String reason, boolean permanent) {
        LocalDateTime expiresAt = permanent ? null : LocalDateTime.now().plusDays(7);

        ChatModerationAction action = ChatModerationAction.builder()
            .userId(userId)
            .roomId(roomId)
            .actionType(ChatModerationAction.ActionType.BAN)
            .moderatorId(moderatorId)
            .reason(reason)
            .expiresAt(expiresAt)
            .build();

        chatModerationRepository.save(action);

        // Remove user from room
        if (roomId != null) {
            ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException("Room not found"));

            room.removeParticipant(userId);
            chatRoomRepository.save(room);
        }

        log.info("User {} banned from room {} by moderator {}. Permanent: {}",
            userId, roomId, moderatorId, permanent);
    }

    /**
     * Check if user is muted
     */
    public boolean isUserMuted(String userId, String roomId) {
        Optional<ChatModerationAction> action = chatModerationRepository.findActiveActionInRoom(
            userId,
            roomId,
            ChatModerationAction.ActionType.MUTE,
            LocalDateTime.now()
        );

        return action.isPresent() && action.get().isActive();
    }

    /**
     * Check if user is banned
     */
    public boolean isUserBanned(String userId, String roomId) {
        Optional<ChatModerationAction> action = chatModerationRepository.findActiveActionInRoom(
            userId,
            roomId,
            ChatModerationAction.ActionType.BAN,
            LocalDateTime.now()
        );

        return action.isPresent() && action.get().isActive();
    }

    /**
     * Get user's rooms
     */
    public List<ChatRoom> getUserRooms(String userId) {
        return chatRoomRepository.findRoomsByParticipant(userId);
    }

    /**
     * Get message count for a room
     */
    public Long getMessageCount(String roomCode) {
        ChatRoom room = getRoom(roomCode);
        return chatMessageRepository.countByRoomId(room.getId());
    }
}
