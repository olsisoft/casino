package com.casino.game.config;

import com.casino.game.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final GameService gameService;

    /**
     * Clean up abandoned sessions every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void cleanupAbandonedSessions() {
        log.debug("Running scheduled task: cleanup abandoned sessions");
        gameService.cleanupAbandonedSessions();
    }
}
