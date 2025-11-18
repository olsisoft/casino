package com.casino.payment.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/users/balance/real/{userId}")
    BigDecimal getRealBalance(@PathVariable("userId") String userId);

    @PostMapping("/users/balance/add")
    void addRealBalance(
        @RequestParam("userId") String userId,
        @RequestParam("amount") BigDecimal amount
    );

    @PostMapping("/users/balance/deduct")
    boolean deductRealBalance(
        @RequestParam("userId") String userId,
        @RequestParam("amount") BigDecimal amount
    );

    @PostMapping("/users/balance/lock")
    void lockAmount(
        @RequestParam("userId") String userId,
        @RequestParam("amount") BigDecimal amount
    );

    @PostMapping("/users/balance/unlock")
    void unlockAmount(
        @RequestParam("userId") String userId,
        @RequestParam("amount") BigDecimal amount
    );
}
