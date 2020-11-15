package com.agenson.cinema.user;

import com.agenson.cinema.security.restriction.RestrictToStaff;
import com.agenson.cinema.security.restriction.RestrictToUser;
import com.agenson.cinema.security.SecurityRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final BCryptPasswordEncoder encoder;

    private final UserRepository userRepository;

    @RestrictToUser
    public Optional<UserCompleteDTO> findUser(UUID uuid) {
        return this.userRepository.findByUuid(uuid).map(UserCompleteDTO::new);
    }

    @RestrictToStaff
    public List<UserDetailsDTO> findUsers() {
        return this.userRepository.findAll().stream().map(UserDetailsDTO::new).collect(Collectors.toList());
    }

    public UserBasicDTO createUser(String username, String password) {
        this.validateUsername(username);
        this.validatePassword(password);

        String encodedPassword = encoder.encode(password);
        UserDB user = this.userRepository.save(new UserDB(username, encodedPassword));

        return new UserBasicDTO(user);
    }

    @RestrictToStaff
    public Optional<UserDetailsDTO> updateUserRole(UUID uuid, SecurityRole role) {
        return this.userRepository.findByUuid(uuid).map(user -> {
           user.setRole(role);

           return new UserDetailsDTO(this.userRepository.save(user));
        });
    }

    private void validateUsername(String username) {
        if (username == null)
            throw new InvalidUserException(InvalidUserException.Type.USERNAME_MANDATORY);
        else if (username.trim().length() == 0)
            throw new InvalidUserException(InvalidUserException.Type.USERNAME_MANDATORY);
        else if (username.trim().length() > 16)
            throw new InvalidUserException(InvalidUserException.Type.USERNAME_MAXSIZE);
        else if (this.userRepository.findByUsername(username).isPresent())
            throw new InvalidUserException(InvalidUserException.Type.USERNAME_EXISTS);
    }

    private void validatePassword(String password) {
        if (password == null)
            throw new InvalidUserException(InvalidUserException.Type.PASSWORD_MANDATORY);
        else if (password.trim().length() == 0)
            throw new InvalidUserException(InvalidUserException.Type.PASSWORD_MANDATORY);
        else if (password.trim().length() > 16)
            throw new InvalidUserException(InvalidUserException.Type.PASSWORD_MAXSIZE);
    }
}
