package com.casino.user.repository;

import com.casino.user.entity.VipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VipTierRepository extends JpaRepository<VipTier, String> {

    Optional<VipTier> findByLevel(Integer level);

    Optional<VipTier> findByName(String name);

    List<VipTier> findByIsActiveTrueOrderByLevelAsc();

    List<VipTier> findAllByOrderByLevelAsc();
}
