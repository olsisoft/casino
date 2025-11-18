package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "friendships", indexes = {
    @Index(name = "idx_user1_user2", columnList = "userId1,userId2", unique = true),
    @Index(name = "idx_user1_status", columnList = "userId1,status"),
    @Index(name = "idx_user2_status", columnList = "userId2,status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId1; // User who sent the request

    @Column(nullable = false)
    private String userId2; // User who received the request

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = FriendshipStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum FriendshipStatus {
        PENDING,    // Request sent, awaiting response
        ACCEPTED,   // Friends
        REJECTED,   // Request rejected
        BLOCKED     // User blocked
    }

    public boolean isFriend(String userId) {
        return status == FriendshipStatus.ACCEPTED &&
               (userId1.equals(userId) || userId2.equals(userId));
    }

    public String getOtherUserId(String userId) {
        return userId1.equals(userId) ? userId2 : userId1;
    }
}
