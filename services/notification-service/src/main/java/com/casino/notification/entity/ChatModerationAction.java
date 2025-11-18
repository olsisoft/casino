package com.casino.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_moderation_actions", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_room_id", columnList = "roomId"),
    @Index(name = "idx_expires_at", columnList = "expiresAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatModerationAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    private String roomId; // null = global action

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    @Column(nullable = false)
    private String moderatorId;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt; // null = permanent

    private Boolean active;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
    }

    public enum ActionType {
        MUTE,           // Cannot send messages
        BAN,            // Cannot access room/chat
        WARNING,        // Warning issued (no restrictions)
        KICK            // Removed from room
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return active != null && active && !isExpired();
    }
}
