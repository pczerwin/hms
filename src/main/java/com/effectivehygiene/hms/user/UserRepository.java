package com.effectivehygiene.hms.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds an active user by username.
     * Deactivated users are excluded — they cannot log in.
     */
    Optional<User> findByUsernameAndActiveTrue(String username);
}

