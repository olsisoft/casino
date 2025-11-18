package com.casino.user.repository;

import com.casino.user.entity.UserVipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserVipStatusRepository extends JpaRepository<UserVipStatus, String> {

    Optional<UserVipStatus> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
