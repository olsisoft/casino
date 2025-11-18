package com.casino.user.repository;

import com.casino.user.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, String> {

    Optional<AdminUser> findByUsername(String username);

    Optional<AdminUser> findByEmail(String email);

    List<AdminUser> findByRole(AdminUser.AdminRole role);

    List<AdminUser> findByIsActiveTrue();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
