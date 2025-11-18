package com.casino.tournament.repository;

import com.casino.tournament.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, String> {

    List<Tournament> findByStatusOrderByStartTimeAsc(Tournament.TournamentStatus status);

    List<Tournament> findByStatusInOrderByStartTimeAsc(List<Tournament.TournamentStatus> statuses);

    List<Tournament> findByGameCodeAndStatusOrderByStartTimeAsc(
        String gameCode,
        Tournament.TournamentStatus status
    );

    List<Tournament> findByStartTimeBetweenOrderByStartTimeAsc(
        LocalDateTime start,
        LocalDateTime end
    );
}
