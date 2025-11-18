package com.casino.user.service;

import com.casino.user.entity.Friendship;
import com.casino.user.entity.User;
import com.casino.user.exception.FriendshipException;
import com.casino.user.repository.FriendshipRepository;
import com.casino.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * Send a friend request
     */
    @Transactional
    public Friendship sendFriendRequest(String fromUserId, String toUserId) {
        // Validate users exist
        if (!userRepository.existsById(fromUserId)) {
            throw new FriendshipException("User not found: " + fromUserId);
        }
        if (!userRepository.existsById(toUserId)) {
            throw new FriendshipException("User not found: " + toUserId);
        }

        // Can't friend yourself
        if (fromUserId.equals(toUserId)) {
            throw new FriendshipException("Cannot send friend request to yourself");
        }

        // Check if friendship already exists
        if (friendshipRepository.findByUsers(fromUserId, toUserId).isPresent()) {
            throw new FriendshipException("Friendship already exists or request already sent");
        }

        Friendship friendship = Friendship.builder()
            .userId1(fromUserId)
            .userId2(toUserId)
            .status(Friendship.FriendshipStatus.PENDING)
            .build();

        friendship = friendshipRepository.save(friendship);

        log.info("Friend request sent from {} to {}", fromUserId, toUserId);

        return friendship;
    }

    /**
     * Accept a friend request
     */
    @Transactional
    public Friendship acceptFriendRequest(String userId, String friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new FriendshipException("Friendship not found"));

        // Verify user is the recipient
        if (!friendship.getUserId2().equals(userId)) {
            throw new FriendshipException("You are not authorized to accept this request");
        }

        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            throw new FriendshipException("Request is not pending");
        }

        friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        friendship.setAcceptedAt(LocalDateTime.now());

        friendship = friendshipRepository.save(friendship);

        log.info("Friend request accepted: {} and {} are now friends",
            friendship.getUserId1(), friendship.getUserId2());

        return friendship;
    }

    /**
     * Reject a friend request
     */
    @Transactional
    public void rejectFriendRequest(String userId, String friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new FriendshipException("Friendship not found"));

        // Verify user is the recipient
        if (!friendship.getUserId2().equals(userId)) {
            throw new FriendshipException("You are not authorized to reject this request");
        }

        friendship.setStatus(Friendship.FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);

        log.info("Friend request rejected by {}", userId);
    }

    /**
     * Remove a friend
     */
    @Transactional
    public void removeFriend(String userId, String friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new FriendshipException("Friendship not found"));

        // Verify user is part of this friendship
        if (!friendship.getUserId1().equals(userId) && !friendship.getUserId2().equals(userId)) {
            throw new FriendshipException("You are not part of this friendship");
        }

        friendshipRepository.delete(friendship);

        log.info("Friendship removed: {}", friendshipId);
    }

    /**
     * Block a user
     */
    @Transactional
    public Friendship blockUser(String userId, String userToBlockId) {
        if (!userRepository.existsById(userToBlockId)) {
            throw new FriendshipException("User not found: " + userToBlockId);
        }

        // Check if friendship exists
        Friendship friendship = friendshipRepository.findByUsers(userId, userToBlockId)
            .orElse(Friendship.builder()
                .userId1(userId)
                .userId2(userToBlockId)
                .build());

        friendship.setStatus(Friendship.FriendshipStatus.BLOCKED);
        friendship = friendshipRepository.save(friendship);

        log.info("User {} blocked user {}", userId, userToBlockId);

        return friendship;
    }

    /**
     * Get all friends for a user
     */
    public List<Friendship> getFriends(String userId) {
        return friendshipRepository.findByUserIdAndStatus(userId, Friendship.FriendshipStatus.ACCEPTED);
    }

    /**
     * Get pending friend requests (received)
     */
    public List<Friendship> getPendingRequests(String userId) {
        return friendshipRepository.findPendingRequestsForUser(userId);
    }

    /**
     * Get sent friend requests
     */
    public List<Friendship> getSentRequests(String userId) {
        return friendshipRepository.findSentRequestsByUser(userId);
    }

    /**
     * Get friend count
     */
    public Long getFriendCount(String userId) {
        return friendshipRepository.countFriends(userId);
    }

    /**
     * Check if two users are friends
     */
    public boolean areFriends(String userId1, String userId2) {
        return friendshipRepository.areFriends(userId1, userId2);
    }

    /**
     * Get friend IDs for a user
     */
    public List<String> getFriendIds(String userId) {
        return getFriends(userId).stream()
            .map(f -> f.getOtherUserId(userId))
            .collect(Collectors.toList());
    }
}
