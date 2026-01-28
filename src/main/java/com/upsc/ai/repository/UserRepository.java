package com.upsc.ai.repository;

import com.upsc.ai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByOauthProviderAndOauthProviderId(String provider, String providerId);

    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);
}
