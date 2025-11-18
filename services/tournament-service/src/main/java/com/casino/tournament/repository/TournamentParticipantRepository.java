package com.casino.tournament.repository;

import com.casino.tournament.entity.TournamentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, String> {

    List<TournamentParticipant> findByTournamentIdOrderByRankAsc(String tournamentId);

    List<TournamentParticipant> findByUserIdOrderByRegisteredAtDesc(String userId);

    Optional<TournamentParticipant> findByTournamentIdAndUserId(String tournamentId, String userId);

    boolean existsByTournamentIdAndUserId(String tournamentId, String userId);

    long countByTournamentId(String tournamentId);
}
