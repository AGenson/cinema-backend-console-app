package com.agenson.cinema.security;

import com.agenson.cinema.user.UserDetailsDTO;
import com.agenson.cinema.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final BCryptPasswordEncoder encoder;

    private final UserRepository userRepository;

    private UserDetailsDTO currentUser = null;

    public Optional<UserDetailsDTO> getCurrentUser() {
        return (this.currentUser != null) ? Optional.of(this.currentUser) : Optional.empty();
    }

    public UserDetailsDTO login(String username, String password) {
        return this.userRepository.findByUsername(username).map(user -> {
            if (password != null && encoder.matches(password, user.getPassword())) {
                this.currentUser = new UserDetailsDTO(user.getUuid(), user.getUsername(), user.getRole());

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
