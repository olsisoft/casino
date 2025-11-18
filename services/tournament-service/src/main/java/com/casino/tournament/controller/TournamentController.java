package com.casino.tournament.controller;

import com.casino.tournament.entity.Tournament;
import com.casino.tournament.entity.TournamentParticipant;
import com.casino.tournament.service.TournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    @GetMapping
    public ResponseEntity<List<Tournament>> getActiveTournaments() {
        log.info("GET /tournaments - Fetching active tournaments");
        return ResponseEntity.ok(tournamentService.getActiveTournaments());
    }

    @GetMapping("/{tournamentId}")
    public ResponseEntity<Tournament> getTournament(@PathVariable String tournamentId) {
        log.info("GET /tournaments/{}", tournamentId);
        return ResponseEntity.ok(tournamentService.getTournament(tournamentId));
    }

    @GetMapping("/{tournamentId}/leaderboard")
    public ResponseEntity<List<TournamentParticipant>> getLeaderboard(@PathVariable String tournamentId) {
        log.info("GET /tournaments/{}/leaderboard", tournamentId);
        return ResponseEntity.ok(tournamentService.getLeaderboard(tournamentId));
    }

    @PostMapping("/{tournamentId}/register")
    public ResponseEntity<TournamentParticipant> register(
        @PathVariable String tournamentId,
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader("X-Username") String username
    ) {
        log.info("POST /tournaments/{}/register - userId: {}", tournamentId, userId);
        return ResponseEntity.ok(tournamentService.registerParticipant(tournamentId, userId, username));
    }

    @GetMapping("/user/history")
    public ResponseEntity<List<TournamentParticipant>> getUserTournaments(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /tournaments/user/history - userId: {}", userId);
        return ResponseEntity.ok(tournamentService.getUserTournaments(userId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Tournament Service is running");
    }
}
