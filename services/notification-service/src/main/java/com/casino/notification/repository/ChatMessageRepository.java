package com.casino.notification.repository;

import com.casino.notification.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    List<ChatMessage> findByRoomIdOrderByCreatedAtDesc(String roomId, Pageable pageable);

    List<ChatMessage> findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(String roomId, LocalDateTime after);

    List<ChatMessage> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE m.roomId = :roomId AND m.deleted = false ORDER BY m.createdAt DESC")
    List<ChatMessage> findActiveMessagesByRoom(String roomId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.roomId = :roomId")
    Long countByRoomId(String roomId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.userId = :userId AND m.createdAt > :since")
    Long countByUserIdAndCreatedAtAfter(String userId, LocalDateTime since);

    void deleteByRoomId(String roomId);
}
