package com.agenson.cinema.user;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final ModelMapper mapper;

    public Optional<UserDTO> findUser(UUID uuid) {
        return this.userRepository.findByUuid(uuid).map(this::toDTO);
    }

    public Optional<UserDTO> findUser(String username) {
        return this.userRepository.findByUsername(username).map(this::toDTO);
    }

    public List<UserDTO> findUsers() {
        return this.userRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public UserDTO loginUser(String username, String password) {
        return this.userRepository.findByUsername(username).map(user -> {
            if (this.passwordEncoder.matches(password, user.getPassword()))
                return this.toDTO(user);

            return null;
        }).orElseThrow(() -> new InvalidUserException(InvalidUserException.Type.CONNECTION));
    }

    public UserDTO createUser(String username, String password) {
        this.validateUsername(null, username);
        this.validatePassword(password);

        String encodedPassword = this.passwordEncoder.encode(password);

        return this.toDTO(this.userRepository.save(new UserDB(username, encodedPassword)));
    }

    public Optional<UserDTO> updateUserUsername(UUID uuid, String username) {
        return this.userRepository.findByUuid(uuid).map(user -> {
            this.validateUsername(uuid, username);
            user.setUsername(username);

            return this.toDTO(this.userRepository.save(user));
        });
    }

    public Optional<UserDTO> updateUserPassword(UUID uuid, String password) {
        return this.userRepository.findByUuid(uuid).map(user -> {
            this.validatePassword(password);

            String encodedPassword = this.passwordEncoder.encode(password);
            user.setPassword(encodedPassword);

            return this.toDTO(this.userRepository.save(user));
        });
    }

    public Optional<UserDTO> updateUserRole(UUID uuid, Role role) {
        return this.userRepository.findByUuid(uuid).map(user -> {
           user.setRole(role);

           return this.toDTO(this.userRepository.save(user));
        });
    }

    public void removeUser(UUID uuid) {
        this.userRepository.deleteByUuid(uuid);
    }

    private UserDTO toDTO(UserDB user) {
        return this.mapper.map(user, UserDTO.class);
    }

    private void validateUsername(UUID uuid, String username) {
        if (username == null)
            throw new InvalidUserException(InvalidUserException.Type.USERNAME_MANDATORY);
        else if (username.trim().length() == 0)
            throw new InvalidUserException(InvalidUserException.Type.USERNAME_MANDATORY);
        else if (username.trim().length() > 16)
            throw new InvalidUserException(InvalidUserException.Type.USERNAME_MAXSIZE);
        else {
            this.userRepository.findByUsername(username).ifPresent(userWithSameUsername -> {
                if (uuid == null || userWithSameUsername.getUuid() != uuid)
                    throw new InvalidUserException(InvalidUserException.Type.USERNAME_EXISTS);
            });
        }
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
