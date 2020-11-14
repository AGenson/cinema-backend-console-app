package com.agenson.cinema.security;

import com.agenson.cinema.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final BCryptPasswordEncoder encoder;

    private final UserRepository userRepository;

    @Value
    public static class UserDetails {

        UUID uuid;
        String username;
        SecurityRole role;
    }

    private UserDetails currentUser = null;

    public Optional<UserDetails> getCurrentUser() {
        if (this.currentUser == null)
            return Optional.empty();

        return Optional.of(this.currentUser);
    }

    public UserDetails login(String username, String password) {
        return this.userRepository.findByUsername(username).map(user -> {
            if (password != null && encoder.matches(password, user.getPassword())) {
                this.currentUser = new UserDetails(user.getUuid(), user.getUsername(), user.getRole());

                return this.currentUser;
            }

            return null;
        }).orElseThrow(() -> new SecurityException(SecurityException.Type.CONNECTION));
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return this.currentUser != null;
    }

    public boolean hasRole(SecurityRole role) {
        return this.isLoggedIn() && this.currentUser.getRole() == role;
    }

    public boolean isUser(UUID uuid) {
        return this.isLoggedIn() && this.currentUser.getUuid().equals(uuid);
    }
}
