package com.effectivehygiene.hms.security;

import com.effectivehygiene.hms.user.User;
import com.effectivehygiene.hms.user.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads user credentials from the database for Spring Security authentication.
 * Replaces the in-memory admin account used during early development.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Called by Spring Security on every login attempt.
     * Returns a UserDetails built from the DB record.
     * Throws UsernameNotFoundException if the user is not found or is inactive.
     * Note: com.effectivehygiene.hms.user.User (entity) is imported as User;
     *       org.springframework.security.core.userdetails.User is referenced by FQN to avoid clash.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}


