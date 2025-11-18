package com.casino.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_rooms", indexes = {
    @Index(name = "idx_room_type", columnList = "roomType"),
    @Index(name = "idx_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String roomCode;

    @Column(nullable = false)
    private String roomName;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private Boolean moderated;

    // For private/group rooms, store participant IDs
    @ElementCollection
    @CollectionTable(name = "chat_room_participants", joinColumns = @Column(name = "room_id"))
    @Column(name = "user_id")
    private Set<String> participants = new HashSet<>();

    // For game-specific rooms
    private String gameCode;

    // Room settings
    private Integer maxParticipants;
    private Boolean slowMode; // Limits message frequency
    private Integer slowModeSeconds;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (active == null) active = true;
        if (moderated == null) moderated = false;
        if (slowMode == null) slowMode = false;
        if (participants == null) participants = new HashSet<>();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum RoomType {
        GLOBAL,         // Platform-wide chat
        GAME,           // Game-specific chat (e.g., Blackjack room)
        TOURNAMENT,     // Tournament chat
        VIP,            // VIP users only
        PRIVATE,        // 1-on-1 private chat
        GROUP           // Private group chat
    }

    public void addParticipant(String userId) {
        participants.add(userId);
    }

    public void removeParticipant(String userId) {
        participants.remove(userId);
    }

    public boolean hasParticipant(String userId) {
        return participants.contains(userId);
    }

    public int getParticipantCount() {
        return participants.size();
    }

    public boolean isFull() {
        return maxParticipants != null && participants.size() >= maxParticipants;
    }
}
