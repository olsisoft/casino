package com.casino.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_room_id", columnList = "roomId"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_room_created", columnList = "roomId,createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private String userId;

    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    @Column(nullable = false, length = 2000)
    private String content;

    // For replies/threads
    private String replyToMessageId;

    // For system messages
    private Boolean isSystemMessage;

    // Moderation
    private Boolean deleted;
    private String deletedBy;
    private LocalDateTime deletedAt;
    private String deleteReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isSystemMessage == null) {
            isSystemMessage = false;
        }
        if (deleted == null) {
            deleted = false;
        }
    }

    public enum MessageType {
        TEXT,           // Regular text message
        EMOJI,          // Emoji-only message
        SYSTEM,         // System announcement
        JOIN,           // User joined room
        LEAVE,          // User left room
        GIFT,           // Virtual gift sent
        REACTION        // Reaction to another message
    }

    public boolean isDeleted() {
        return deleted != null && deleted;
    }

    public void markAsDeleted(String moderatorId, String reason) {
        this.deleted = true;
        this.deletedBy = moderatorId;
        this.deletedAt = LocalDateTime.now();
        this.deleteReason = reason;
    }
}
