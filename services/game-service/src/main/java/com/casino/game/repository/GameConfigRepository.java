package com.casino.game.repository;

import com.casino.game.entity.GameConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameConfigRepository extends JpaRepository<GameConfig, String> {

    Optional<GameConfig> findByGameCode(String gameCode);

    List<GameConfig> findByActiveTrue();

    List<GameConfig> findByGameType(GameConfig.GameType gameType);

    List<GameConfig> findByGameTypeAndActiveTrue(GameConfig.GameType gameType, boolean active);

    @Query("SELECT gc FROM GameConfig gc WHERE gc.active = true ORDER BY gc.totalPlays DESC")
    List<GameConfig> findMostPopular();

    @Query("SELECT gc FROM GameConfig gc WHERE gc.active = true ORDER BY gc.createdAt DESC")
    List<GameConfig> findNewest();

    @Modifying
    @Query("UPDATE GameConfig gc SET gc.totalPlays = gc.totalPlays + 1, " +
           "gc.activePlayers = gc.activePlayers + 1 WHERE gc.gameCode = :gameCode")
    int incrementPlayCount(@Param("gameCode") String gameCode);

    @Modifying
    @Query("UPDATE GameConfig gc SET gc.activePlayers = gc.activePlayers - 1 " +
           "WHERE gc.gameCode = :gameCode AND gc.activePlayers > 0")
    int decrementActiveCount(@Param("gameCode") String gameCode);

    @Modifying
    @Query("UPDATE GameConfig gc SET gc.totalWagered = gc.totalWagered + :amount " +
           "WHERE gc.gameCode = :gameCode")
    int addToTotalWagered(@Param("gameCode") String gameCode, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE GameConfig gc SET gc.totalPaidOut = gc.totalPaidOut + :amount " +
           "WHERE gc.gameCode = :gameCode")
    int addToTotalPaidOut(@Param("gameCode") String gameCode, @Param("amount") BigDecimal amount);

    boolean existsByGameCode(String gameCode);
}
