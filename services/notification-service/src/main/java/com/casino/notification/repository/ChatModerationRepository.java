package com.casino.notification.repository;

import com.casino.notification.entity.ChatModerationAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatModerationRepository extends JpaRepository<ChatModerationAction, String> {

    List<ChatModerationAction> findByUserIdAndActiveTrue(String userId);

    List<ChatModerationAction> findByUserIdAndRoomIdAndActiveTrue(String userId, String roomId);

    @Query("SELECT a FROM ChatModerationAction a WHERE a.userId = :userId AND a.actionType = :actionType AND a.active = true AND (a.expiresAt IS NULL OR a.expiresAt > :now)")
    Optional<ChatModerationAction> findActiveActionByTypeAndUser(
        String userId,
        ChatModerationAction.ActionType actionType,
        LocalDateTime now
    );

    @Query("SELECT a FROM ChatModerationAction a WHERE a.userId = :userId AND a.roomId = :roomId AND a.actionType = :actionType AND a.active = true AND (a.expiresAt IS NULL OR a.expiresAt > :now)")
    Optional<ChatModerationAction> findActiveActionInRoom(
        String userId,
        String roomId,
        ChatModerationAction.ActionType actionType,
        LocalDateTime now
    );

    List<ChatModerationAction> findByModeratorIdOrderByCreatedAtDesc(String moderatorId);
}
