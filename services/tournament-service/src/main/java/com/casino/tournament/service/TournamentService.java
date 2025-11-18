package com.casino.tournament.service;

import com.casino.tournament.entity.Tournament;
import com.casino.tournament.entity.TournamentParticipant;
import com.casino.tournament.repository.TournamentParticipantRepository;
import com.casino.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;

    public List<Tournament> getActiveTournaments() {
        return tournamentRepository.findByStatusInOrderByStartTimeAsc(
            List.of(Tournament.TournamentStatus.UPCOMING,
                   Tournament.TournamentStatus.REGISTRATION_OPEN)
        );
    }

    public Tournament getTournament(String tournamentId) {
        return tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new RuntimeException("Tournament not found"));
    }

    public List<TournamentParticipant> getLeaderboard(String tournamentId) {
        return participantRepository.findByTournamentIdOrderByRankAsc(tournamentId);
    }

    @Transactional
    public TournamentParticipant registerParticipant(String tournamentId, String userId, String username) {
        Tournament tournament = getTournament(tournamentId);

        if (tournament.getStatus() != Tournament.TournamentStatus.REGISTRATION_OPEN) {
            throw new RuntimeException("Registration is closed for this tournament");
        }

        if (tournament.getCurrentParticipants() >= tournament.getMaxParticipants()) {
            throw new RuntimeException("Tournament is full");
        }

        if (participantRepository.existsByTournamentIdAndUserId(tournamentId, userId)) {
            throw new RuntimeException("Already registered for this tournament");
        }

        TournamentParticipant participant = TournamentParticipant.builder()
            .tournamentId(tournamentId)
            .userId(userId)
            .username(username)
            .build();

        participant = participantRepository.save(participant);

        tournament.setCurrentParticipants(tournament.getCurrentParticipants() + 1);
        tournamentRepository.save(tournament);

        log.info("User {} registered for tournament {}", userId, tournamentId);
        return participant;
    }

    public List<TournamentParticipant> getUserTournaments(String userId) {
        return participantRepository.findByUserIdOrderByRegisteredAtDesc(userId);
    }
}
