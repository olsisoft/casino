package com.casino.notification.controller;

import com.casino.notification.entity.ChatMessage;
import com.casino.notification.service.ChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    /**
     * Handle messages sent to /app/chat/{roomCode}/send
     * Broadcasts to /topic/room.{roomId}
     */
    @MessageMapping("/chat/{roomCode}/send")
    public void sendMessage(
        @DestinationVariable String roomCode,
        @Payload ChatMessagePayload payload,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        log.info("WebSocket message from user {} to room {}: {}", userId, roomCode, payload.getContent());

        chatService.sendMessage(roomCode, userId, username, payload.getContent());
    }

    /**
     * Handle join room request
     */
    @MessageMapping("/chat/{roomCode}/join")
    public void joinRoom(
        @DestinationVariable String roomCode,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        log.info("User {} joining room {}", userId, roomCode);

        chatService.joinRoom(roomCode, userId, username);
    }

    /**
     * Handle leave room request
     */
    @MessageMapping("/chat/{roomCode}/leave")
    public void leaveRoom(
        @DestinationVariable String roomCode,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        log.info("User {} leaving room {}", userId, roomCode);

        chatService.leaveRoom(roomCode, userId, username);
    }

    @Data
    public static class ChatMessagePayload {
        private String content;
        private String replyToMessageId;
    }
}
