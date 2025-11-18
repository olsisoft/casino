package com.casino.notification.repository;

import com.casino.notification.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    Optional<ChatRoom> findByRoomCode(String roomCode);

    List<ChatRoom> findByActiveTrue();

    List<ChatRoom> findByRoomType(ChatRoom.RoomType roomType);

    List<ChatRoom> findByRoomTypeAndActiveTrue(ChatRoom.RoomType roomType, Boolean active);

    Optional<ChatRoom> findByGameCodeAndActiveTrue(String gameCode);

    @Query("SELECT r FROM ChatRoom r WHERE :userId MEMBER OF r.participants")
    List<ChatRoom> findRoomsByParticipant(String userId);

    @Query("SELECT r FROM ChatRoom r WHERE r.roomType = 'GLOBAL' AND r.active = true")
    List<ChatRoom> findGlobalRooms();

    boolean existsByRoomCode(String roomCode);
}
