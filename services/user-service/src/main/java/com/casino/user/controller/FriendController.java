package com.casino.user.controller;

import com.casino.user.entity.Friendship;
import com.casino.user.service.FriendService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request")
    public ResponseEntity<Friendship> sendFriendRequest(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody SendFriendRequestRequest request
    ) {
        log.info("POST /friends/request - userId: {}, toUserId: {}", userId, request.getToUserId());

        Friendship friendship = friendService.sendFriendRequest(userId, request.getToUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(friendship);
    }

    @PostMapping("/{friendshipId}/accept")
    public ResponseEntity<Friendship> acceptFriendRequest(
        @PathVariable String friendshipId,
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /friends/{}/accept - userId: {}", friendshipId, userId);

        Friendship friendship = friendService.acceptFriendRequest(userId, friendshipId);

        return ResponseEntity.ok(friendship);
    }

    @PostMapping("/{friendshipId}/reject")
    public ResponseEntity<Void> rejectFriendRequest(
        @PathVariable String friendshipId,
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /friends/{}/reject - userId: {}", friendshipId, userId);

        friendService.rejectFriendRequest(userId, friendshipId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<Void> removeFriend(
        @PathVariable String friendshipId,
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("DELETE /friends/{} - userId: {}", friendshipId, userId);

        friendService.removeFriend(userId, friendshipId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/block")
    public ResponseEntity<Friendship> blockUser(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody BlockUserRequest request
    ) {
        log.info("POST /friends/block - userId: {}, userToBlock: {}", userId, request.getUserToBlockId());

        Friendship friendship = friendService.blockUser(userId, request.getUserToBlockId());

        return ResponseEntity.ok(friendship);
    }

    @GetMapping
    public ResponseEntity<List<Friendship>> getFriends(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /friends - userId: {}", userId);

        List<Friendship> friends = friendService.getFriends(userId);

        return ResponseEntity.ok(friends);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Friendship>> getPendingRequests(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /friends/pending - userId: {}", userId);

        List<Friendship> requests = friendService.getPendingRequests(userId);

        return ResponseEntity.ok(requests);
    }

    @GetMapping("/sent")
    public ResponseEntity<List<Friendship>> getSentRequests(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /friends/sent - userId: {}", userId);

        List<Friendship> requests = friendService.getSentRequests(userId);

        return ResponseEntity.ok(requests);
    }

    @GetMapping("/count")
    public ResponseEntity<FriendCountResponse> getFriendCount(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /friends/count - userId: {}", userId);

        Long count = friendService.getFriendCount(userId);

        return ResponseEntity.ok(new FriendCountResponse(count));
    }

    @GetMapping("/check/{otherUserId}")
    public ResponseEntity<AreFriendsResponse> checkFriendship(
        @PathVariable String otherUserId,
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /friends/check/{} - userId: {}", otherUserId, userId);

        boolean areFriends = friendService.areFriends(userId, otherUserId);

        return ResponseEntity.ok(new AreFriendsResponse(areFriends));
    }

    @Data
    public static class SendFriendRequestRequest {
        @NotBlank
        private String toUserId;
    }

    @Data
    public static class BlockUserRequest {
        @NotBlank
        private String userToBlockId;
    }

    @Data
    public static class FriendCountResponse {
        private Long count;

        public FriendCountResponse(Long count) {
            this.count = count;
        }
    }

    @Data
    public static class AreFriendsResponse {
        private Boolean areFriends;

        public AreFriendsResponse(Boolean areFriends) {
            this.areFriends = areFriends;
        }
    }
}
