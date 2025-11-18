package com.casino.user.repository;

import com.casino.user.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    @Query("SELECT f FROM Friendship f WHERE (f.userId1 = :userId OR f.userId2 = :userId) AND f.status = :status")
    List<Friendship> findByUserIdAndStatus(String userId, Friendship.FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE f.userId2 = :userId AND f.status = 'PENDING'")
    List<Friendship> findPendingRequestsForUser(String userId);

    @Query("SELECT f FROM Friendship f WHERE f.userId1 = :userId AND f.status = 'PENDING'")
    List<Friendship> findSentRequestsByUser(String userId);

    @Query("SELECT f FROM Friendship f WHERE ((f.userId1 = :userId1 AND f.userId2 = :userId2) OR (f.userId1 = :userId2 AND f.userId2 = :userId1))")
    Optional<Friendship> findByUsers(String userId1, String userId2);

    @Query("SELECT COUNT(f) FROM Friendship f WHERE (f.userId1 = :userId OR f.userId2 = :userId) AND f.status = 'ACCEPTED'")
    Long countFriends(String userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f WHERE ((f.userId1 = :userId1 AND f.userId2 = :userId2) OR (f.userId1 = :userId2 AND f.userId2 = :userId1)) AND f.status = 'ACCEPTED'")
    boolean areFriends(String userId1, String userId2);
}
