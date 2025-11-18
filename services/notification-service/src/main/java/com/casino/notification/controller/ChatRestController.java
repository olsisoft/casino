package com.casino.notification.controller;

import com.casino.notification.entity.ChatMessage;
import com.casino.notification.entity.ChatRoom;
import com.casino.notification.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getActiveRooms() {
        log.info("GET /chat/rooms");
        List<ChatRoom> rooms = chatService.getActiveRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/rooms/{roomCode}")
    public ResponseEntity<ChatRoom> getRoom(@PathVariable String roomCode) {
        log.info("GET /chat/rooms/{}", roomCode);
        ChatRoom room = chatService.getRoom(roomCode);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoom> createRoom(
        @RequestHeader("X-Admin-Id") String adminId,
        @Valid @RequestBody CreateRoomRequest request
    ) {
        log.info("POST /chat/rooms - admin: {}, roomCode: {}", adminId, request.getRoomCode());

        ChatRoom room = ChatRoom.builder()
            .roomCode(request.getRoomCode())
            .roomName(request.getRoomName())
            .description(request.getDescription())
            .roomType(request.getRoomType())
            .maxParticipants(request.getMaxParticipants())
            .slowMode(request.getSlowMode())
            .slowModeSeconds(request.getSlowModeSeconds())
            .moderated(request.getModerated())
            .build();

        room = chatService.createRoom(room);

        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @GetMapping("/rooms/{roomCode}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(
        @PathVariable String roomCode,
        @RequestParam(defaultValue = "50") int limit
    ) {
        log.info("GET /chat/rooms/{}/messages?limit={}", roomCode, limit);
        List<ChatMessage> messages = chatService.getRecentMessages(roomCode, limit);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/rooms/{roomCode}/messages/since")
    public ResponseEntity<List<ChatMessage>> getMessagesSince(
        @PathVariable String roomCode,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since
    ) {
        log.info("GET /chat/rooms/{}/messages/since?since={}", roomCode, since);
        List<ChatMessage> messages = chatService.getMessagesSince(roomCode, since);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/rooms/{roomCode}/join")
    public ResponseEntity<Void> joinRoom(
        @PathVariable String roomCode,
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader("X-Username") String username
    ) {
        log.info("POST /chat/rooms/{}/join - userId: {}", roomCode, userId);
        chatService.joinRoom(roomCode, userId, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomCode}/leave")
    public ResponseEntity<Void> leaveRoom(
        @PathVariable String roomCode,
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader("X-Username") String username
    ) {
        log.info("POST /chat/rooms/{}/leave - userId: {}", roomCode, userId);
        chatService.leaveRoom(roomCode, userId, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomCode}/messages")
    public ResponseEntity<ChatMessage> sendMessage(
        @PathVariable String roomCode,
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader("X-Username") String username,
        @Valid @RequestBody SendMessageRequest request
    ) {
        log.info("POST /chat/rooms/{}/messages - userId: {}", roomCode, userId);

        ChatMessage message = chatService.sendMessage(
            roomCode,
            userId,
            username,
            request.getContent()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping("/my-rooms")
    public ResponseEntity<List<ChatRoom>> getMyRooms(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /chat/my-rooms - userId: {}", userId);
        List<ChatRoom> rooms = chatService.getUserRooms(userId);
        return ResponseEntity.ok(rooms);
    }

    // Moderation endpoints

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
        @PathVariable String messageId,
        @RequestHeader("X-Moderator-Id") String moderatorId,
        @RequestParam String reason
    ) {
        log.info("DELETE /chat/messages/{} - moderator: {}", messageId, moderatorId);
        chatService.deleteMessage(messageId, moderatorId, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/moderation/mute")
    public ResponseEntity<Void> muteUser(
        @RequestHeader("X-Moderator-Id") String moderatorId,
        @Valid @RequestBody MuteUserRequest request
    ) {
        log.info("POST /chat/moderation/mute - moderator: {}, user: {}", moderatorId, request.getUserId());

        chatService.muteUser(
            request.getUserId(),
            request.getRoomId(),
            moderatorId,
            request.getReason(),
            request.getDurationMinutes()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/moderation/ban")
    public ResponseEntity<Void> banUser(
        @RequestHeader("X-Moderator-Id") String moderatorId,
        @Valid @RequestBody BanUserRequest request
    ) {
        log.info("POST /chat/moderation/ban - moderator: {}, user: {}", moderatorId, request.getUserId());

        chatService.banUser(
            request.getUserId(),
            request.getRoomId(),
            moderatorId,
            request.getReason(),
            request.getPermanent()
        );

        return ResponseEntity.ok().build();
    }

    @Data
    public static class CreateRoomRequest {
        @NotBlank
        private String roomCode;

        @NotBlank
        private String roomName;

        private String description;

        @NotNull
        private ChatRoom.RoomType roomType;

        private Integer maxParticipants;
        private Boolean slowMode;
        private Integer slowModeSeconds;
        private Boolean moderated;
    }

    @Data
    public static class SendMessageRequest {
        @NotBlank
        private String content;
    }

    @Data
    public static class MuteUserRequest {
        @NotBlank
        private String userId;

        private String roomId; // null = global mute

        @NotBlank
        private String reason;

        private Integer durationMinutes; // null = permanent
    }

    @Data
    public static class BanUserRequest {
        @NotBlank
        private String userId;

        private String roomId; // null = global ban

        @NotBlank
        private String reason;

        private Boolean permanent;
    }
}
